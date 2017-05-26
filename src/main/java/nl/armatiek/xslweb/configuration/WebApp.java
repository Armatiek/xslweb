package nl.armatiek.xslweb.configuration;

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

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.ProxySelector;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLContext;
import javax.xml.XMLConstants;
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
import org.apache.http.HttpHost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import net.sf.joost.trax.TrAXConstants;
import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.serialize.MessageWarner;
import net.sf.saxon.xpath.XPathFactoryImpl;
import nl.armatiek.xmlindex.XMLIndex;
import nl.armatiek.xslweb.error.XSLWebException;
import nl.armatiek.xslweb.joost.MessageEmitter;
import nl.armatiek.xslweb.quartz.NonConcurrentExecutionXSLWebJob;
import nl.armatiek.xslweb.quartz.XSLWebJob;
import nl.armatiek.xslweb.saxon.configuration.XSLWebConfiguration;
import nl.armatiek.xslweb.saxon.errrorlistener.TransformationErrorListener;
import nl.armatiek.xslweb.saxon.errrorlistener.ValidatorErrorHandler;
import nl.armatiek.xslweb.utils.XMLUtils;
import nl.armatiek.xslweb.utils.XSLWebUtils;

public class WebApp implements ErrorHandler {
  
  private static final Logger logger = LoggerFactory.getLogger(WebApp.class);
  
  private Map<String, XsltExecutable> xsltExecutableCache = new ConcurrentHashMap<String, XsltExecutable>();
  private Map<String, XQueryExecutable> xqueryExecutableCache = new ConcurrentHashMap<String, XQueryExecutable>();
  private Map<String, Templates> templatesCache =  new ConcurrentHashMap<String, Templates>();
  private Map<String, Schema> schemaCache = new ConcurrentHashMap<String, Schema>();
  private Map<String, Collection<Attribute>> attributes = new ConcurrentHashMap<String, Collection<Attribute>>();
  private Map<String, XMLIndex> indexCache = new ConcurrentHashMap<String, XMLIndex>();
  private Map<String, ComboPooledDataSource> dataSourceCache = new ConcurrentHashMap<String, ComboPooledDataSource>();
  private Map<String, FopFactory> fopFactoryCache = new ConcurrentHashMap<String, FopFactory>();
      
  private volatile boolean isClosed = true;
  private File definition;
  private File homeDir;  
  private String name;
  private String title;
  private String description;
  private boolean developmentMode;
  private boolean waitForJobsAtClose;
  private int maxUploadSize;
  private Scheduler scheduler;
  private List<Resource> resources = new ArrayList<Resource>();
  private List<Parameter> parameters = new ArrayList<Parameter>();
  private Map<String, Index> indexes = new HashMap<String, Index>();
  private Map<String, DataSource> dataSources = new HashMap<String, DataSource>();
  private Map<String, String> fopConfigs = new HashMap<String, String>();
  private XSLWebConfiguration configuration;  
  private Processor processor;  
  private FileAlterationMonitor monitor;
  private CloseableHttpClient httpClient;
  private volatile int jobRequestCount = 0;
  
  public WebApp(File webAppDefinition) throws Exception {   
    logger.info(String.format("Loading webapp definition \"%s\" ...", webAppDefinition.getAbsolutePath()));
    
    Context context = Context.getInstance();
    this.definition = webAppDefinition;
    this.homeDir = webAppDefinition.getParentFile();
    this.name = this.homeDir.getName();
    
    this.configuration = new XSLWebConfiguration(this);
    this.processor = new Processor(this.configuration.getConfiguration());
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);    
    dbf.setSchema(context.getWebAppSchema());    
    dbf.setXIncludeAware(true);
    DocumentBuilder db = dbf.newDocumentBuilder();
    db.setErrorHandler(this); 
    
    String defXml = IOUtils.toString(new XmlStreamReader(webAppDefinition));
    Properties vars = new Properties(System.getProperties());
    vars.setProperty("webapp-dir", webAppDefinition.getParentFile().getAbsolutePath().replace('\\', '/'));
    String resolvedDefXml = XSLWebUtils.resolveProperties(defXml, vars);
    // Document webAppDoc = db.parse(webAppDefinition);
    InputSource src = new InputSource(new StringReader(resolvedDefXml));
    src.setSystemId(webAppDefinition.getAbsolutePath());
    Document webAppDoc = db.parse(src);
    
    XPath xpath = new XPathFactoryImpl().newXPath();
    xpath.setNamespaceContext(XMLUtils.getNamespaceContext("webapp", Definitions.NAMESPACEURI_XSLWEB_WEBAPP));    
    Node docElem = webAppDoc.getDocumentElement();
    this.title = (String) xpath.evaluate("webapp:title", docElem, XPathConstants.STRING);
    this.description = (String) xpath.evaluate("webapp:description", docElem, XPathConstants.STRING);
    String devModeValue = (String) xpath.evaluate("webapp:development-mode", docElem, XPathConstants.STRING);
    this.developmentMode = XMLUtils.getBooleanValue(devModeValue, false); 
    String maxUploadSizeValue = (String) xpath.evaluate("webapp:max-upload-size", docElem, XPathConstants.STRING);
    this.maxUploadSize = XMLUtils.getIntegerValue(maxUploadSizeValue, 10);    
    String waitForJobsAtCloseValue = (String) xpath.evaluate("webapp:wait-for-jobs-at-close", docElem, XPathConstants.STRING);
    this.waitForJobsAtClose = XMLUtils.getBooleanValue(waitForJobsAtCloseValue, true);
    
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
    
    NodeList indexNodes = (NodeList) xpath.evaluate("webapp:indexes/webapp:index", docElem, XPathConstants.NODESET);
    for (int i=0; i<indexNodes.getLength(); i++) {
      Index index = new Index(getConfiguration(), xpath, (Element) indexNodes.item(i), homeDir);
      indexes.put(index.getName(), index);
    } 
    
    NodeList fopConfigNodes = (NodeList) xpath.evaluate("webapp:fop-configs/webapp:fop-config", docElem, XPathConstants.NODESET);
    for (int i=0; i<fopConfigNodes.getLength(); i++) {
      Element fopConfig = (Element) fopConfigNodes.item(i);     
      Element fopElement = XMLUtils.getFirstChildElement(fopConfig);
      fopConfigs.put(fopConfig.getAttribute("name"), XMLUtils.nodeToString(fopElement));
    }
   
    // initClassLoader();
            
    initFileAlterationObservers();    
  }
  
  public void open() throws Exception {
    logger.info(String.format("Opening webapp \"%s\" ...", name));
    
    logger.debug("Starting file alteration monitor ...");
    monitor.start();
    
    if (scheduler != null) {
      logger.debug("Starting Quartz scheduler ...");    
      scheduler.start();    
      logger.debug("Quartz scheduler started.");
    }
    
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
    
    logger.info("Closing XPath extension functions ...");
    Iterator<ExtensionFunctionDefinition> functions = configuration.getRegisteredExtensionFunctions();
    while (functions.hasNext()) {
      ExtensionFunctionDefinition function = functions.next();
      if (function instanceof Closeable) {
        ((Closeable) function).close();
      }
    }
    
    if (httpClient != null) {
      logger.debug("Closing HTTP client ...");
      httpClient.close();
      httpClient = null;
    }
    
    if (!indexCache.isEmpty()) {
      logger.info("Closing Indexes ...");
      for (XMLIndex index : indexCache.values()) {
        index.close();
      }
    }
    
    if (!dataSourceCache.isEmpty()) {
      logger.info("Closing Datasources ...");
      for (ComboPooledDataSource cpds : dataSourceCache.values()) {
        cpds.close();
      }
    }
    
    logger.info("Clearing compiled XSLT stylesheet cache ...");
    xsltExecutableCache.clear();
    
    logger.info("Clearing compiled STX stylesheet cache ...");
    templatesCache.clear();
    
    logger.info("Clearing compiled XQuery cache ...");
    xqueryExecutableCache.clear();
    
    logger.info("Clearing compiled XML Schema cache ...");
    schemaCache.clear();
    
    Thread.sleep(250);
    
    logger.info(String.format("Webapp \"%s\" closed.", name));
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
  
  public int getMaxUploadSize() {
    return maxUploadSize;
  }

  public List<Resource> getResources() {
    return resources;
  }

  public List<Parameter> getParameters() {
    return parameters;
  }
  
  public Map<String, Index> getIndexes() {
    return indexes;
  }
  
  public Map<String, DataSource> getDataSources() {
    return dataSources;
  }
  
  public Configuration getConfiguration() {
    return configuration.getConfiguration();
  }
  
  public Processor getProcessor() {
    return processor;
  }
  
  public CloseableHttpClient getHttpClient() {    
    if (httpClient == null) {
      PoolingHttpClientConnectionManager cm;
      if (Context.getInstance().getTrustAllCerts()) {
        try {
          SSLContextBuilder scb = SSLContexts.custom();
          scb.loadTrustMaterial(null, new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
              return true;
            }
          });
          SSLContext sslContext = scb.build();
          SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
          Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
              .register("https", sslsf)
              .register("http", new PlainConnectionSocketFactory())
              .build();
          cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        } catch (Exception e) {
          logger.warn("Could not set HttpClient to trust all SSL certificates", e);
          cm = new PoolingHttpClientConnectionManager();
        }
      } else {
        cm = new PoolingHttpClientConnectionManager();
      }
      cm.setMaxTotal(200);
      cm.setDefaultMaxPerRoute(20);
      HttpHost localhost = new HttpHost("localhost", 80);
      cm.setMaxPerRoute(new HttpRoute(localhost), 50);
      HttpClientBuilder builder = HttpClients.custom().setConnectionManager(cm);
      builder.setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()));
      builder.setDefaultCookieStore(new BasicCookieStore());
      httpClient = builder.build();
    }
    return httpClient;
  }

  public XsltExecutable getRequestDispatcherTemplates(ErrorListener errorListener, boolean tracing) throws Exception {
    return tryXsltExecutableCache(new File(getHomeDir(), 
        Definitions.PATHNAME_REQUESTDISPATCHER_XSL).getAbsolutePath(), errorListener, tracing);
  }
  
  public XsltExecutable getXsltExecutable(String path, ErrorListener errorListener, boolean tracing) throws Exception {    
    if (new File(path).isAbsolute()) {
      return tryXsltExecutableCache(path, errorListener, tracing);
    }    
    return tryXsltExecutableCache(new File(getHomeDir(), "xsl" + "/" + path).getAbsolutePath(), errorListener, tracing);
  }
  
  public Templates getTemplates(String path, ErrorListener errorListener) throws Exception {    
    if (new File(path).isAbsolute()) {
      return tryTemplatesCache(path, errorListener);
    }    
    return tryTemplatesCache(new File(getHomeDir(), "stx" + "/" + path).getAbsolutePath(), errorListener);
  }
  
  public XQueryExecutable getQuery(String path, ErrorListener errorListener, boolean tracing) throws Exception {    
    if (new File(path).isAbsolute()) {
      return tryQueryCache(path, errorListener, tracing);
    }    
    return tryQueryCache(new File(getHomeDir(), "xquery" + "/" + path).getAbsolutePath(), errorListener, tracing);
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
  
  public XsltExecutable tryXsltExecutableCache(String transformationPath,  
      ErrorListener errorListener, boolean tracing) throws Exception {
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
        Source source = new SAXSource(reader, new InputSource(transformationPath));         
        XsltCompiler comp = processor.newXsltCompiler();
        comp.setCompileWithTracing(tracing);
        comp.setErrorListener(errorListener);
        xsltExecutable = comp.compile(source);        
      } catch (Exception e) {
        logger.error("Could not compile XSLT stylesheet \"" + transformationPath + "\"", e);
        throw e;
      }      
      if (!developmentMode) {
        xsltExecutableCache.put(key, xsltExecutable);
      }      
    }
    return xsltExecutable;
  }
  
  public Templates tryTemplatesCache(String transformationPath,  
      ErrorListener errorListener) throws Exception {
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
        Source source = new SAXSource(reader, new InputSource(transformationPath));         
        net.sf.joost.trax.TransformerFactoryImpl tfi = new net.sf.joost.trax.TransformerFactoryImpl();
        tfi.setAttribute(TrAXConstants.MESSAGE_EMITTER_CLASS, new MessageEmitter());
        tfi.setErrorListener(errorListener);
        templates = tfi.newTemplates(source);
      } catch (Exception e) {
        logger.error("Could not compile STX stylesheet \"" + transformationPath + "\"", e);
        throw e;
      }      
      if (!developmentMode) {
        templatesCache.put(key, templates);
      }      
    }
    return templates;
  }
  
  public XQueryExecutable tryQueryCache(String xqueryPath,  
      ErrorListener errorListener, boolean tracing) throws Exception {
    String key = FilenameUtils.normalize(xqueryPath);
    XQueryExecutable xquery = xqueryExecutableCache.get(key);    
    if (xquery == null) {
      logger.info("Compiling and caching xquery \"" + xqueryPath + "\" ...");                 
      try {
        XQueryCompiler comp = processor.newXQueryCompiler();
        comp.setErrorListener(errorListener);
        comp.setCompileWithTracing(tracing);
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
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);       
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
        Source source = new StreamSource(new File(schematronPath));
        File schematronDir = new File(Context.getInstance().getHomeDir(), "common/xsl/system/schematron");
        
        ErrorListener listener = new TransformationErrorListener(null, developmentMode); 
        MessageWarner messageWarner = new MessageWarner();
        
        Xslt30Transformer stage1 = tryXsltExecutableCache(new File(schematronDir, "iso_dsdl_include.xsl").getAbsolutePath(), errorListener, false).load30();
        stage1.setErrorListener(listener);
        stage1.getUnderlyingController().setMessageEmitter(messageWarner);
        Xslt30Transformer stage2 = tryXsltExecutableCache(new File(schematronDir, "iso_abstract_expand.xsl").getAbsolutePath(), errorListener, false).load30();
        stage2.setErrorListener(listener);
        stage2.getUnderlyingController().setMessageEmitter(messageWarner);
        Xslt30Transformer stage3 = tryXsltExecutableCache(new File(schematronDir, "iso_svrl_for_xslt2.xsl").getAbsolutePath(), errorListener, false).load30();
        stage3.setErrorListener(listener);
        stage3.getUnderlyingController().setMessageEmitter(messageWarner);
       
        XdmDestination destStage1 = new XdmDestination();
        XdmDestination destStage2 = new XdmDestination();
        XdmDestination destStage3 = new XdmDestination();

        stage1.applyTemplates(source, destStage1);
        stage2.applyTemplates(destStage1.getXdmNode().asSource(), destStage2);
        stage3.applyTemplates(destStage2.getXdmNode().asSource(), destStage3);
        
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
  
  public Map<String, Collection<Attribute>> getAttributes() {
    return this.attributes;
  }
  
  public void setAttributes(Map<String, Collection<Attribute>> attributes) {
    this.attributes = attributes;
  }
  
  public Collection<Attribute> getAttribute(String name) {
    return attributes.get(name);
  }
  
  public void removeAttribute(String name) {
    attributes.remove(name);
  }
  
  public void setAttribute(String name, Collection<Attribute> attrs) {
    if (attrs != null) {
      attributes.put(name, attrs);
    } else {
      attributes.remove(name);
    }
  }
  
  @SuppressWarnings("unchecked")
  public Collection<Attribute> getCacheValue(String cacheName, String keyName) {
    Cache cache = Context.getInstance().getCacheManager().getCache(cacheName);
    if (cache == null) {
      return null;
    }    
    net.sf.ehcache.Element elem = cache.get(keyName);
    if (elem != null) {    
      return (Collection<Attribute>) elem.getObjectValue();
    } 
    return null;
  }
  
  public void setCacheValue(String cacheName, String keyName, Collection<Attribute> attrs, int tti, int ttl) {
    CacheManager manager = Context.getInstance().getCacheManager();
    Cache cache = manager.getCache(cacheName);
    if (cache == null) {      
      cache = new Cache(
          new CacheConfiguration(cacheName, 1000)
            .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU)
            .eternal(false)
            .timeToLiveSeconds(60)
            .timeToIdleSeconds(60)
            .diskExpiryThreadIntervalSeconds(120)
            .persistence(new PersistenceConfiguration().strategy(Strategy.LOCALTEMPSWAP)));
      manager.addCache(cache);
    }            
    cache.put(new net.sf.ehcache.Element(keyName, attrs, false, tti, ttl));
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
  
  public XMLIndex getXMLIndex(String name) throws Exception {
    XMLIndex xmlIndex = indexCache.get(name);
    if (xmlIndex == null) {      
      Index index = indexes.get(name);
      if (index == null)
        throw new XSLWebException("Index definition \"" + name + "\" not found in webapp.xml");        
      Path indexPath = Paths.get(index.getPath());
      if (!indexPath.isAbsolute())
        indexPath = homeDir.toPath().resolve(indexPath);
      xmlIndex = new XMLIndex(index.getName(), indexPath);
      xmlIndex.setConfiguration(index.getConfig());
      xmlIndex.open();
      indexCache.put(name, xmlIndex);
    }
    return xmlIndex;
  }
  
  public ComboPooledDataSource getDataSource(String name) throws Exception {
    ComboPooledDataSource cpds = dataSourceCache.get(name);    
    if (cpds == null) {      
      DataSource dataSource = dataSources.get(name);
      if (dataSource == null) {
        throw new XSLWebException("Datasource definition \"" + name + "\" not found in webapp.xml");        
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
  
  public FopFactory getFopFactory(String configName) throws Exception {
    FopFactory fopFactory = fopFactoryCache.get(configName);
    if (fopFactory == null) {
      String fopConfig = fopConfigs.get(configName);
      if (fopConfig == null) {
        throw new XSLWebException("FOP Configuration \"" + configName + "\" not found in webapp.xml");        
      }      
      fopFactory = FopFactory.newInstance(getHomeDir().toURI(), IOUtils.toInputStream(fopConfig, "UTF-8"));      
      fopFactoryCache.put(configName, fopFactory);
    }
    return fopFactory;
  }
  
  public synchronized void incJobRequestCount() {
    jobRequestCount++;
    logger.debug("Ïncrement jobRequestCount " + jobRequestCount);
  }
  
  public synchronized void decJobRequestCount() {
    jobRequestCount--;
    logger.debug("Decrement jobRequestCount " + jobRequestCount);
  }
  
}