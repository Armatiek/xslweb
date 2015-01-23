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
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.xpath.XPathFactoryImpl;
import nl.armatiek.xslweb.quartz.NonConcurrentExecutionXSLWebJob;
import nl.armatiek.xslweb.quartz.XSLWebJob;
import nl.armatiek.xslweb.saxon.configuration.XSLWebConfiguration;
import nl.armatiek.xslweb.utils.XMLUtils;
import nl.armatiek.xslweb.utils.XSLWebUtils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
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
import org.quartz.impl.StdSchedulerFactory;
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

public class WebApp implements ErrorHandler {
  
  private static final Logger logger = LoggerFactory.getLogger(WebApp.class);
  
  private Map<String, XsltExecutable> templatesCache = 
      Collections.synchronizedMap(new HashMap<String, XsltExecutable>());
  
  private Map<String, Collection<Attribute>> attributes = 
      Collections.synchronizedMap(new HashMap<String, Collection<Attribute>>());
      
  private File definition;
  private File homeDir;  
  private String name;
  private String title;
  private String description;
  private boolean developmentMode;
  private int maxUploadSize;
  private Scheduler scheduler;
  private List<Resource> resources = new ArrayList<Resource>();
  private List<Parameter> parameters = new ArrayList<Parameter>();  
  private XSLWebConfiguration configuration;  
  private Processor processor;  
  private FileAlterationMonitor monitor;
  private CloseableHttpClient httpClient;
  
  public WebApp(File webAppDefinition) throws Exception {   
    logger.info(String.format("Loading webapp definition \"%s\" ...", webAppDefinition.getAbsolutePath()));
    
    Context context = Context.getInstance();
    this.definition = webAppDefinition;
    this.homeDir = webAppDefinition.getParentFile();
    this.name = this.homeDir.getName();
    
    this.configuration = new XSLWebConfiguration(this);
    this.processor = new Processor(this.configuration);
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);    
    dbf.setSchema(context.getWebAppSchema());    
    dbf.setXIncludeAware(true);
    DocumentBuilder db = dbf.newDocumentBuilder();
    db.setErrorHandler(this);    
    Document webAppDoc = db.parse(webAppDefinition);
        
    XPath xpath = new XPathFactoryImpl().newXPath();
    xpath.setNamespaceContext(XMLUtils.getNamespaceContext("webapp", Definitions.NAMESPACEURI_XSLWEB_WEBAPP));    
    Node docElem = webAppDoc.getDocumentElement();
    this.title = (String) xpath.evaluate("webapp:title", docElem, XPathConstants.STRING);
    this.description = (String) xpath.evaluate("webapp:description", docElem, XPathConstants.STRING);
    String devModeValue = (String) xpath.evaluate("webapp:development-mode", docElem, XPathConstants.STRING);
    this.developmentMode = XMLUtils.getBooleanValue(devModeValue, false); 
    String maxUploadSizeValue = (String) xpath.evaluate("webapp:max-upload-size", docElem, XPathConstants.STRING);
    this.maxUploadSize = XMLUtils.getIntegerValue(maxUploadSizeValue, 10);
    
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
    
    logger.info(String.format("Webapp \"%s\" opened.", name));    
  }
  
  public void close() throws Exception {    
    logger.info(String.format("Closing webapp \"%s\" ...", name));
    
    logger.debug("Stopping file alteration monitor ...");
    monitor.stop();
    
    if (scheduler != null) {
      logger.debug("Shutting down Quartz scheduler ...");
      scheduler.shutdown(!developmentMode);
      logger.debug("Shutdown of Quartz scheduler complete.");
    }
    
    logger.debug("Closing XPath extension functions ...");
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
  
  public File getHomeDir() {
    return homeDir;
  }
  
  public String getName() {
    return name;
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
  
  public Configuration getConfiguration() {
    return configuration;
  }
  
  public Processor getProcessor() {
    return processor;
  }
  
  public CloseableHttpClient getHttpClient() {    
    if (httpClient == null) {
      PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
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

  public XsltExecutable getRequestDispatcherTemplates(ErrorListener errorListener) throws Exception {
    return tryTemplatesCache(new File(getHomeDir(), Definitions.PATHNAME_REQUESTDISPATCHER_XSL).getAbsolutePath(), errorListener);
  }
  
  public XsltExecutable getTemplates(String path, ErrorListener errorListener) throws Exception {    
    if (new File(path).isAbsolute()) {
      return tryTemplatesCache(path, errorListener);
    }    
    return tryTemplatesCache(new File(getHomeDir(), "xsl" + "/" + path).getAbsolutePath(), errorListener);
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
  
  public XsltExecutable tryTemplatesCache(String transformationPath,  
      ErrorListener errorListener) throws Exception {
    String key = FilenameUtils.normalize(transformationPath);
    XsltExecutable templates = (XsltExecutable) templatesCache.get(key);    
    if (templates == null) {
      logger.info("Compiling and caching stylesheet \"" + transformationPath + "\" ...");                 
      try {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setXIncludeAware(true);
        spf.setValidating(false);
        SAXParser parser = spf.newSAXParser();
        XMLReader reader = parser.getXMLReader();        
        Source source = new SAXSource(reader, new InputSource(transformationPath));         
        XsltCompiler comp = processor.newXsltCompiler();
        comp.setErrorListener(errorListener);
        templates = comp.compile(source);        
      } catch (Exception e) {
        logger.error("Could not compile stylesheet \"" + transformationPath + "\"", e);
        throw e;
      }      
      if (!developmentMode) {
        templatesCache.put(key, templates);
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
    return (Collection<Attribute>) cache.get(keyName).getObjectValue();
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
  
}