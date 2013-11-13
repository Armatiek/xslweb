package nl.armatiek.xslweb.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
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
import javax.xml.transform.stream.StreamResult;
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
import nl.armatiek.xslweb.saxon.functions.ResponseHeader;
import nl.armatiek.xslweb.saxon.functions.ResponseStatus;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.expath.httpclient.saxon.SendRequestFunction;
import org.expath.zip.saxon.BinaryEntryFunction;
import org.expath.zip.saxon.EntriesFunction;
import org.expath.zip.saxon.HtmlEntryFunction;
import org.expath.zip.saxon.TextEntryFunction;
import org.expath.zip.saxon.UpdateEntriesFunction;
import org.expath.zip.saxon.XmlEntryFunction;
import org.expath.zip.saxon.ZipFileFunction;
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
      
      configuration.registerExtensionFunction(new ResponseHeader());
      configuration.registerExtensionFunction(new ResponseStatus());
      
      configuration.registerExtensionFunction(new EntriesFunction());
      configuration.registerExtensionFunction(new UpdateEntriesFunction());
      configuration.registerExtensionFunction(new ZipFileFunction());
      configuration.registerExtensionFunction(new BinaryEntryFunction());
      configuration.registerExtensionFunction(new HtmlEntryFunction());
      configuration.registerExtensionFunction(new TextEntryFunction());
      configuration.registerExtensionFunction(new XmlEntryFunction());
      
      configuration.registerExtensionFunction(new SendRequestFunction());
  
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
  
  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
    
    //TransformerHandler firstHandler = null;
    //TransformerHandler newHandler = null;
    //TransformerHandler currentHandler = null;    
    // Templates templates = null;
    List<PipelineStep> steps = pipelineHandler.getPipelineSteps();
    if (steps == null) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
    }
    steps.add(new TransformerStep("system/response.xsl"));
    
    /*
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
    */ 
    
    Templates templates = null;
    TransformerHandler handler = null;
    Controller transformer = null;
    
    List<TransformerHandler> handlers = new ArrayList<TransformerHandler>();
    for (PipelineStep step : steps) {
      templates = TemplatesCache.tryTemplatesCache(new File(homeDir, "xsl/" + ((TransformerStep) step).getXslPath()).getAbsolutePath(), errorListener, configuration);      
      handler = stf.newTransformerHandler(templates);
      transformer = (Controller) handler.getTransformer();
      setPropertyParameters(transformer);
      transformer.setErrorListener(errorListener);
      transformer.setMessageEmitter(messageWarner);
      if (!handlers.isEmpty()) {
        handlers.get(handlers.size()-1).setResult(new SAXResult(handler));
      }
      handlers.add(handler); 
    }
            
    OutputStream os = (Config.getInstance().isDevelopmentMode()) ? new ByteArrayOutputStream() : resp.getOutputStream();            
    // newHandler.setResult(new SAXResult(new ResponseHandler(resp, os, currentHandler.getTransformer().getOutputProperties())));
    handler.setResult(new StreamResult(os));
    transformer.setParameter("{" + Definitions.NAMESPACEURI_XSLWEB_RESPONSE + "}response", resp);
    TransformerHandler lastHandler = handlers.get(handlers.size()-2);
    transformer.setOutputProperties(lastHandler.getTransformer().getOutputProperties());
    
    //Controller stepsTransformer = (Controller) stf.newTransformer();
    //setPropertyParameters(stepsTransformer);
    //stepsTransformer.setParameter("{" + Definitions.NAMESPACEURI_XSLWEB_RESPONSE + "}response", resp);
    //stepsTransformer.setErrorListener(errorListener);    
    // stepsTransformer.setURIResolver(uriResolver);
    // stepsTransformer.setOutputURIResolver(outputURIResolver);    
    //stepsTransformer.setMessageEmitter(messageWarner);
    transformer.transform(new StreamSource(new StringReader(requestXML)), new SAXResult(handlers.get(0)));
    
    if (isDevelopmentMode) {
      byte[] body = ((ByteArrayOutputStream) os).toByteArray();
      FileUtils.writeByteArrayToFile(responseDebugFile, body);
      IOUtils.copy(new ByteArrayInputStream(body), resp.getOutputStream());
    }    
  }
  
}