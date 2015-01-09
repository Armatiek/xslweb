package nl.armatiek.xslweb.web.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Serializer.Property;
import net.sf.saxon.s9api.TeeDestination;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.serialize.MessageWarner;
import net.sf.saxon.stax.XMLStreamWriterDestination;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.pipeline.JSONSerializerStep;
import nl.armatiek.xslweb.pipeline.PipelineHandler;
import nl.armatiek.xslweb.pipeline.PipelineStep;
import nl.armatiek.xslweb.pipeline.ResponseStep;
import nl.armatiek.xslweb.pipeline.SerializerStep;
import nl.armatiek.xslweb.pipeline.SystemTransformerStep;
import nl.armatiek.xslweb.pipeline.TransformerStep;
import nl.armatiek.xslweb.saxon.errrorlistener.TransformationErrorListener;
import nl.armatiek.xslweb.utils.XSLWebUtils;
import nl.armatiek.xslweb.xml.CleanupXMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ProxyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XSLWebServlet extends HttpServlet {
  
  private static final long serialVersionUID = 1L;
  
  private static final Logger logger = LoggerFactory.getLogger(XSLWebServlet.class);
    
  private File homeDir;    
  
  public void init() throws ServletException {    
    super.init();   
    try {                        
      homeDir = Context.getInstance().getHomeDir();      
    } catch (Exception e) {
      logger.error(e.getMessage());
      throw new ServletException(e);
    }
  }
  
  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    OutputStream respOs = resp.getOutputStream();
    boolean developmentMode = true;
    try {            
      WebApp webApp = (WebApp) req.getAttribute(Definitions.ATTRNAME_WEBAPP);
      executeRequest(webApp, req, resp, respOs);                 
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      if (developmentMode) {              
        resp.setContentType("text/plain; charset=UTF-8");        
        e.printStackTrace(new PrintStream(respOs));        
      } else if (!resp.isCommitted()) {
        resp.resetBuffer();
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setContentType("text/html; charset=UTF-8");
        Writer w = new OutputStreamWriter(respOs, "UTF-8");
        w.write("<html><body><h1>Internal Server Error</h1></body></html>");
      }
    }      
  }

  private Destination getDestination(WebApp webApp, Destination destination, PipelineStep step) {
    if (webApp.getDevelopmentMode() && step.getLog()) {
      StringWriter sw = new StringWriter();
      sw.write("----------\n");
      sw.write("OUTPUT OF STEP: \"" + step.getName() + "\":\n");            
      Serializer debugSerializer = webApp.getProcessor().newSerializer(new ProxyWriter(sw) {
        @Override
        public void flush() throws IOException {        
          logger.debug(out.toString());                
        }
      });                
      debugSerializer.setOutputProperty(Serializer.Property.METHOD, "xml");
      debugSerializer.setOutputProperty(Serializer.Property.INDENT, "yes");           
      destination = new TeeDestination(destination, debugSerializer);
    }
    return destination;
  }
  
  private void executeRequest(WebApp webApp, HttpServletRequest req, HttpServletResponse resp, 
      OutputStream respOs) throws Exception {        
    boolean developmentMode = webApp.getDevelopmentMode();               
    
    String requestXML = (String) req.getAttribute(Definitions.ATTRNAME_REQUESTXML);
    
    PipelineHandler pipelineHandler = (PipelineHandler) req.getAttribute(Definitions.ATTRNAME_PIPELINEHANDLER);
         
    ErrorListener errorListener = new TransformationErrorListener(resp, developmentMode);      
    MessageWarner messageWarner = new MessageWarner();
    
    List<PipelineStep> steps = pipelineHandler.getPipelineSteps();
    if (steps == null || steps.isEmpty()) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
      return;
    }
    
    steps.add(new SystemTransformerStep("system/response/response.xsl", "client-response", false));
                     
    ArrayList<XsltExecutable> executables = new ArrayList<XsltExecutable>();
    ArrayList<XsltTransformer> transformers = new ArrayList<XsltTransformer>();
    SerializerStep serializerStep = null;
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
      } else if (step instanceof SerializerStep) {
        serializerStep = (SerializerStep) step;
        continue;
      }
      XsltExecutable templates = webApp.getTemplates(xslPath, errorListener);       
      XsltTransformer transformer = templates.load();
      transformer.getUnderlyingController().setMessageEmitter(messageWarner);
      XSLWebUtils.setPropertyParameters(transformer, webApp, homeDir);
      XSLWebUtils.setObjectParameters(transformer, webApp, req, resp);
      XSLWebUtils.setParameters(transformer, webApp.getParameters());
      XSLWebUtils.setParameters(transformer, ((TransformerStep) step).getParameters()); 
      transformer.setErrorListener(errorListener);
      if (!transformers.isEmpty()) {                                        
        PipelineStep prevStep = steps.get(i-1);
        Destination destination = getDestination(webApp, transformer, prevStep);
        transformers.get(transformers.size()-1).setDestination(destination);
      }                
      transformers.add(transformer);
      executables.add(templates);
    }
    
    XsltTransformer firstTransformer = transformers.get(0);      
    XsltTransformer lastTransformer = transformers.get(transformers.size()-1);
    
    Properties outputProperties = null;
    if (executables.size() >= 2) {
      XsltExecutable lastUserExecutable = executables.get(executables.size()-2);
      outputProperties = lastUserExecutable.getUnderlyingCompiledStylesheet().getOutputProperties();      
    }
          
    OutputStream os = (developmentMode) ? new ByteArrayOutputStream() : respOs;
    
    Destination dest = null;
    if (serializerStep == null) {      
      Serializer serializer = webApp.getProcessor().newSerializer(os);
      if (outputProperties != null) {
        for (String key : outputProperties.stringPropertyNames()) {
          String value = outputProperties.getProperty(key);
          Property prop = Property.get(key);
          if (prop == null) {
            continue;          
          }
          serializer.setOutputProperty(prop, value);                      
        }
      }
      XMLStreamWriter xsw = new CleanupXMLStreamWriter(serializer.getXMLStreamWriter());
      dest = new XMLStreamWriterDestination(xsw);
    } else if (serializerStep instanceof JSONSerializerStep) {
      XMLStreamWriter xsw = ((JSONSerializerStep) serializerStep).getWriter(os, outputProperties.getProperty("encoding", "UTF-8"));
      dest = new XMLStreamWriterDestination(xsw);        
    }
    
    Destination destination = getDestination(webApp, dest, steps.get(steps.size()-1));
                
    lastTransformer.setDestination(destination);
    firstTransformer.setSource(new StreamSource(new StringReader(requestXML)));                 
    firstTransformer.transform();
    
    if (developmentMode) {                       
      byte[] body = ((ByteArrayOutputStream) os).toByteArray();                         
      IOUtils.copy(new ByteArrayInputStream(body), respOs);
    }                      
  }
  
}