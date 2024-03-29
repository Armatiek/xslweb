/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.armatiek.xslweb.configuration;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.input.XmlStreamReader;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.apache.fop.apps.FopFactory;
import org.apache.shiro.config.Ini;
import org.apache.shiro.web.env.IniWebEnvironment;
import org.apache.shiro.web.env.WebEnvironment;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import net.sf.joost.trax.TrAXConstants;
import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.s9api.ExtensionFunction;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.UnprefixedElementMatchingPolicy;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.trace.TraceCodeInjector;
import net.sf.saxon.xpath.XPathFactoryImpl;
import net.sf.webdav.LocalFileSystemStore;
import net.sf.webdav.WebDavServletBean;
import nl.armatiek.xslweb.ehcache.DefaultExpiryPolicy;
import nl.armatiek.xslweb.error.XSLWebException;
import nl.armatiek.xslweb.joost.MessageEmitter;
import nl.armatiek.xslweb.quartz.NonConcurrentExecutionXSLWebJob;
import nl.armatiek.xslweb.quartz.XSLWebJob;
import nl.armatiek.xslweb.saxon.configuration.XSLWebConfiguration;
import nl.armatiek.xslweb.saxon.debug.DebugTraceCodeInjector;
import nl.armatiek.xslweb.saxon.errrorlistener.TransformationErrorListener;
import nl.armatiek.xslweb.saxon.errrorlistener.ValidatorErrorHandler;
import nl.armatiek.xslweb.saxon.utils.SaxonUtils;
import nl.armatiek.xslweb.utils.XMLUtils;
import nl.armatiek.xslweb.utils.XSLWebUtils;

public class WebApp implements ErrorHandler {
  
  private static final Logger logger = LoggerFactory.getLogger(WebApp.class);
  
  private Map<String, XsltExecutable> xsltExecutableCache = new ConcurrentHashMap<String, XsltExecutable>();
  private Map<String, XQueryExecutable> xqueryExecutableCache = new ConcurrentHashMap<String, XQueryExecutable>();
  private Map<String, Templates> templatesCache =  new ConcurrentHashMap<String, Templates>();
  private Map<String, Schema> schemaCache = new ConcurrentHashMap<String, Schema>();
  private Map<String, byte[]> stylesheetExportFileCache = new ConcurrentHashMap<String, byte[]>();
  private Map<String, ArrayList<Attribute>> attributes = new ConcurrentHashMap<String, ArrayList<Attribute>>();
  private Map<String, ComboPooledDataSource> dataSourceCache = new ConcurrentHashMap<String, ComboPooledDataSource>();
  private Map<String, ScriptEngine> scriptEngineCache = new ConcurrentHashMap<String, ScriptEngine>();
  private Map<String, FopFactory> fopFactoryCache = new ConcurrentHashMap<String, FopFactory>();
  private Map<String, ExecutorService> executorServiceCache = new ConcurrentHashMap<String, ExecutorService>();
  private Map<String, ExtensionFunctionDefinition> extensionFunctionDefinitions = new ConcurrentHashMap<String, ExtensionFunctionDefinition>();
  private Map<String, ExtensionFunction> extensionFunctions = new ConcurrentHashMap<String, ExtensionFunction>();
  private CacheManager cacheManager;
  private CacheConfigurationBuilder<String, ArrayList> cacheConfig;
      
  private volatile boolean isClosed = true;
  private File definition;
  private File homeDir;  
  private String name;
  private String title;
  private String description;
  private boolean developmentMode;
  private boolean debugMode;
  private boolean waitForJobsAtClose;
  // private boolean disableCookieManagement;
  private int maxUploadSize;
  private String cacheBusterId;
  private Scheduler scheduler;
  private List<Resource> resources = new ArrayList<Resource>();
  private List<Parameter> parameters = new ArrayList<Parameter>();
  private Map<String, DataSource> dataSources = new HashMap<String, DataSource>();
  private Map<String, String> fopConfigs = new HashMap<String, String>();
  private Map<String, Queue> queues = new HashMap<String, Queue>();
  private XsltExecutable identityXsltExecutable;
  private XSLWebConfiguration configuration;  
  private Processor processor;  
  private Fingerprints fingerprints;
  private FileAlterationMonitor monitor;
  private WebEnvironment shiroWebEnvironment;
  private WebDavServletBean webDavServletBean;
  // private CloseableHttpClient httpClient;
  private volatile int jobRequestCount = 0;
  
  public WebApp(File webAppDefinition) throws Exception {   
    logger.info(String.format("Loading webapp definition \"%s\" ...", webAppDefinition.getAbsolutePath()));
    
    Context context = Context.getInstance();
    this.definition = webAppDefinition;
    this.homeDir = webAppDefinition.getParentFile();
    this.name = this.homeDir.getName();
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);    
    dbf.setSchema(context.getWebAppSchema());    
    dbf.setXIncludeAware(true);
    DocumentBuilder db = dbf.newDocumentBuilder();
    db.setErrorHandler(this); 
    
    String defXml = IOUtils.toString(new XmlStreamReader(webAppDefinition));
    Properties vars = new Properties(System.getProperties());
    vars.setProperty("webapp-dir", webAppDefinition.getParentFile().getAbsolutePath().replace('\\', '/'));
    vars.setProperty("webapp-name", this.name);
    vars.setProperty("webapp-path", this.getPath());
    String resolvedDefXml = XSLWebUtils.resolveProperties(defXml, vars);
    InputSource src = new InputSource(new StringReader(resolvedDefXml));
    src.setSystemId(webAppDefinition.getAbsolutePath());
    Document webAppDoc = db.parse(src);
        
    XPath xpath = new XPathFactoryImpl().newXPath();
    DualHashBidiMap<String, String> map = new DualHashBidiMap<String, String>();
    map.put("webapp", Definitions.NAMESPACEURI_XSLWEB_WEBAPP);
    map.put("saxon-config", NamespaceConstant.SAXON_CONFIGURATION);
    map.put("ehcache", Definitions.NAMESPACEURI_EHCACHE);
    xpath.setNamespaceContext(XMLUtils.getNamespaceContext(map));    
    Node docElem = webAppDoc.getDocumentElement();
    
    Node saxonConfigNode = (Node) xpath.evaluate("saxon-config:configuration", docElem, XPathConstants.NODE);
    this.configuration = new XSLWebConfiguration(this, saxonConfigNode, webAppDefinition.getAbsolutePath());
    this.processor = new Processor(this.configuration.getConfiguration());
    this.fingerprints = new Fingerprints(this.configuration.getConfiguration().getNamePool());
    
    this.title = (String) xpath.evaluate("webapp:title", docElem, XPathConstants.STRING);
    this.description = (String) xpath.evaluate("webapp:description", docElem, XPathConstants.STRING);
    String devModeValue = (String) xpath.evaluate("webapp:development-mode", docElem, XPathConstants.STRING);
    this.developmentMode = XMLUtils.getBooleanValue(devModeValue, false); 
    String debModeValue = (String) xpath.evaluate("webapp:debug-mode", docElem, XPathConstants.STRING);
    this.debugMode = XMLUtils.getBooleanValue(debModeValue, false);
    
    if (context.getDebugEnable() && debugMode) {
      Configuration config = this.configuration.getConfiguration();
      config.setConfigurationProperty(Feature.STYLE_PARSER_CLASS, "nl.armatiek.xslweb.saxon.debug.DebugXMLReader");
      config.setBooleanProperty(Feature.LINE_NUMBERING, true);
      config.setBooleanProperty(Feature.ALLOW_MULTITHREADING, false);
      config.setBooleanProperty(Feature.COMPILE_WITH_TRACING, true);
      // config.setCompileWithTracing(true);
      if (config.getDefaultXsltCompilerInfo() != null) {
        config.getDefaultXsltCompilerInfo().setCodeInjector(new DebugTraceCodeInjector());
      }
      if (config.getDefaultStaticQueryContext() != null) {
        config.getDefaultStaticQueryContext().setCodeInjector(new TraceCodeInjector());
      }  
    }

    String maxUploadSizeValue = (String) xpath.evaluate("webapp:max-upload-size", docElem, XPathConstants.STRING);
    this.maxUploadSize = XMLUtils.getIntegerValue(maxUploadSizeValue, 10);    
    String waitForJobsAtCloseValue = (String) xpath.evaluate("webapp:wait-for-jobs-at-close", docElem, XPathConstants.STRING);
    this.waitForJobsAtClose = XMLUtils.getBooleanValue(waitForJobsAtCloseValue, true);
    this.cacheBusterId = (String) xpath.evaluate("webapp:resources/webapp:cache-buster-id", docElem, XPathConstants.STRING); 
    
    NodeList resourceNodes = (NodeList) xpath.evaluate("webapp:resources/webapp:resource", docElem, XPathConstants.NODESET);
    for (int i=0; i<resourceNodes.getLength(); i++) {
      resources.add(new Resource((Element) resourceNodes.item(i)));
    }
    NodeList paramNodes = (NodeList) xpath.evaluate("webapp:parameters/webapp:parameter", docElem, XPathConstants.NODESET);
    for (int i=0; i<paramNodes.getLength(); i++) {
      parameters.add(new Parameter(processor, (Element) paramNodes.item(i)));
    }
    NodeList jobNodes = (NodeList) xpath.evaluate("webapp:jobs/webapp:job", docElem, XPathConstants.NODESET);
    if (jobNodes.getLength() > 0) {
      File quartzFile = new File(homeDir, Definitions.FILENAME_QUARTZ); 
      quartzFile = (quartzFile.isFile()) ? quartzFile : new File(context.getHomeDir(), "config" + File.separatorChar + Definitions.FILENAME_QUARTZ);
      SchedulerFactory sf;
      if (quartzFile.isFile()) {
        logger.info(String.format("Initializing Quartz scheduler using properties file \"%s\" ...", quartzFile.getAbsolutePath()));        
        Properties props = XSLWebUtils.readProperties(quartzFile);
        props.setProperty(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, name);        
        sf = new StdSchedulerFactory(props);        
      } else {
        logger.info("Initializing Quartz scheduler ...");
        sf = new StdSchedulerFactory();        
      }
      scheduler = sf.getScheduler(); 
      for (int i=0; i<jobNodes.getLength(); i++) {
        Element jobElem = (Element) jobNodes.item(i);      
        String jobName = XMLUtils.getValueOfChildElementByLocalName(jobElem, "name");
        String jobUri = XMLUtils.getValueOfChildElementByLocalName(jobElem, "uri");
        String jobCron = XMLUtils.getValueOfChildElementByLocalName(jobElem, "cron");
        boolean concurrent = XMLUtils.getBooleanValue(XMLUtils.getValueOfChildElementByLocalName(jobElem, "concurrent"), true);
        String jobId = "job_" + name + "_" + jobName;
        JobDetail job = newJob(concurrent ? XSLWebJob.class : NonConcurrentExecutionXSLWebJob.class)
            .withIdentity(jobId, name)
            .usingJobData("webapp-path", getPath())
            .usingJobData("uri", jobUri)            
            .build();            
        Trigger trigger = newTrigger()
            .withIdentity("trigger_" + name + "_" + jobName, name)
            .withSchedule(cronSchedule(jobCron))
            .forJob(jobId, name)                     
            .build(); 
        logger.info(String.format("Scheduling job \"%s\" of webapp \"%s\" ...", jobName, name));
        scheduler.scheduleJob(job, trigger);
      }
    }    
    
    NodeList dataSourceNodes = (NodeList) xpath.evaluate("webapp:datasources/webapp:datasource", docElem, XPathConstants.NODESET);
    for (int i=0; i<dataSourceNodes.getLength(); i++) {
      DataSource dataSource = new DataSource((Element) dataSourceNodes.item(i));
      dataSources.put(dataSource.getName(), dataSource);
    }
    
    NodeList fopConfigNodes = (NodeList) xpath.evaluate("webapp:fop-configs/webapp:fop-config", docElem, XPathConstants.NODESET);
    for (int i=0; i<fopConfigNodes.getLength(); i++) {
      Element fopConfig = (Element) fopConfigNodes.item(i);     
      Element fopElement = XMLUtils.getFirstChildElement(fopConfig);
      fopConfigs.put(fopConfig.getAttribute("name"), XMLUtils.nodeToString(fopElement));
    }
    
    NodeList queueNodes = (NodeList) xpath.evaluate("webapp:queues/webapp:queue", docElem, XPathConstants.NODESET);
    for (int i=0; i<queueNodes.getLength(); i++) {
      Queue queue = new Queue((Element) queueNodes.item(i));
      queues.put(queue.getName(), queue);
    }
    
    StreamSource source = new StreamSource(getClass().getClassLoader().getResourceAsStream("identity.xsl"));
    XsltCompiler comp = processor.newXsltCompiler();
    this.identityXsltExecutable = comp.compile(source); 
   
    String shiroIniStr = (String) xpath.evaluate("webapp:security/webapp:shiro-ini", docElem, XPathConstants.STRING);
    if (StringUtils.isNoneBlank(shiroIniStr)) {
      Ini shiroIni = new Ini();
      shiroIni.load(shiroIniStr);
      shiroWebEnvironment = new IniWebEnvironment();
      ((IniWebEnvironment) shiroWebEnvironment).setIni(shiroIni);
      ((IniWebEnvironment) shiroWebEnvironment).init();
    }
    
    boolean webdavEnabled = XMLUtils.getBooleanValue((String) xpath.evaluate("webapp:webdav/webapp:enabled", docElem, XPathConstants.STRING), false);
    if (webdavEnabled) {
      webDavServletBean = new WebDavServletBean();
      webDavServletBean.init(
          new LocalFileSystemStore(this.homeDir, this.getPath() + "/webdav"), 
          StringUtils.trimToNull((String) xpath.evaluate("webapp:webdav/webapp:index-file", docElem, XPathConstants.STRING)), 
          StringUtils.trimToNull((String) xpath.evaluate("webapp:webdav/webapp:instead-of-404", docElem, XPathConstants.STRING)), 
          Integer.parseInt(StringUtils.defaultString(StringUtils.trimToNull((String) xpath.evaluate("webapp:webdav/no-contentlength-header", docElem, XPathConstants.STRING)), "0")), 
          XMLUtils.getBooleanValue((String) xpath.evaluate("webapp:webdav/webapp:lazy-folder-creation-on-put", docElem, XPathConstants.STRING), false));
    }
    
    Node ehCacheConfigNode = (Node) xpath.evaluate("ehcache:config", docElem, XPathConstants.NODE);
    
    if (ehCacheConfigNode != null) {
      String ehCacheConfigXML = XMLUtils.nodeToString(ehCacheConfigNode);
      Document ehCacheConfigDoc = XMLUtils.stringToDocument(ehCacheConfigXML);
      XmlConfiguration xmlConfig = new XmlConfiguration(ehCacheConfigDoc);
      cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig); 
    } else {
      cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
    }
    
    this.cacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
      String.class, ArrayList.class, ResourcePoolsBuilder.heap(32))
        .withExpiry(new DefaultExpiryPolicy());
    
    // initClassLoader();
            
    initFileAlterationObservers();    
  }
  
  public void open() throws Exception {
    logger.info(String.format("Opening webapp \"%s\" ...", name));
    
    logger.info("Starting file alteration monitor ...");
    monitor.start();
    
    if (scheduler != null) {
      logger.info("Starting Quartz scheduler ...");    
      scheduler.start();    
    }
    
    if (cacheManager != null) {
      logger.info("Initializing cache manager ...");
      cacheManager.init();
    }
    
    logger.info("Executing handler for webapp-open event ...");
    executeEvent(Definitions.EVENTNAME_WEBAPPOPEN);
    
    isClosed = false;
    
    logger.info(String.format("Webapp \"%s\" opened.", name));    
  }
  
  public void close() throws Exception {        
    if (isClosed) {
      return;
    }
    
    isClosed = true;
    
    logger.info(String.format("Closing webapp \"%s\" ...", name));
    
    logger.info("Stopping file alteration monitor ...");
    if (monitor != null)
      monitor.stop();
    
    if (scheduler != null) {
      logger.info("Shutting down Quartz scheduler ...");
      if (waitForJobsAtClose) {
        /* Unschedule all jobs: */
        for (String triggerName : scheduler.getTriggerGroupNames()) {
          for (TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(triggerName))) {
            scheduler.unscheduleJob(triggerKey);
          }
        }
        if (jobRequestCount > 0) {
          logger.info("Wait for running jobs to finish ...");
          while (jobRequestCount > 0) {
            Thread.sleep(250);
          }
          logger.info("All jobs finished");
        }
      }
      scheduler.shutdown(false);
      logger.info("Shutdown of Quartz scheduler complete.");
    }
    
    if (cacheManager != null) {
      logger.info("Closing cache manager ...");
      cacheManager.close();
    }
    
    logger.info("Executing handler for webapp-close event ...");
    executeEvent(Definitions.EVENTNAME_WEBAPPCLOSE);
    
    logger.info("Closing XPath extension functions ...");
    if (configuration != null) {
      Iterator<ExtensionFunctionDefinition> functions = configuration.getRegisteredExtensionFunctions();
      while (functions.hasNext()) {
        ExtensionFunctionDefinition function = functions.next();
        if (function instanceof Closeable) {
          ((Closeable) function).close();
        }
      }
    }
    
    if (!dataSourceCache.isEmpty()) {
      logger.info("Closing Datasources ...");
      for (ComboPooledDataSource cpds : dataSourceCache.values()) {
        cpds.close();
      }
    }
    
    logger.info("Stopping queueing services ...");
    for (ExecutorService service : executorServiceCache.values()) {
      service.shutdownNow();
    }
    
    logger.info("Clearing compiled XSLT stylesheet cache ...");
    xsltExecutableCache.clear();
    
    logger.info("Clearing compiled STX stylesheet cache ...");
    templatesCache.clear();
    
    logger.info("Clearing compiled XQuery cache ...");
    xqueryExecutableCache.clear();
    
    logger.info("Clearing stylesheet export file cache ...");
    stylesheetExportFileCache.clear();
    
    logger.info("Clearing compiled XML Schema cache ...");
    schemaCache.clear();
    
    logger.info(String.format("Webapp \"%s\" closed.", name));
  }
  
  public void executeEvent(final QName templateName) {
    try {
      File eventsXslPath = new File(getHomeDir(), "xsl/events.xsl");
      if (!eventsXslPath.isFile()) {
        return;
      }
      TransformationErrorListener errorListener = new TransformationErrorListener(null, developmentMode);  
      XsltExecutable templates = getXsltExecutable("events.xsl", errorListener);
      Xslt30Transformer transformer = templates.load30();
      SaxonUtils.setMessageEmitter(transformer.getUnderlyingController(), getConfiguration(), errorListener);
      transformer.setErrorListener(errorListener);
      Map<QName, XdmValue> params = XSLWebUtils.getStylesheetParameters(this, null, null, getHomeDir());
      transformer.setStylesheetParameters(params);
      try {
        transformer.applyTemplates(new StreamSource(new StringReader('<' + templateName.toString() + " xmlns:event=\"" + Definitions.NAMESPACEURI_XSLWEB_EVENT + "\"/>")));
      } catch (SaxonApiException e) {
        // thrown when template does not exist. Any other errors are reported via ErrorListener?
      }
    } catch (Exception e) {
      logger.error(String.format("Error executing event \"%s\" of webapp \"%s\"", templateName.getLocalName(), name), e);
    }
  }
  
  private void onFileChanged(File file, String message) {
    if (!file.isFile()) {
      return;
    }
    logger.info(String.format(message, file.getAbsolutePath()));
    Context.getInstance().reloadWebApp(this.definition, true);
  }
    
  private void initFileAlterationObservers() { 
    monitor = new FileAlterationMonitor(3000);    
    
    if (!developmentMode) {
      IOFileFilter xslFiles = FileFilterUtils.and(FileFilterUtils.fileFileFilter(), new SuffixFileFilter(new String[] {".xsl", ".xslt"}, IOCase.INSENSITIVE));
      FileAlterationObserver xslObserver = new FileAlterationObserver(new File(homeDir, "xsl"), FileFilterUtils.or(FileFilterUtils.directoryFileFilter(), xslFiles));        
      xslObserver.addListener(new FileAlterationListenerAdaptor() {
  
        @Override
        public void onFileChange(File file) {        
          onFileChanged(file, "Change in XSL stylesheet \"%s\" detected. Reloading webapp ...");
        }
  
        @Override
        public void onFileDelete(File file) {
          onFileChanged(file, "Deletion of XSL stylesheet \"%s\" detected. Reloading webapp ...");
        }
        
      });
      monitor.addObserver(xslObserver);
    }
    
    IOFileFilter jarAndClassFiles = FileFilterUtils.and(FileFilterUtils.fileFileFilter(), new SuffixFileFilter(new String[] {".jar", ".class"}, IOCase.INSENSITIVE));       
    FileAlterationObserver classObserver = new FileAlterationObserver(new File(homeDir, "lib"), FileFilterUtils.or(FileFilterUtils.directoryFileFilter(), jarAndClassFiles));
    classObserver.addListener(new FileAlterationListenerAdaptor() {

      @Override
      public void onFileCreate(File file) {        
        onFileChanged(file, "New plugin jar or class file \"%s\" detected. Reloading webapp ...");
      }

      @Override
      public void onFileChange(File file) {
        onFileChanged(file, "Change in plugin jar or class file \"%s\" detected. Reloading webapp ...");
      }

      @Override
      public void onFileDelete(File file) {
        onFileChanged(file, "Deletion of plugin jar or class file \"%s\" detected. Reloading webapp ...");
      }
      
    });
    monitor.addObserver(classObserver);
  }
  
  /*
  private void initClassLoader() {
    logger.debug("Initializing webapp specific classloader ...");
    
    File libDir = new File(getHomeDir(), "lib");
    if (!libDir.isDirectory()) {
      return;
    }
    this.classPath = new ArrayList<File>();                
    classPath.addAll(FileUtils.listFiles(libDir, new WildcardFileFilter("*.jar"), DirectoryFileFilter.DIRECTORY));
    if (classPath.isEmpty() && !XSLWebUtils.hasSubDirectories(libDir)) {
      return;
    }
    classPath.add(libDir);
    Collection<File> saxonJars = FileUtils.listFiles(new File(Context.getInstance().getWebInfDir(), "lib"), 
        new WildcardFileFilter("*saxon*.jar", IOCase.INSENSITIVE), FalseFileFilter.INSTANCE);    
    classPath.addAll(saxonJars);
    
    ClassLoaderBuilder builder = new ClassLoaderBuilder();    
    builder.add(classPath);
    
    this.classLoader = builder.createClassLoader();
  }
  */
  
  public File getHomeDir() {
    return homeDir;
  }
  
  public String getName() {
    return name;
  }
  
  public boolean isClosed() {
    return isClosed;
  }
  
  public String getPath() {
    return (name.equals("ROOT")) ? "" : "/" + name;
  }
  
  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }
  
  public boolean getDevelopmentMode() {
    return developmentMode;
  }
  
  public boolean getDebugMode() {
    return debugMode;
  }
  
  public int getMaxUploadSize() {
    return maxUploadSize;
  }
  
  public String getCacheBusterId() {
    return cacheBusterId;
  }

  public List<Resource> getResources() {
    return resources;
  }

  public List<Parameter> getParameters() {
    return parameters;
  }
  
  public Map<String, DataSource> getDataSources() {
    return dataSources;
  }
  
  public WebDavServletBean getWebDavServletBean() {
    return webDavServletBean;
  }
  
  public WebEnvironment getShiroWebEnvironment() {
    return shiroWebEnvironment;
  }
  
  public Configuration getConfiguration() {
    return configuration.getConfiguration();
  }
  
  public Fingerprints getFingerprints() {
    return fingerprints;
  }
  
  public Processor getProcessor() {
    return processor;
  }
  
  public XsltExecutable getRequestDispatcherTemplates(ErrorListener errorListener) throws Exception {
    return tryXsltExecutableCache(new File(getHomeDir(), 
        Definitions.PATHNAME_REQUESTDISPATCHER_XSL).getAbsolutePath(), errorListener);
  }
  
  public XsltExecutable getXsltExecutable(String path, ErrorListener errorListener) throws Exception {    
    if (new File(path).isAbsolute()) {
      return tryXsltExecutableCache(path, errorListener);
    }    
    return tryXsltExecutableCache(new File(getHomeDir(), "xsl" + "/" + path).getAbsolutePath(), errorListener);
  }
  
  public XsltExecutable getIdentityXsltExecutable() {
    return this.identityXsltExecutable;
  }
  
  public Templates getTemplates(String path, ErrorListener errorListener) throws Exception {    
    if (new File(path).isAbsolute()) {
      return tryTemplatesCache(path, errorListener);
    }    
    return tryTemplatesCache(new File(getHomeDir(), "stx" + "/" + path).getAbsolutePath(), errorListener);
  }
  
  public XQueryExecutable getQuery(String path, ErrorListener errorListener) throws Exception {    
    if (new File(path).isAbsolute()) {
      return tryQueryCache(path, errorListener);
    }    
    return tryQueryCache(new File(getHomeDir(), "xquery" + "/" + path).getAbsolutePath(), errorListener);
  }
  
  public Schema getSchema(Collection<String> schemaPaths, ErrorListener errorListener) throws Exception {    
    ArrayList<String> resolvedPaths = new ArrayList<String>();
    for (String path : schemaPaths) {
      if (new File(path).isAbsolute()) {
        resolvedPaths.add(path);
      } else {
        resolvedPaths.add(new File(getHomeDir(), "xsd" + "/" + path).getAbsolutePath());
      }
    }
    return trySchemaCache(resolvedPaths, errorListener);
  }
  
  public XsltExecutable getSchematron(String path, String phase, ErrorListener errorListener) throws Exception {    
    if (new File(path).isAbsolute()) {
      return trySchematronCache(path, phase, errorListener);
    }    
    return trySchematronCache(new File(getHomeDir(), "sch" + "/" + path).getAbsolutePath(), phase, errorListener);
  }
  
  public byte[] getStylesheetExportFile(String xslPath, ErrorListener errorListener) throws Exception {
    if (new File(xslPath).isAbsolute()) {
      return tryStylesheetExportFile(xslPath, errorListener);
    } 
    return tryStylesheetExportFile(new File(getHomeDir(), "xsl" + "/" + xslPath).getAbsolutePath(), errorListener);
  }
  
  public File getStaticFile(String path) {    
    String relPath = (name.equals("ROOT")) ? path.substring(1) : StringUtils.substringAfter(path, this.name + "/");
    return new File(this.homeDir, "static" + "/" + relPath);    
  }
  
  public String getRelativePath(String path) {    
    return (name.equals("ROOT")) ? path : StringUtils.substringAfter(path, "/" + name);        
  }

  public Resource matchesResource(String path) {    
    for (Resource resource : this.resources) {      
      if (resource.getPattern().matcher(path).matches()) {
        return resource;
      }     
    }
    return null;
  }
  
  public XsltExecutable tryXsltExecutableCache(String transformationPath, ErrorListener errorListener, boolean cache) throws Exception {
    String key = FilenameUtils.normalize(transformationPath);
    XsltExecutable xsltExecutable = xsltExecutableCache.get(key);    
    if (xsltExecutable == null) {
      logger.info("Compiling and caching XSLT stylesheet \"" + transformationPath + "\" ...");                 
      try {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setXIncludeAware(true);
        spf.setValidating(false);
        SAXParser parser = spf.newSAXParser();
        XMLReader reader = parser.getXMLReader(); 
        /*
        if (Context.getInstance().getDebugEnable() && debugMode) {
          DebugXMLFilter debugXMLFilter = new DebugXMLFilter();
          debugXMLFilter.setParent(reader);
          reader = debugXMLFilter;
        }
        */
        Source source;
        if (transformationPath.startsWith("classpath:")) {
          source = new StreamSource(getClass().getClassLoader().getResourceAsStream(StringUtils.substringAfter(transformationPath, ":")));
        } else {
          source = new SAXSource(reader, new InputSource(transformationPath));
        }      
        XsltCompiler comp = processor.newXsltCompiler();
        if (errorListener != null) {
          comp.setErrorListener(errorListener);
        }
        xsltExecutable = comp.compile(source);        
      } catch (Exception e) {
        logger.error("Could not compile XSLT stylesheet \"" + transformationPath + "\"", e);
        throw e;
      }      
      if (!developmentMode || cache) {
        xsltExecutableCache.put(key, xsltExecutable);
      }      
    }
    return xsltExecutable;
  }
  
  public XsltExecutable tryXsltExecutableCache(String transformationPath, ErrorListener errorListener) throws Exception {
    return tryXsltExecutableCache(transformationPath, errorListener, false);
  }
  
  public Templates tryTemplatesCache(String transformationPath,  
      ErrorListener errorListener, boolean cache) throws Exception {
    String key = FilenameUtils.normalize(transformationPath);
    Templates templates = templatesCache.get(key);    
    if (templates == null) {
      logger.info("Compiling and caching STX stylesheet \"" + transformationPath + "\" ...");                 
      try {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setXIncludeAware(true);
        spf.setValidating(false);
        SAXParser parser = spf.newSAXParser();
        XMLReader reader = parser.getXMLReader(); 
        Source source;
        if (transformationPath.startsWith("classpath:")) {
          source = new StreamSource(getClass().getClassLoader().getResourceAsStream(StringUtils.substringAfter(transformationPath, ":")));
        } else {
          source = new SAXSource(reader, new InputSource(transformationPath));
        }
        net.sf.joost.trax.TransformerFactoryImpl tfi = new net.sf.joost.trax.TransformerFactoryImpl();
        tfi.setAttribute(TrAXConstants.MESSAGE_EMITTER_CLASS, new MessageEmitter());
        if (errorListener != null) {
          tfi.setErrorListener(errorListener);
        };
        templates = tfi.newTemplates(source);
      } catch (Exception e) {
        logger.error("Could not compile STX stylesheet \"" + transformationPath + "\"", e);
        throw e;
      }      
      if (!developmentMode || cache) {
        templatesCache.put(key, templates);
      }      
    }
    return templates;
  }
  
  public Templates tryTemplatesCache(String transformationPath, ErrorListener errorListener) throws Exception {
    return tryTemplatesCache(transformationPath, errorListener, false);
  }
  
  public XQueryExecutable tryQueryCache(String xqueryPath, ErrorListener errorListener) throws Exception {
    String key = FilenameUtils.normalize(xqueryPath);
    XQueryExecutable xquery = xqueryExecutableCache.get(key);    
    if (xquery == null) {
      logger.info("Compiling and caching xquery \"" + xqueryPath + "\" ...");                 
      try {
        XQueryCompiler comp = processor.newXQueryCompiler();
        comp.setErrorListener(errorListener);
        xquery = comp.compile(new File(xqueryPath));     
      } catch (Exception e) {
        logger.error("Could not compile XQuery \"" + xqueryPath + "\"", e);
        throw e;
      }      
      if (!developmentMode) {
        xqueryExecutableCache.put(key, xquery);
      }      
    }
    return xquery;
  }
  
  public Schema trySchemaCache(Collection<String> schemaPaths,  
      ErrorListener errorListener) throws Exception {
    String key = StringUtils.join(schemaPaths, ";");
    Schema schema = schemaCache.get(key);    
    if (schema == null) {
      logger.info("Compiling and caching schema(s) \"" + key + "\" ...");                 
      try {
        ArrayList<Source> schemaSources = new ArrayList<Source>();
        for (String path: schemaPaths) {
          File file = new File(path);
          if (!file.isFile()) {
            throw new FileNotFoundException("XML Schema file \"" + file.getAbsolutePath() + "\" not found");
          }
          schemaSources.add(new StreamSource(file));
        }
        SchemaFactory schemaFactory = XMLUtils.getNonSaxonJAXPSchemaFactory();       
        schemaFactory.setErrorHandler(new ValidatorErrorHandler("Schema file(s)"));
        schema = schemaFactory.newSchema(schemaSources.toArray(new Source[schemaSources.size()]));
      } catch (Exception e) {
        logger.error("Error compiling schema(s) \"" + key + "\"", e);
        throw e;
      }      
      if (!developmentMode) {
        schemaCache.put(key, schema);
      }      
    }
    return schema;
  }
    
  public XsltExecutable trySchematronCache(String schematronPath, String phase, 
      ErrorListener errorListener) throws Exception {
    String key = FilenameUtils.normalize(schematronPath) + (phase != null ? phase : "");
    XsltExecutable templates = xsltExecutableCache.get(key);    
    if (templates == null) {
      logger.info("Compiling and caching schematron schema \"" + schematronPath + "\" ...");                 
      try {
        XdmNode source1 = processor.newDocumentBuilder().build(new File(schematronPath));
        
        File schematronDir = new File(Context.getInstance().getHomeDir(), "common/xsl/system/schematron");
        
        ErrorListener listener = new TransformationErrorListener(null, developmentMode); 
        
        Xslt30Transformer stage1 = tryXsltExecutableCache(new File(schematronDir, "iso_dsdl_include.xsl").getAbsolutePath(), errorListener).load30();
        stage1.setErrorListener(listener);
        SaxonUtils.setMessageEmitter(stage1.getUnderlyingController(), getConfiguration(), errorListener);
        Xslt30Transformer stage2 = tryXsltExecutableCache(new File(schematronDir, "iso_abstract_expand.xsl").getAbsolutePath(), errorListener).load30();
        stage2.setErrorListener(listener);
        SaxonUtils.setMessageEmitter(stage2.getUnderlyingController(), getConfiguration(), errorListener);
        Xslt30Transformer stage3 = tryXsltExecutableCache(new File(schematronDir, "iso_svrl_for_xslt2.xsl").getAbsolutePath(), errorListener).load30();
        stage3.setErrorListener(listener);
        SaxonUtils.setMessageEmitter(stage3.getUnderlyingController(), getConfiguration(), errorListener);
       
        XdmDestination destStage1 = new XdmDestination();
        XdmDestination destStage2 = new XdmDestination();
        XdmDestination destStage3 = new XdmDestination();

        stage1.setGlobalContextItem(source1);
        stage1.applyTemplates(source1, destStage1);
        XdmNode source2 = destStage1.getXdmNode();
        stage2.setGlobalContextItem(source2);
        stage2.applyTemplates(source2, destStage2);
        XdmNode source3 = destStage2.getXdmNode();
        stage3.setGlobalContextItem(source3);
        stage3.applyTemplates(source3, destStage3);
        
        Source generatedXsltSource = destStage3.getXdmNode().asSource();
        
        if (this.developmentMode) {
          TransformerFactory factory = new TransformerFactoryImpl();
          Transformer transformer = factory.newTransformer();
          Properties props = new Properties();
          props.put(OutputKeys.INDENT, "yes");
          transformer.setOutputProperties(props);
          StringWriter sw = new StringWriter();
          transformer.transform(generatedXsltSource, new StreamResult(sw));
          logger.info("Generated Schematron XSLT for \"" + schematronPath + "\", phase \"" + (phase != null ? phase : "") + "\" [" + sw.toString() + "]");
        }
        
        
        XsltCompiler comp = processor.newXsltCompiler();
        comp.setErrorListener(errorListener);
        templates = comp.compile(generatedXsltSource);
        
      } catch (Exception e) {
        logger.error("Could not compile schematron schema \"" + schematronPath + "\"", e);
        throw e;
      }      
      if (!developmentMode) {
        xsltExecutableCache.put(key, templates);
      }      
    }
    return templates;
  }
  
  public byte[] tryStylesheetExportFile(String xslPath, ErrorListener errorListener) throws Exception {
    String key = FilenameUtils.normalize(xslPath);
    byte[] sef = stylesheetExportFileCache.get(key);    
    if (sef == null) {
      logger.info("Generating and caching stylesheet export file for \"" + xslPath + "\" ...");                 
      try {
        XsltCompiler comp = processor.newXsltCompiler();
        comp.setTargetEdition("JS");
        comp.setErrorListener(errorListener);
        comp.setJustInTimeCompilation(false);
        comp.setDefaultElementNamespace(Definitions.NAMESPACEURI_XHTML);
        comp.setUnprefixedElementMatchingPolicy(UnprefixedElementMatchingPolicy.DEFAULT_NAMESPACE_OR_NONE);
        XsltExecutable exec = comp.compile(new StreamSource(xslPath));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exec.export(baos, "JS");
        sef = baos.toByteArray();
      } catch (Exception e) {
        logger.error("Could not generate stylesheet export file for \"" + xslPath + "\"", e);
        throw e;
      }      
      if (!developmentMode) {
        stylesheetExportFileCache.put(key, sef);
      }      
    }
    return sef;
  }
  
  public Map<String, ArrayList<Attribute>> getAttributes() {
    return this.attributes;
  }
  
  public void setAttributes(Map<String, ArrayList<Attribute>> attributes) {
    this.attributes = attributes;
  }
  
  public ArrayList<Attribute> getAttribute(String name) {
    return attributes.get(name);
  }
  
  public void removeAttribute(String name) {
    attributes.remove(name);
  }
  
  public void setAttribute(String name, ArrayList<Attribute> attrs) {
    if (attrs != null) {
      attributes.put(name, attrs);
    } else {
      attributes.remove(name);
    }
  }
  
  private Cache<String, ArrayList> getCache(String cacheName) throws XSLWebException {
    if (cacheManager == null) {
      throw new XSLWebException("Caching is not configured in webapp.xml");
    }
    Cache<String, ArrayList> cache = cacheManager.getCache(cacheName, String.class, ArrayList.class);
    if (cache == null) {
      cache = cacheManager.createCache(cacheName, cacheConfig);
    }
    return cache;
  }
  
  @SuppressWarnings("unchecked")
  public ArrayList<Attribute> getCacheValue(String cacheName, String keyName) throws XSLWebException {
    return getCache(cacheName).get(keyName);
  }
  
  public void setCacheValue(String cacheName, String keyName, ArrayList<Attribute> attrs, 
      int tti, int ttl) throws XSLWebException {
    try {
      if (ttl > -1)
        DefaultExpiryPolicy.setTTL(ttl);
      if (tti > -1)
        DefaultExpiryPolicy.setTTI(tti);
      getCache(cacheName).put(keyName, attrs);
    } finally {
      DefaultExpiryPolicy.setTTL(-1);
      DefaultExpiryPolicy.setTTI(-1);
    }
  }
  
  public void removeCacheValue(String cacheName, String keyName) {
    getCache(cacheName).remove(keyName);
  }
  
  @Override
  public void error(SAXParseException e) throws SAXException {
    logger.error(String.format("Error parsing \"%s\"", definition.getAbsolutePath()), e); 
    throw e;
  }

  @Override
  public void fatalError(SAXParseException e) throws SAXException {
    logger.error(String.format("Error parsing \"%s\"", definition.getAbsolutePath()), e); 
    throw e;
  }

  @Override
  public void warning(SAXParseException e) throws SAXException {
    logger.warn(String.format("Error parsing \"%s\"", definition.getAbsolutePath()), e);     
  }
  
  public ComboPooledDataSource getDataSource(String name) throws Exception {
    ComboPooledDataSource cpds = dataSourceCache.get(name);    
    if (cpds == null) {      
      DataSource dataSource = dataSources.get(name);
      if (dataSource == null) {
        throw new XSLWebException("Datasource definition \"" + name + "\" not configured in webapp.xml");        
      }
      
      Class.forName(dataSource.getDriverClass());
      
      cpds = new ComboPooledDataSource();
      cpds.setDriverClass(dataSource.getDriverClass());
      cpds.setJdbcUrl(dataSource.getJdbcUrl());
      if (dataSource.getUsername() != null) {
        cpds.setUser(dataSource.getUsername());
      }
      if (dataSource.getPassword() != null) {
        cpds.setUser(dataSource.getPassword());
      }
      if (dataSource.getProperties() != null) {
        cpds.setProperties(dataSource.getProperties());
      }
      dataSourceCache.put(name, cpds);
    }
    return cpds;
  }
  
  public ScriptEngine getScriptEngine(String instanceName, String engineName, Map<String, Object> extraBindings) {
    ScriptEngine engine = scriptEngineCache.get(instanceName);
    if (engine == null) {
      System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
      engine = new ScriptEngineManager().getEngineByName(engineName);
      if (engine.getClass().getName().endsWith("GraalJSScriptEngine")) {
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowHostAccess", true);
        bindings.put("polyglot.js.allowHostClassLookup", (Predicate<String>) s -> true);
        if (extraBindings != null) {
          bindings.putAll(extraBindings);
        }
      }
      scriptEngineCache.put(instanceName, engine);
    }
    return engine;
  }
  
  public FopFactory getFopFactory(String configName) throws Exception {
    FopFactory fopFactory = fopFactoryCache.get(configName);
    if (fopFactory == null) {
      String fopConfig = fopConfigs.get(configName);
      if (fopConfig == null) {
        throw new XSLWebException("FOP Configuration \"" + configName + "\" not configured in webapp.xml");        
      }      
      fopFactory = FopFactory.newInstance(getHomeDir().toURI(), IOUtils.toInputStream(fopConfig, "UTF-8"));      
      fopFactoryCache.put(configName, fopFactory);
    }
    return fopFactory;
  }
  
  public ExecutorService getExecutorService(String queueName) {
    ExecutorService service = executorServiceCache.get(queueName);
    if (service == null) {
      Queue queue = queues.get(queueName);
      if (queue == null) {
        throw new XSLWebException("Queue \"" + queueName + "\" not configured in webapp.xml");        
      }
      int numberOfThreads = queue.getNumberOfThreads();
      int maxQueueSize = queue.getMaxQueueSize();
      final BlockingQueue<Runnable> bq = new ArrayBlockingQueue<>(maxQueueSize);
      service = new ThreadPoolExecutor(numberOfThreads, numberOfThreads, 0L, TimeUnit.MILLISECONDS, bq);
      executorServiceCache.put(queueName, service);
    }
    return service;
  }
  
  public synchronized void incJobRequestCount() {
    jobRequestCount++;
    logger.debug("Increment jobRequestCount " + jobRequestCount);
  }
  
  public synchronized void decJobRequestCount() {
    jobRequestCount--;
    logger.debug("Decrement jobRequestCount " + jobRequestCount);
  }
  
  public void registerExtensionFunctionDefinition(String name, ExtensionFunctionDefinition funcDef) {
    configuration.registerExtensionFunction(funcDef);
    extensionFunctionDefinitions.put(name, funcDef);
  }
  
  public ExtensionFunctionDefinition getExtensionFunctionDefinition(String name) {
    return extensionFunctionDefinitions.get(name);
  }
  
  public void registerExtensionFunction(String name, ExtensionFunction func) {
    processor.registerExtensionFunction(func);
    extensionFunctions.put(name, func);
  }
  
  public ExtensionFunction getExtensionFunction(String name) {
    return extensionFunctions.get(name);
  }
  
}