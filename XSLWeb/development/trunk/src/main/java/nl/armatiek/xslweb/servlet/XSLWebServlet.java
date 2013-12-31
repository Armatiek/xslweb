package nl.armatiek.xslweb.servlet;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Controller;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.serialize.MessageWarner;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.Parameter;
import nl.armatiek.xslweb.configuration.Resource;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.pipeline.PipelineHandler;
import nl.armatiek.xslweb.pipeline.PipelineStep;
import nl.armatiek.xslweb.pipeline.ResponseStep;
import nl.armatiek.xslweb.pipeline.SystemTransformerStep;
import nl.armatiek.xslweb.pipeline.TransformerStep;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;

public class XSLWebServlet extends HttpServlet {
  
  private static final long serialVersionUID = 1L;
  
  private static final Logger logger = LoggerFactory.getLogger(XSLWebServlet.class);
    
  private File homeDir;  
  private boolean isDevelopmentMode;
  private File debugDir;
  private File requestDebugFile;
  private File responseDebugFile;
  
  public void init() throws ServletException {
    super.init();   
    try {    
      isDevelopmentMode = Context.getInstance().isDevelopmentMode();
      if (isDevelopmentMode) {
        this.debugDir = new File(Context.getInstance().getHomeDir(), "debug");
        this.requestDebugFile = new File(debugDir, "request.xml");
        this.responseDebugFile = new File(debugDir, "response.xml");
        if (!debugDir.exists()) {
          debugDir.mkdirs();
        } else {      
          FileUtils.deleteQuietly(requestDebugFile);
          FileUtils.deleteQuietly(responseDebugFile);
        }                
        /*
        XSLTTraceListener listener = new XSLTTraceListener();
        listener.setOutputDestination(stream);
        configuration.setTraceListener();
        */
      }
      
      homeDir = Context.getInstance().getHomeDir();
      
    } catch (Exception e) {
      logger.error(e.getMessage());
      throw new ServletException(e);
    }
  }
  
  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      String path = StringUtils.defaultString(req.getPathInfo()) + req.getServletPath();      
      WebApp webApp = Context.getInstance().getWebApp(path);
      if (webApp == null) {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else {
        Resource resource = webApp.matchesResource(webApp.getRelativePath(path));
        if (resource == null) {                                
          executeRequest(webApp, req, resp);               
        } else {          
          resp.setContentType(resource.getMediaType());        
          FileUtils.copyFile(webApp.getStaticFile(path), resp.getOutputStream());
        }
      }
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

  private void setPropertyParameters(Controller controller, WebApp webApp) throws IOException {
    Properties props = Context.getInstance().getProperties();
    for (String key : props.stringPropertyNames()) {
      String value = props.getProperty(key);      
      controller.setParameter(
          new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_CONFIGURATION, key),
          new StringValue(value));
    }    
    controller.setParameter(
        new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_CONFIGURATION, "home-dir"),
        new StringValue(homeDir.getAbsolutePath()));
    controller.setParameter(
        new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_CONFIGURATION, "webapp-dir"),
        new StringValue(webApp.getHomeDir().getAbsolutePath()));
  }
  
  private void setParameters(Controller controller, List<Parameter> parameters) throws IOException {
    if (parameters == null) {
      return;
    }
    for (Parameter param : parameters) {
      String name = param.getName();
      if (param.getURI() != null) {
        name = "{" + param.getURI() + "}" + name;
      }            
      controller.setParameter(name, param.getValue());                  
    }        
  }
  
  private void setObjectParameters(Controller controller, WebApp webApp, HttpServletRequest req, HttpServletResponse resp) throws IOException {    
    controller.setParameter("{" + Definitions.NAMESPACEURI_XSLWEB_REQUEST + "}request", req);
    controller.setParameter("{" + Definitions.NAMESPACEURI_XSLWEB_RESPONSE + "}response", resp);
    controller.setParameter("{" + Definitions.NAMESPACEURI_XSLWEB_WEBAPP + "}webapp", webApp);               
  }
  
  public static void serializeXMLToFile(String xml, File file) throws Exception {
    TransformerFactory factory = TransformerFactory.newInstance();
    Transformer transformer = factory.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");    
    transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(file));
  }
  
  private void executeRequest(WebApp webApp, HttpServletRequest req, HttpServletResponse resp) throws Exception {        
    RequestSerializer requestSerializer = new RequestSerializer(req, webApp);    
    try {    
      String requestXML = requestSerializer.serializeToXML();
      
      if (isDevelopmentMode) {
        FileUtils.cleanDirectory(debugDir);        
        serializeXMLToFile(requestXML, requestDebugFile);
      }
      
      ErrorListener errorListener = new TransformationErrorListener(resp);      
      MessageWarner messageWarner = new MessageWarner();
      
      Templates requestDispatcherTemplates = webApp.getRequestDispatcherTemplates(errorListener);    
      Controller controllerTransformer = (Controller) requestDispatcherTemplates.newTransformer();
      setPropertyParameters(controllerTransformer, webApp);
      setObjectParameters(controllerTransformer, webApp, req, resp);
      controllerTransformer.setErrorListener(errorListener);        
      controllerTransformer.setMessageEmitter(messageWarner);            
                               
      PipelineHandler pipelineHandler = new PipelineHandler(webApp.getConfiguration());
      controllerTransformer.transform(new StreamSource(new StringReader(requestXML)), new SAXResult(pipelineHandler));
      
      SAXTransformerFactory stf = (SAXTransformerFactory) net.sf.saxon.TransformerFactoryImpl.newInstance();      
      stf.setErrorListener(errorListener);
      
      List<PipelineStep> steps = pipelineHandler.getPipelineSteps();
      if (steps == null || steps.isEmpty()) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
        return;
      }
      steps.add(new SystemTransformerStep("system/response.xsl", "client-response"));
                   
      Templates templates = null;
      TransformerHandler nextHandler = null;
      Controller transformer = null;      
      List<OutputStream> debugOutputStreams = null;
      if (isDevelopmentMode) {
        debugOutputStreams = new ArrayList<OutputStream>();
      }
      
      try {
        List<TransformerHandler> handlers = new ArrayList<TransformerHandler>();
        String stepName = null;
        for (int i=0; i<steps.size(); i++) {
          PipelineStep step = steps.get(i);          
          String xslPath = null;
          if (step instanceof SystemTransformerStep) {
            xslPath = new File(homeDir, "xsl/" + ((TransformerStep) step).getXslPath()).getAbsolutePath();                      
          } else if (step instanceof TransformerStep) {          
            xslPath = ((TransformerStep) step).getXslPath();            
          } else if (step instanceof ResponseStep) {
            requestXML = ((ResponseStep) step).getResponse();
            continue;
          }
          templates = webApp.getTemplates(xslPath, errorListener);
          
          nextHandler = stf.newTransformerHandler(templates);
          transformer = (Controller) nextHandler.getTransformer();          
          setPropertyParameters(transformer, webApp);
          setParameters(transformer, webApp.getParameters());
          setParameters(transformer, ((TransformerStep) step).getParameters());          
          transformer.setErrorListener(errorListener);          
          transformer.setMessageEmitter(messageWarner);
          if (!handlers.isEmpty()) {            
            TransformerHandler prevHandler = handlers.get(handlers.size()-1);
            ContentHandler nextContentHandler;
            if (isDevelopmentMode) {
              OutputStream os = new BufferedOutputStream(new FileOutputStream(new File(this.debugDir, stepName + ".xml")));
              debugOutputStreams.add(os);
              nextContentHandler = new DebugContentHandler(nextHandler, os, webApp.getConfiguration(), nextHandler.getTransformer().getOutputProperties()); // TODO             
            } else {
              nextContentHandler = nextHandler;
            }           
            prevHandler.setResult(new SAXResult(nextContentHandler));
          }
          handlers.add(nextHandler);
          stepName = step.getName();
        }
        
        setObjectParameters(transformer, webApp, req, resp);
                
        OutputStream os = (Context.getInstance().isDevelopmentMode()) ? new ByteArrayOutputStream() : resp.getOutputStream();             
        nextHandler.setResult(new StreamResult(os));
        
        Transformer t = stf.newTransformer();
        Properties outputProperties;
        if (handlers.size() > 1) {
          TransformerHandler lastHandler = handlers.get(handlers.size()-2);
          outputProperties = lastHandler.getTransformer().getOutputProperties();
          handlers.get(handlers.size()-1).getTransformer().setOutputProperties(outputProperties);          
        } else {
          outputProperties = new Properties();
          outputProperties.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
          outputProperties.setProperty(OutputKeys.METHOD, "html");
          outputProperties.setProperty(OutputKeys.INDENT, "no");
        }
        t.setOutputProperties(outputProperties);
        t.transform(new StreamSource(new StringReader(requestXML)), new SAXResult(handlers.get(0)));
        
        if (isDevelopmentMode) {
          byte[] body = ((ByteArrayOutputStream) os).toByteArray();
          FileUtils.writeByteArrayToFile(this.responseDebugFile, body);
          IOUtils.copy(new ByteArrayInputStream(body), resp.getOutputStream());
        }
        
      } finally {
        if (debugOutputStreams != null) {
          for (OutputStream os : debugOutputStreams) {
            os.close();
          }
        }        
      }      
    } finally {
      requestSerializer.close();
    }
  }
  
}