package nl.armatiek.xslweb.configuration;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.CronScheduleBuilder.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.Configuration;
import nl.armatiek.xslweb.quartz.JobScheduler;
import nl.armatiek.xslweb.quartz.XSLWebJob;
import nl.armatiek.xslweb.utils.XMLUtils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDetail;
import org.quartz.Trigger;
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
  
  private static Map<String, Templates> templatesCache = 
      Collections.synchronizedMap(new HashMap<String, Templates>());
  
  private File definition;
  private File homeDir;
  private String name;
  private String title;
  private String description;
  private List<Resource> resources = new ArrayList<Resource>();
  private List<Parameter> parameters = new ArrayList<Parameter>();
  
  public WebApp(File webAppDefinition, Schema webAppSchema) throws Exception {   
    logger.info(String.format("Loading webapp definition \"%s\" ...", webAppDefinition.getAbsolutePath()));
    
    this.definition = webAppDefinition;
    this.homeDir = webAppDefinition.getParentFile();
    this.name = this.homeDir.getName();
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);    
    dbf.setSchema(webAppSchema);    
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
    for (int i=0; i<jobNodes.getLength(); i++) {
      Element jobElem = (Element) jobNodes.item(i);      
      String jobName = XMLUtils.getValueOfChildElementByLocalName(jobElem, "name");
      String jobUri = XMLUtils.getValueOfChildElementByLocalName(jobElem, "uri");
      String jobCron = XMLUtils.getValueOfChildElementByLocalName(jobElem, "cron");  
      String jobId = "job_" + name + "_" + jobName;
      JobDetail job = newJob(XSLWebJob.class)
          .withIdentity(jobId, name)
          .usingJobData("uri", jobUri)
          .build();      
      Trigger trigger = newTrigger()
          .withIdentity("trigger_" + name + "_" + jobName, name)
          .withSchedule(cronSchedule(jobCron))
          .forJob(jobId, name)                     
          .build(); 
      logger.info("Adding job to scheduler");
      // JobScheduler.getInstance().getScheduler().scheduleJob(job, trigger);
    }    
  }
  
  public void close() {
    //
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

  public Templates getRequestDispatcherTemplates(ErrorListener errorListener, Configuration configuration) throws Exception {
    return tryTemplatesCache(new File(getHomeDir(), Definitions.FILENAME_REQUESTDISPATCHER_XSL).getAbsolutePath(), errorListener, configuration);
  }
  
  public Templates getTemplates(String path, ErrorListener errorListener, Configuration configuration) throws Exception {
    return tryTemplatesCache(new File(getHomeDir(), "xsl" + "/" + path).getAbsolutePath(), errorListener, configuration);
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
      ErrorListener errorListener, Configuration configuration) throws Exception {
    String key = FilenameUtils.normalize(transformationPath);
    Templates templates = (Templates) templatesCache.get(key);
    if (templates == null) {
      logger.info("Compiling and caching stylesheet \"" + transformationPath + "\" ...");
      TransformerFactory factory = new net.sf.saxon.TransformerFactoryImpl(configuration);      
      if (errorListener != null) {
        factory.setErrorListener(errorListener);
      }
      try {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setXIncludeAware(true);
        spf.setValidating(false);
        SAXParser parser = spf.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        Source source = new SAXSource(reader, new InputSource(transformationPath));
        templates = factory.newTemplates(source);
      } catch (Exception e) {
        logger.error("Could not compile stylesheet \"" + transformationPath + "\"", e);
        throw e;
      }      
      if (!Config.getInstance().isDevelopmentMode()) {
        templatesCache.put(key, templates);
      }      
    }
    return templates;
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