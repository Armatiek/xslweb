package nl.armatiek.xslweb.configuration;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
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
  
  private Map<String, Templates> templatesCache = 
      Collections.synchronizedMap(new HashMap<String, Templates>());
  
  private Map<String, Collection<Attribute>> attributes = 
      Collections.synchronizedMap(new HashMap<String, Collection<Attribute>>());
  
  private File definition;
  private File homeDir;  
  private String name;
  private String title;
  private String description;
  private Scheduler scheduler;
  private List<Resource> resources = new ArrayList<Resource>();
  private List<Parameter> parameters = new ArrayList<Parameter>();
  private XSLWebConfiguration configuration;
  private Processor processor;
  private FileAlterationMonitor monitor;
  
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
    Document webAppDoc = db.parse(webAppDefinition);
        
    XPath xpath = XPathFactory.newInstance().newXPath();
    xpath.setNamespaceContext(XMLUtils.getNamespaceContext("webapp", Definitions.NAMESPACEURI_XSLWEB_WEBAPP));    
    Node docElem = webAppDoc.getDocumentElement();
    this.title = (String) xpath.evaluate("webapp:title", docElem, XPathConstants.STRING);
    this.description = (String) xpath.evaluate("webapp:description", docElem, XPathConstants.STRING);    
    NodeList resourceNodes = (NodeList) xpath.evaluate("webapp:resources/webapp:resource", docElem, XPathConstants.NODESET);
    for (int i=0; i<resourceNodes.getLength(); i++) {
      resources.add(new Resource((Element) resourceNodes.item(i)));
    }
    NodeList paramNodes = (NodeList) xpath.evaluate("webapp:parameters/webapp:parameter", docElem, XPathConstants.NODESET);
    for (int i=0; i<paramNodes.getLength(); i++) {
      parameters.add(new Parameter((Element) paramNodes.item(i)));
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
    
    this.configuration = new XSLWebConfiguration(this);
    this.processor = new Processor(configuration);
    
    /*
    XSLTTraceListener listener = new XSLTTraceListener();
    listener.setOutputDestination(System.out);
    configuration.setTraceListener(listener);
    */
    
    // initExtensionFunctions();
    
    initFileAlterationObservers();
  }
  
  public void open() throws Exception {
    logger.info(String.format("Opening webapp \"%s\" ...", name));
    
    logger.info("Starting file alteration monitor ...");
    monitor.start();
    
    if (scheduler != null) {
      logger.info("Starting Quartz scheduler ...");    
      scheduler.start();    
      logger.info("Quartz scheduler started.");
    }    
    logger.info(String.format("Webapp \"%s\" opened.", name));    
  }
  
  public void close() throws Exception {    
    logger.info(String.format("Closing webapp \"%s\" ...", name));
    
    logger.info("Stopping file alteration monitor ...");
    monitor.stop();
    
    if (scheduler != null) {
      logger.info("Shutting down Quartz scheduler ...");
      scheduler.shutdown(!Context.getInstance().isDevelopmentMode());
      logger.info("Shutdown of Quartz scheduler complete.");
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
    
    if (!Context.getInstance().isDevelopmentMode()) {
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
    return (name.equals("root")) ? "/" : "/" + name;
  }
  
  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
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

  public Templates getRequestDispatcherTemplates(ErrorListener errorListener) throws Exception {
    return tryTemplatesCache(new File(getHomeDir(), Definitions.PATHNAME_REQUESTDISPATCHER_XSL).getAbsolutePath(), errorListener);
  }
  
  public Templates getTemplates(String path, ErrorListener errorListener) throws Exception {    
    if (new File(path).isAbsolute()) {
      return tryTemplatesCache(path, errorListener);
    }    
    return tryTemplatesCache(new File(getHomeDir(), "xsl" + "/" + path).getAbsolutePath(), errorListener);
  }
  
  public File getStaticFile(String path) {
    return new File(this.homeDir, "static" + "/" + StringUtils.substringAfter(path, this.name + "/"));    
  }
  
  public String getRelativePath(String path) {
    return StringUtils.substringAfter(path, "/" + name);
  }

  public Resource matchesResource(String path) {    
    for (Resource resource : this.resources) {      
      if (resource.getPattern().matcher(path).matches()) {
        return resource;
      }     
    }
    return null;
  }
  
  public Templates tryTemplatesCache(String transformationPath,  
      ErrorListener errorListener) throws Exception {
    String key = FilenameUtils.normalize(transformationPath);
    Templates templates = (Templates) templatesCache.get(key);    
    if (templates == null) {
      logger.info("Compiling and caching stylesheet \"" + transformationPath + "\" ...");      
      XsltCompiler compiler = processor.newXsltCompiler();      
      compiler.setErrorListener(errorListener);      
      try {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setXIncludeAware(true);
        spf.setValidating(false);
        SAXParser parser = spf.newSAXParser();
        XMLReader reader = parser.getXMLReader();        
        Source source = new SAXSource(reader, new InputSource(transformationPath));
        XsltExecutable executable = compiler.compile(source);        
        templates = executable.getUnderlyingCompiledStylesheet();               
      } catch (Exception e) {
        logger.error("Could not compile stylesheet \"" + transformationPath + "\"", e);
        throw e;
      }      
      if (!Context.getInstance().isDevelopmentMode()) {
        templatesCache.put(key, templates);
      }      
    }
    return templates;
  }
  
  public  Map<String, Collection<Attribute>> getAttributes() {
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
    attributes.put(name, attrs);
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