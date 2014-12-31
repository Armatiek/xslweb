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
import java.util.Date;
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
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SAXDestination;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Serializer.Property;
import net.sf.saxon.s9api.TeeDestination;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.serialize.MessageWarner;
import net.sf.saxon.stax.XMLStreamWriterDestination;
import net.sf.saxon.value.ObjectValue;
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
import nl.armatiek.xslweb.saxon.errrorlistener.TransformationErrorListener;
import nl.armatiek.xslweb.servlet.CleanupXMLStreamWriter;
import nl.armatiek.xslweb.servlet.RequestSerializer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ProxyWriter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XSLWebServlet extends HttpServlet {
  
  private static final long serialVersionUID = 1L;
  
  private static final Logger logger = LoggerFactory.getLogger(XSLWebServlet.class);
    
  private File homeDir;    
  private String lineSeparator;
  
  public void init() throws ServletException {
    super.init();   
    try {                     
      lineSeparator = System.getProperty("line.separator");
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
      String path = StringUtils.defaultString(req.getPathInfo()) + req.getServletPath();      
      WebApp webApp = Context.getInstance().getWebApp(path);
      if (webApp == null) {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else {
        developmentMode = webApp.getDevelopmentMode();        
        Resource resource = webApp.matchesResource(webApp.getRelativePath(path));
        if (resource == null) {                                
          executeRequest(webApp, req, resp, respOs);               
        } else {
          resp.setContentType(resource.getMediaType());
          File file = webApp.getStaticFile(path);
          Date currentDate = new Date();
          long now = currentDate.getTime();
          long duration = resource.getDuration().getTimeInMillis(currentDate);
          resp.addHeader("Cache-Control", "max-age=" + duration / 1000);
          resp.setDateHeader("Expires", now + duration);
          FileUtils.copyFile(file, respOs);
        }
      }
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

  private void setPropertyParameters(XsltTransformer transformer, WebApp webApp) throws IOException {
    Properties props = Context.getInstance().getProperties();
    for (String key : props.stringPropertyNames()) {
      String value = props.getProperty(key);      
      transformer.setParameter(new QName(Definitions.NAMESPACEURI_XSLWEB_CONFIGURATION, key), new XdmAtomicValue(value));
    }    
    transformer.setParameter(new QName(Definitions.NAMESPACEURI_XSLWEB_CONFIGURATION, "home-dir"), new XdmAtomicValue(homeDir.getAbsolutePath()));
    transformer.setParameter(new QName(Definitions.NAMESPACEURI_XSLWEB_CONFIGURATION, "webapp-dir"), new XdmAtomicValue(webApp.getHomeDir().getAbsolutePath()));
  }
  
  private void setParameters(XsltTransformer transformer, List<Parameter> parameters) throws IOException {
    if (parameters == null) {
      return;
    }
    for (Parameter param : parameters) {
      QName qname = (param.getURI() != null) ? new QName(param.getURI(), param.getName()) : new QName(param.getName());                 
      transformer.setParameter(qname, new XdmValue(param.getValue()));                  
    }        
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void setObjectParameters(XsltTransformer transformer, WebApp webApp, HttpServletRequest req, HttpServletResponse resp) throws IOException {            
    transformer.setParameter(new QName(Definitions.NAMESPACEURI_XSLWEB_REQUEST, "request"),  XdmValue.wrap(new ObjectValue(req)));
    transformer.setParameter(new QName(Definitions.NAMESPACEURI_XSLWEB_RESPONSE, "response"),  XdmValue.wrap(new ObjectValue(resp)));
    transformer.setParameter(new QName(Definitions.NAMESPACEURI_XSLWEB_WEBAPP, "webapp"),  XdmValue.wrap(new ObjectValue(webApp)));               
  }
  
  private List<PipelineStep> getPipelineSteps(WebApp webApp, HttpServletRequest req, HttpServletResponse resp, 
      String requestXML, ErrorListener errorListener, MessageWarner messageWarner) throws Exception {
    XsltExecutable templates = webApp.getRequestDispatcherTemplates(errorListener);
    XsltTransformer transformer = templates.load();
                      
    setPropertyParameters(transformer, webApp);
    setObjectParameters(transformer, webApp, req, resp);
    setParameters(transformer, webApp.getParameters());
    transformer.setErrorListener(errorListener);            
    transformer.getUnderlyingController().setMessageEmitter(messageWarner);            
                             
    PipelineHandler pipelineHandler = new PipelineHandler(webApp.getProcessor(), webApp.getConfiguration());
    transformer.setSource(new StreamSource(new StringReader(requestXML)));
    transformer.setDestination(new SAXDestination(pipelineHandler));
    transformer.transform();
    
    return pipelineHandler.getPipelineSteps();
  }
  
  private Destination getDestination(WebApp webApp, Destination destination, PipelineStep step) {
    if (webApp.getDevelopmentMode() && step.getLog()) {
      StringWriter sw = new StringWriter();
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
    RequestSerializer requestSerializer = new RequestSerializer(req, webApp, developmentMode);    
    try {    
      String requestXML = requestSerializer.serializeToXML();      
      if (developmentMode) {
        logger.debug("REQUEST XML:" + lineSeparator + requestXML);                
      }
      
      ErrorListener errorListener = new TransformationErrorListener(resp, developmentMode);      
      MessageWarner messageWarner = new MessageWarner();
      
      List<PipelineStep> steps = getPipelineSteps(webApp, req, resp, requestXML, errorListener, messageWarner);
      if (steps == null || steps.isEmpty()) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
        return;
      }
      
      steps.add(new SystemTransformerStep("system/response/response.xsl", "client-response", true));
                       
      ArrayList<XsltExecutable> executables = new ArrayList<XsltExecutable>();
      ArrayList<XsltTransformer> transformers = new ArrayList<XsltTransformer>();            
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
        XsltExecutable templates = webApp.getTemplates(xslPath, errorListener);        
        XsltTransformer transformer = templates.load();                
        setPropertyParameters(transformer, webApp);
        setObjectParameters(transformer, webApp, req, resp);
        setParameters(transformer, webApp.getParameters());
        setParameters(transformer, ((TransformerStep) step).getParameters()); 
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
      
      Destination destination = getDestination(webApp, new XMLStreamWriterDestination(xsw), steps.get(steps.size()-1));
                  
      lastTransformer.setDestination(destination);
      firstTransformer.setSource(new StreamSource(new StringReader(requestXML)));                 
      firstTransformer.transform();
      
      if (developmentMode) {                       
        byte[] body = ((ByteArrayOutputStream) os).toByteArray();                         
        IOUtils.copy(new ByteArrayInputStream(body), respOs);
      }                  
    } finally {
      requestSerializer.close();
    }
  }
  
}