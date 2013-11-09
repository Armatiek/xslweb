package nl.armatiek.xslweb.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.serialize.MessageWarner;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Config;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.pipeline.PipelineHandler;
import nl.armatiek.xslweb.pipeline.PipelineStep;
import nl.armatiek.xslweb.pipeline.TransformerStep;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XSLWebServlet extends HttpServlet {
  
  private static final long serialVersionUID = 1L;
  
  private static final Logger logger = LoggerFactory.getLogger(XSLWebServlet.class);
  
  private Configuration configuration;
  private File homeDir;
  //private FileSystemManager fsManager;
  //private VfsResolver vfsResolver;
  private boolean isDevelopmentMode;
  private File requestDebugFile;
  private File responseDebugFile;
  
  public void init() throws ServletException {
    super.init();
    try {
      //fsManager = VFS.getManager();
      // ((DefaultFileSystemManager) fsManager).setCacheStrategy(CacheStrategy.ON_CALL);
      //vfsResolver = new VfsResolver(fsManager);
      
      configuration = new Configuration();
      // configuration.setURIResolver();
      // configuration.setOutputURIResolver();      
      configuration.setXIncludeAware(true);
      // configuration.registerExtensionFunction(new com.armatiek.infofuze.xslt.functions.request.Attribute());
  
      isDevelopmentMode = Config.getInstance().isDevelopmentMode();
      if (isDevelopmentMode) {
        File debugDir = new File(Config.getInstance().getHomeDir(), "debug");
        requestDebugFile = new File(debugDir, "request.xml");
        responseDebugFile = new File(debugDir, "response.xml");
        if (!debugDir.exists()) {
          debugDir.mkdirs();
        } else {      
          FileUtils.deleteQuietly(requestDebugFile);
          FileUtils.deleteQuietly(responseDebugFile);
        }        
      }
      
      homeDir = Config.getInstance().getHomeDir();
      
    } catch (Exception e) {
      logger.error(e.getMessage());
      throw new ServletException(e);
    }
  }
  
  private void setPropertyParameters(Controller controller) throws IOException {
    Properties props = Config.getInstance().getProperties();
    for (String key : props.stringPropertyNames()) {
      String value = props.getProperty(key);      
      controller.setParameter(
          new StructuredQName("configuration", Definitions.NAMESPACEURI_XSLWEB_CONFIGURATION, key),
          new StringValue(value));
    }
  }
  
  private void executeRequest(HttpServletRequest req, HttpServletResponse resp) throws Exception {        
    String requestXML = RequestSerializer.serializeToXML(req);
    
    if (isDevelopmentMode) {      
      FileUtils.deleteQuietly(requestDebugFile);
      FileUtils.deleteQuietly(responseDebugFile);
      FileUtils.writeStringToFile(requestDebugFile, requestXML, "UTF-8");
    }
    
    ErrorListener errorListener = new TransformationErrorListener(resp);
    // URIResolver uriResolver = new VfsUriResolver(vfsResolver);
    // OutputURIResolver outputURIResolver = new VfsOutputUriResolver(vfsResolver);
    MessageWarner messageWarner = new MessageWarner();
    
    Templates controllerTemplates = TemplatesCache.tryTemplatesCache(Config.getInstance().getControllerXslPath(), errorListener, configuration);    
    Controller controllerTransformer = (Controller) controllerTemplates.newTransformer();
    setPropertyParameters(controllerTransformer);
    controllerTransformer.setErrorListener(errorListener);
    // controllerTransformer.setURIResolver(uriResolver);
    // controllerTransformer.setOutputURIResolver(outputURIResolver);    
    controllerTransformer.setMessageEmitter(messageWarner);
    
                             
    PipelineHandler pipelineHandler = new PipelineHandler();
    controllerTransformer.transform(new StreamSource(new StringReader(requestXML)), new SAXResult(pipelineHandler));
    
    SAXTransformerFactory stf = (SAXTransformerFactory) net.sf.saxon.TransformerFactoryImpl.newInstance();
    stf.setErrorListener(errorListener);
    // stf.setURIResolver(uriResolver);
    
    TransformerHandler firstHandler = null;
    TransformerHandler newHandler = null;
    TransformerHandler currentHandler = null;    
    Templates templates = null;
    List<PipelineStep> steps = pipelineHandler.getPipelineSteps();
    if (steps == null) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
    }
    steps.add(new TransformerStep(""));
    for (PipelineStep step : steps) {
      if (step instanceof TransformerStep) {
        templates = TemplatesCache.tryTemplatesCache(new File(homeDir, "xsl/" + ((TransformerStep) step).getXslPath()).getAbsolutePath(), errorListener, configuration);        
        newHandler = stf.newTransformerHandler(templates);
        if (firstHandler == null) {
          firstHandler = newHandler;
        }
        if (currentHandler != null) {
          currentHandler.setResult(new SAXResult(newHandler));
        }        
        currentHandler = newHandler;
      }                        
    }    
    if (newHandler == null) {
      // 
    }
    
    OutputStream os = (Config.getInstance().isDevelopmentMode()) ? new ByteArrayOutputStream() : resp.getOutputStream();            
    newHandler.setResult(new SAXResult(new ResponseHandler(resp, os, currentHandler.getTransformer().getOutputProperties())));
    
    Controller stepsTransformer = (Controller) stf.newTransformer();
    setPropertyParameters(stepsTransformer);
    stepsTransformer.setErrorListener(errorListener);
    // stepsTransformer.setURIResolver(uriResolver);
    // stepsTransformer.setOutputURIResolver(outputURIResolver);    
    stepsTransformer.setMessageEmitter(messageWarner);
    stepsTransformer.transform(new StreamSource(new StringReader(requestXML)), new SAXResult(firstHandler));
    
    if (isDevelopmentMode) {
      byte[] body = ((ByteArrayOutputStream) os).toByteArray();
      FileUtils.writeByteArrayToFile(responseDebugFile, body);
      IOUtils.copy(new ByteArrayInputStream(body), resp.getOutputStream());
    }    
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {       
    try {
      executeRequest(req, resp);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      if (isDevelopmentMode) {              
        resp.setContentType("text/plain; charset=UTF-8");        
        e.printStackTrace(new PrintStream(resp.getOutputStream()));        
      } else if (!resp.isCommitted()) {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    }            
  }
  
  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    this.doGet(req, resp);        
  }

  @Override
  protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {    
    this.doGet(req, resp);
  }

  @Override
  protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {    
    this.doGet(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    this.doGet(req, resp);
    
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    this.doGet(req, resp);
  }

  @Override
  protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    this.doGet(req, resp);
  }
  
}