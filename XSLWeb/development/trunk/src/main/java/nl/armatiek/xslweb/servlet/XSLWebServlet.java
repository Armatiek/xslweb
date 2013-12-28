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

import net.sf.saxon.Configuration;
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
import nl.armatiek.xslweb.pipeline.SystemTransformerStep;
import nl.armatiek.xslweb.pipeline.TransformerStep;
import nl.armatiek.xslweb.saxon.functions.expath.file.Append;
import nl.armatiek.xslweb.saxon.functions.expath.file.AppendBinary;
import nl.armatiek.xslweb.saxon.functions.expath.file.AppendText;
import nl.armatiek.xslweb.saxon.functions.expath.file.AppendTextLines;
import nl.armatiek.xslweb.saxon.functions.expath.file.BaseName;
import nl.armatiek.xslweb.saxon.functions.expath.file.Copy;
import nl.armatiek.xslweb.saxon.functions.expath.file.CreateDir;
import nl.armatiek.xslweb.saxon.functions.expath.file.Delete;
import nl.armatiek.xslweb.saxon.functions.expath.file.DirName;
import nl.armatiek.xslweb.saxon.functions.expath.file.DirSeparator;
import nl.armatiek.xslweb.saxon.functions.expath.file.Exists;
import nl.armatiek.xslweb.saxon.functions.expath.file.IsDir;
import nl.armatiek.xslweb.saxon.functions.expath.file.IsFile;
import nl.armatiek.xslweb.saxon.functions.expath.file.LastModified;
import nl.armatiek.xslweb.saxon.functions.expath.file.LineSeparator;
import nl.armatiek.xslweb.saxon.functions.expath.file.Move;
import nl.armatiek.xslweb.saxon.functions.expath.file.PathSeparator;
import nl.armatiek.xslweb.saxon.functions.expath.file.PathToNative;
import nl.armatiek.xslweb.saxon.functions.expath.file.PathToURI;
import nl.armatiek.xslweb.saxon.functions.expath.file.ReadBinary;
import nl.armatiek.xslweb.saxon.functions.expath.file.ReadText;
import nl.armatiek.xslweb.saxon.functions.expath.file.ReadTextLines;
import nl.armatiek.xslweb.saxon.functions.expath.file.ResolvePath;
import nl.armatiek.xslweb.saxon.functions.expath.file.Size;
import nl.armatiek.xslweb.saxon.functions.expath.file.Write;
import nl.armatiek.xslweb.saxon.functions.expath.file.WriteBinary;
import nl.armatiek.xslweb.saxon.functions.expath.file.WriteText;
import nl.armatiek.xslweb.saxon.functions.expath.file.WriteTextLines;
import nl.armatiek.xslweb.saxon.functions.log.Log;
import nl.armatiek.xslweb.saxon.functions.response.Cookies;
import nl.armatiek.xslweb.saxon.functions.response.Headers;
import nl.armatiek.xslweb.saxon.functions.response.Session;
import nl.armatiek.xslweb.saxon.functions.response.SetStatus;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.expath.httpclient.saxon.SendRequestFunction;
import org.expath.pkg.saxon.EXPathFunctionDefinition;
import org.expath.zip.saxon.BinaryEntryFunction;
import org.expath.zip.saxon.EntriesFunction;
import org.expath.zip.saxon.HtmlEntryFunction;
import org.expath.zip.saxon.TextEntryFunction;
import org.expath.zip.saxon.UpdateEntriesFunction;
import org.expath.zip.saxon.XmlEntryFunction;
import org.expath.zip.saxon.ZipFileFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;

public class XSLWebServlet extends HttpServlet {
  
  private static final long serialVersionUID = 1L;
  
  private static final Logger logger = LoggerFactory.getLogger(XSLWebServlet.class);
    
  private Configuration configuration;
  private File homeDir;  
  private boolean isDevelopmentMode;
  private File debugDir;
  private File requestDebugFile;
  private File responseDebugFile;
  
  public void init() throws ServletException {
    super.init();
    try {    
      configuration = new Configuration();           
      configuration.setXIncludeAware(true);
      
      /* Log */
      configuration.registerExtensionFunction(new Log());
      
      /* Response */
      configuration.registerExtensionFunction(new SetStatus());
      configuration.registerExtensionFunction(new Headers());
      configuration.registerExtensionFunction(new Session());
      configuration.registerExtensionFunction(new Cookies());
            
      /* EXPath File: */
      configuration.registerExtensionFunction(new Append());
      configuration.registerExtensionFunction(new AppendBinary());
      configuration.registerExtensionFunction(new AppendText());
      configuration.registerExtensionFunction(new AppendTextLines());
      configuration.registerExtensionFunction(new BaseName());
      configuration.registerExtensionFunction(new Copy());
      configuration.registerExtensionFunction(new CreateDir());
      configuration.registerExtensionFunction(new Delete());
      configuration.registerExtensionFunction(new DirName());
      configuration.registerExtensionFunction(new DirSeparator());
      configuration.registerExtensionFunction(new Exists());
      configuration.registerExtensionFunction(new IsDir());
      configuration.registerExtensionFunction(new IsFile());
      configuration.registerExtensionFunction(new LastModified());
      configuration.registerExtensionFunction(new LineSeparator());
      configuration.registerExtensionFunction(new nl.armatiek.xslweb.saxon.functions.expath.file.List());
      configuration.registerExtensionFunction(new Move());
      configuration.registerExtensionFunction(new PathSeparator());
      configuration.registerExtensionFunction(new PathToNative());
      configuration.registerExtensionFunction(new PathToURI());
      configuration.registerExtensionFunction(new ReadBinary());
      configuration.registerExtensionFunction(new ReadText());
      configuration.registerExtensionFunction(new ReadTextLines());
      configuration.registerExtensionFunction(new ResolvePath());
      configuration.registerExtensionFunction(new Size());
      configuration.registerExtensionFunction(new Write());
      configuration.registerExtensionFunction(new WriteBinary());
      configuration.registerExtensionFunction(new WriteText());
      configuration.registerExtensionFunction(new WriteTextLines());

      /* EXPath Zip: */
      registerEXPathFunction(new EntriesFunction(), configuration);
      registerEXPathFunction(new UpdateEntriesFunction(), configuration);
      registerEXPathFunction(new ZipFileFunction(), configuration);
      configuration.registerExtensionFunction(new BinaryEntryFunction());
      configuration.registerExtensionFunction(new HtmlEntryFunction());
      configuration.registerExtensionFunction(new TextEntryFunction());
      configuration.registerExtensionFunction(new XmlEntryFunction());
      
      /* EXPath HttpClient: */           
      registerEXPathFunction(new SendRequestFunction(), configuration);
  
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
  
  private void registerEXPathFunction(EXPathFunctionDefinition function, Configuration configuration) {
    function.setConfiguration(configuration);      
    configuration.registerExtensionFunction(function);
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
      
      Templates requestDispatcherTemplates = webApp.getRequestDispatcherTemplates(errorListener, configuration);    
      Controller controllerTransformer = (Controller) requestDispatcherTemplates.newTransformer();
      setPropertyParameters(controllerTransformer, webApp);
      controllerTransformer.setErrorListener(errorListener);        
      controllerTransformer.setMessageEmitter(messageWarner);            
                               
      PipelineHandler pipelineHandler = new PipelineHandler();
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
          if (step instanceof SystemTransformerStep) {
            templates = TemplatesCache.getTemplates(new File(this.homeDir, "xsl/" + ((TransformerStep) step).getXslPath()).getAbsolutePath(), errorListener, configuration);
          } else {          
            templates = webApp.getTemplates(((TransformerStep) step).getXslPath(), errorListener, configuration);
          }
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
              nextContentHandler = new DebugContentHandler(nextHandler, os, configuration, nextHandler.getTransformer().getOutputProperties()); // TODO             
            } else {
              nextContentHandler = nextHandler;
            }           
            prevHandler.setResult(new SAXResult(nextContentHandler));
          }
          handlers.add(nextHandler);
          stepName = step.getName();
        }
        
        transformer.setParameter("{" + Definitions.NAMESPACEURI_XSLWEB_REQUEST + "}request", req);
        transformer.setParameter("{" + Definitions.NAMESPACEURI_XSLWEB_RESPONSE + "}response", resp);
                
        OutputStream os = (Context.getInstance().isDevelopmentMode()) ? new ByteArrayOutputStream() : resp.getOutputStream();             
        nextHandler.setResult(new StreamResult(os));
                        
        TransformerHandler lastHandler = handlers.get(handlers.size()-2);
                
        Transformer t = stf.newTransformer();
        Properties outputProperties = lastHandler.getTransformer().getOutputProperties();
        handlers.get(handlers.size()-1).getTransformer().setOutputProperties(outputProperties);
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