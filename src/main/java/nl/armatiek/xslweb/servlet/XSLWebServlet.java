package nl.armatiek.xslweb.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.serialize.MessageWarner;
import nl.armatiek.xslweb.configuration.Config;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.pipeline.PipelineHandler;
import nl.armatiek.xslweb.pipeline.PipelineStep;
import nl.armatiek.xslweb.pipeline.TransformerStep;
import nl.armatiek.xslweb.vfs.VfsResolver;
import nl.armatiek.xslweb.vfs.transform.VfsOutputUriResolver;
import nl.armatiek.xslweb.vfs.transform.VfsUriResolver;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XSLWebServlet extends HttpServlet {
  
  private static final long serialVersionUID = 1L;
  
  private static final Logger logger = LoggerFactory.getLogger(XSLWebServlet.class);
  
  private Configuration configuration;
  private FileSystemManager fsManager;
  private VfsResolver vfsResolver;
  private boolean isDevelopmentMode;
  
  public void init() throws ServletException {
    super.init();
    try {
      fsManager = VFS.getManager();
      vfsResolver = new VfsResolver(fsManager);
      
      isDevelopmentMode = Config.getInstance().isDevelopmentMode();
      
      configuration = new Configuration();
      // configuration.setURIResolver();
      // configuration.setOutputURIResolver();
      configuration.setXIncludeAware(true);
      // configuration.registerExtensionFunction(new com.armatiek.infofuze.xslt.functions.request.Attribute());
      
    } catch (Exception e) {
      logger.error(e.getMessage());
      throw new ServletException(e);
    }
  }
  
  private Controller getController(String path, ErrorListener errorListener) throws Exception {
    Templates controllerTemplates = TemplatesCache.tryTemplatesCache(path, 
        errorListener, configuration);
    
    Controller controller = (Controller) controllerTemplates.newTransformer();
    controller.setURIResolver(new VfsUriResolver(vfsResolver));
    controller.setOutputURIResolver(new VfsOutputUriResolver(vfsResolver));    
    controller.setMessageEmitter(new MessageWarner());
    return controller;
  }
 
  private void executeRequest(HttpServletRequest req, HttpServletResponse resp) throws Exception {        
    String requestXML = RequestSerializer.serializeToXML(req);
    
    File requestDebugFile = null;
    File responseDebugFile = null;
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
      FileUtils.writeStringToFile(requestDebugFile, requestXML, "UTF-8");
    }
    
    ErrorListener errorListener = new TransformationErrorListener(resp);       
    
    // 
    Controller controllerTransformer = getController(Config.getInstance().getControllerXslPath(), errorListener);
                       
    PipelineHandler pipelineHandler = new PipelineHandler();
    controllerTransformer.transform(new StreamSource(new StringReader(requestXML)), new SAXResult(pipelineHandler));
    
    SAXTransformerFactory stf = (SAXTransformerFactory) TransformerFactory.newInstance();
    TransformerHandler firstHandler = null;
    TransformerHandler newHandler = null;
    TransformerHandler currentHandler = null;    
    Templates templates = null;
    List<PipelineStep> steps = pipelineHandler.getPipelineSteps();
    for (PipelineStep step : steps) {
      if (step instanceof TransformerStep) {
        templates = TemplatesCache.tryTemplatesCache(((TransformerStep) step).getXslPath(), 
            errorListener, configuration);        
        newHandler = stf.newTransformerHandler(templates);
        if (firstHandler != null) {
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
    
    Transformer transformer = stf.newTransformer();
    transformer.transform(new StreamSource(new StringReader(requestXML)), new SAXResult(firstHandler));
    
    if (isDevelopmentMode) {
      FileUtils.writeByteArrayToFile(responseDebugFile, ((ByteArrayOutputStream) os).toByteArray());
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