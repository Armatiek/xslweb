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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ProxyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Serializer.Property;
import net.sf.saxon.s9api.TeeDestination;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.stax.XMLStreamWriterDestination;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.error.XSLWebException;
import nl.armatiek.xslweb.joost.MessageEmitter;
import nl.armatiek.xslweb.pipeline.BinarySerializerStep;
import nl.armatiek.xslweb.pipeline.FopSerializerStep;
import nl.armatiek.xslweb.pipeline.IdentityTransformerStep;
import nl.armatiek.xslweb.pipeline.JSONSerializerStep;
import nl.armatiek.xslweb.pipeline.ParameterizablePipelineStep;
import nl.armatiek.xslweb.pipeline.PipelineHandler;
import nl.armatiek.xslweb.pipeline.PipelineStep;
import nl.armatiek.xslweb.pipeline.QueryStep;
import nl.armatiek.xslweb.pipeline.ResourceSerializerStep;
import nl.armatiek.xslweb.pipeline.ResponseStep;
import nl.armatiek.xslweb.pipeline.SchemaValidatorStep;
import nl.armatiek.xslweb.pipeline.SchematronValidatorStep;
import nl.armatiek.xslweb.pipeline.SerializerStep;
import nl.armatiek.xslweb.pipeline.StylesheetExportFileStep;
import nl.armatiek.xslweb.pipeline.SystemTransformerStep;
import nl.armatiek.xslweb.pipeline.TransformerSTXStep;
import nl.armatiek.xslweb.pipeline.TransformerStep;
import nl.armatiek.xslweb.pipeline.ZipSerializerStep;
import nl.armatiek.xslweb.saxon.debug.DebugUtils;
import nl.armatiek.xslweb.saxon.destination.SourceDestination;
import nl.armatiek.xslweb.saxon.destination.TeeSourceDestination;
import nl.armatiek.xslweb.saxon.destination.XdmSourceDestination;
import nl.armatiek.xslweb.saxon.errrorlistener.TransformationErrorListener;
import nl.armatiek.xslweb.saxon.errrorlistener.ValidatorErrorHandler;
import nl.armatiek.xslweb.saxon.uriresolver.XSLWebURIResolver;
import nl.armatiek.xslweb.saxon.uriresolver.XSLWebURIResolver.DefaultBehaviour;
import nl.armatiek.xslweb.saxon.utils.SaxonUtils;
import nl.armatiek.xslweb.utils.Closeable;
import nl.armatiek.xslweb.utils.XSLWebUtils;
import nl.armatiek.xslweb.xml.CleanupXMLStreamWriter;

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
  
  @SuppressWarnings("unchecked")
  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    OutputStream respOs = resp.getOutputStream();    
    WebApp webApp = null;
    try {            
      webApp = (WebApp) req.getAttribute(Definitions.ATTRNAME_WEBAPP);
      if (webApp.isClosed()) {
        resp.resetBuffer();
        resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        resp.setContentType("text/html; charset=UTF-8");
        Writer w = new OutputStreamWriter(respOs, "UTF-8");
        w.write("<html><body><h1>Service temporarily unavailable</h1></body></html>");
        return;
      }      
      executeRequest(webApp, req, resp, respOs);         
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      if (webApp != null && webApp.getDevelopmentMode()) {              
        resp.setContentType("text/plain; charset=UTF-8");        
        e.printStackTrace(new PrintStream(respOs));        
      } else if (!resp.isCommitted()) {
        resp.resetBuffer();
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setContentType("text/html; charset=UTF-8");
        Writer w = new OutputStreamWriter(respOs, "UTF-8");
        w.write("<html><body><h1>Internal Server Error</h1></body></html>");
      }
    } finally {
      // Delete any registered temporary files:
      try {
        List<File> tempFiles = (List<File>) req.getAttribute(Definitions.ATTRNAME_TEMPFILES);                        
        if (tempFiles != null) {
          ListIterator<File> li = tempFiles.listIterator();
          while(li.hasNext()) {
            File file;
            if ((file = li.next()) != null) {
              FileUtils.deleteQuietly(file);
            }           
          }                    
        }
      } catch (Exception se) {
        logger.error("Error deleting registered temporary files", se);
      }
      
      // Close any closeables:
      try {
        List<Closeable> closeables = (List<Closeable>) req.getAttribute("xslweb-closeables");                        
        if (closeables != null) {
          ListIterator<Closeable> li = closeables.listIterator(closeables.size());
          while(li.hasPrevious()) {
            li.previous().close();
          }                    
        }
      } catch (Exception se) {
        logger.error("Could not close Closeable", se);
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
      if (destination instanceof XdmDestination) {
        destination = new TeeSourceDestination((XdmDestination) destination, debugSerializer);
      } else {
        destination = new TeeDestination(destination, debugSerializer);
      }
    }
    return destination;
  }
  
  private Properties getOutputProperties(WebApp webApp, ErrorListener errorListener, List<PipelineStep> steps) throws Exception {
    for (int i=steps.size()-1; i>=0; i--) {
      PipelineStep step = steps.get(i);
      if (step instanceof TransformerStep) {
        String xslPath = ((TransformerStep) step).getXslPath();
        XsltExecutable executable = webApp.getXsltExecutable(xslPath, errorListener);
        return executable.getUnderlyingCompiledStylesheet().getOutputProperties();
      }
    }
    return null;
  }
  
  private void addResponseTransformationStep(WebApp webApp, List<PipelineStep> steps) {
    SystemTransformerStep step = new SystemTransformerStep("system/response/response.xsl", "client-response", false);
    if (steps.get(steps.size()-1) instanceof SerializerStep) {
      steps.add(steps.size()-1, step);
    } else {
      steps.add(step);
    }
  }
  
  private Destination getDestination(WebApp webApp, HttpServletRequest req, HttpServletResponse resp, 
      OutputStream os, Properties outputProperties, PipelineStep currentStep, PipelineStep nextStep, 
      ErrorListener errorListener) throws Exception {
    Destination dest;
    if (nextStep == null) {      
      Serializer serializer = webApp.getProcessor().newSerializer(os);
      if (outputProperties != null) {
        for (String key : outputProperties.stringPropertyNames()) {
          String value = outputProperties.getProperty(key);
          if (key.equals(OutputKeys.CDATA_SECTION_ELEMENTS)) {
            value = value.replaceAll("\\{\\}", "{''}");
          }
          Property prop = Property.get(key);
          if (prop == null) {
            continue;          
          }
          serializer.setOutputProperty(prop, value);                      
        }
      }
      XMLStreamWriter xsw = new CleanupXMLStreamWriter(serializer.getXMLStreamWriter());
      dest = new XMLStreamWriterDestination(xsw);
    } else if (nextStep instanceof TransformerStep || nextStep instanceof SchemaValidatorStep || nextStep instanceof SchematronValidatorStep) {
      dest = new XdmSourceDestination();
    } else if (nextStep instanceof JSONSerializerStep) {
      dest = ((JSONSerializerStep) nextStep).getDestination(webApp, req, resp, os, outputProperties, errorListener);             
    } else if (nextStep instanceof BinarySerializerStep) {
      dest = ((BinarySerializerStep) nextStep).getDestination(webApp, req, resp, os, outputProperties, errorListener);
    } else if (nextStep instanceof ZipSerializerStep) {
      dest = ((ZipSerializerStep) nextStep).getDestination(webApp, req, resp, os, outputProperties, errorListener);
    } else if (nextStep instanceof ResourceSerializerStep) {
      dest = ((ResourceSerializerStep) nextStep).getDestination(webApp, req, resp, os, outputProperties, errorListener);
    } else if (nextStep instanceof FopSerializerStep) {
      dest = ((FopSerializerStep) nextStep).getDestination(webApp, req, resp, os, outputProperties, errorListener);
    } else if (nextStep instanceof StylesheetExportFileStep) {
      dest = ((StylesheetExportFileStep) nextStep).getDestination(webApp, req, resp, os, outputProperties, errorListener);
    } else {
      throw new XSLWebException("Could not determine destination");
    }
    return getDestination(webApp, dest, currentStep);
  }
  
  private Result getResult(WebApp webApp, HttpServletRequest req, HttpServletResponse resp, 
      OutputStream os, Properties outputProperties, PipelineStep currentStep, PipelineStep nextStep) throws Exception {
    Result result;
    if (nextStep == null) {
      result = new StreamResult(os);
    } else {
      result = new StreamResult(new ByteArrayOutputStream());
    }
    return result;
  }
  
  private Source makeNodeInfoSource(Source source, WebApp webApp, ErrorListener errorListener) throws Exception {
    if (source instanceof NodeInfo) {
      return source;
    }
    XdmDestination dest = new XdmDestination();
    webApp.getIdentityXsltExecutable().load30().applyTemplates(source, dest);
    return dest.getXdmNode().asSource();
  }
  
  private Source makeJAXPSource(Source source) throws Exception {
    if (source instanceof DOMResult || source instanceof StreamResult || source instanceof DOMResult) {
      return source;
    } else if (source instanceof NodeInfo) {
      return new DOMSource(NodeOverNodeInfo.wrap((NodeInfo) source));
    } else {
      throw new XSLWebException("Could not create JAXP Source for Source of class " + source.getClass());
    }
  }
  
  private Map<QName, XdmValue> getStylesheetParameters(ParameterizablePipelineStep step, 
      Map<QName, XdmValue> base, Map<QName, XdmValue> extra) throws IOException {
    Map<QName, XdmValue> stylesheetParameters = new HashMap<QName, XdmValue>();
    stylesheetParameters.putAll(base);
    XSLWebUtils.addStylesheetParameters(stylesheetParameters, step.getParameters());
    if (extra != null) {
      stylesheetParameters.putAll(extra);
      extra.clear();
    }
    return stylesheetParameters;
  }
  
  private Map<String, Object> getStylesheetParametersJAXP(ParameterizablePipelineStep step,
      Map<QName, XdmValue> base, Map<QName, XdmValue> extra) throws IOException {
    Map<QName, XdmValue> params = getStylesheetParameters(step, base, extra);
    Map<String, Object> stylesheetParameters = new HashMap<String, Object>();
    for (Map.Entry<QName, XdmValue> entry : params.entrySet()) {
      QName name = entry.getKey();
      XdmValue value = entry.getValue();
      if (!(value instanceof XdmAtomicValue))
        continue;
      stylesheetParameters.put(name.getClarkName(), ((XdmAtomicValue) value).getValue());
    }
    return stylesheetParameters;
  }
  
  private void preprocessPipelines(WebApp webApp, List<PipelineStep> steps) {
    for (int i=0; i<steps.size(); i++) {
      PipelineStep step = steps.get(i);
      if (step instanceof TransformerSTXStep && i<steps.size()) {
        /* Add extra identity tranformation after STX transformation that is not the 
         * last transformation in the pipeline: */
        steps.add(i+1, new IdentityTransformerStep());
      }
    } 
  }
  
  private void executeRequest(WebApp webApp, HttpServletRequest req, HttpServletResponse resp, 
      OutputStream respOs) throws Exception {        
    boolean developmentMode = webApp.getDevelopmentMode();               
    
    Source source = (NodeInfo) req.getAttribute(Definitions.ATTRNAME_REQUESTXML);
    
    PipelineHandler pipelineHandler = (PipelineHandler) req.getAttribute(Definitions.ATTRNAME_PIPELINEHANDLER);
         
    TransformationErrorListener errorListener = new TransformationErrorListener(resp, developmentMode);      
    
    List<PipelineStep> steps = pipelineHandler.getPipelineSteps();
    if (steps == null || steps.isEmpty()) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
      return;
    }
    preprocessPipelines(webApp, steps);
    
    OutputStream os = (developmentMode) ? new ByteArrayOutputStream() : respOs;
    
    Properties outputProperties = getOutputProperties(webApp, errorListener, steps);
    
    Map<QName, XdmValue> baseStylesheetParameters = XSLWebUtils.getStylesheetParameters(webApp, req, resp, homeDir);
    
    addResponseTransformationStep(webApp, steps);
    
    Map<QName, XdmValue> extraStylesheetParameters = null;
    Destination destination = null;
    
    for (int i=0; i<steps.size(); i++) {
      PipelineStep step = steps.get(i);
      if (step instanceof SerializerStep) {
        break;
      }
      PipelineStep nextStep = (i<steps.size()-1) ? steps.get(i+1) : null;
      if (step instanceof TransformerStep) {
        String xslPath = null;
        if (step instanceof SystemTransformerStep) {
          xslPath = new File(homeDir, "common/xsl/" + ((TransformerStep) step).getXslPath()).getAbsolutePath();                      
        } else {          
          xslPath = ((TransformerStep) step).getXslPath();
        }
        XsltExecutable templates = webApp.getXsltExecutable(xslPath, errorListener); 
        Xslt30Transformer transformer = templates.load30();
        SaxonUtils.setMessageEmitter(transformer.getUnderlyingController(), webApp.getConfiguration(), errorListener);
        transformer.setErrorListener(errorListener);
        DebugUtils.setDebugTraceListener(webApp, req, transformer);
        Map<QName, XdmValue> stylesheetParameters = getStylesheetParameters((ParameterizablePipelineStep) step, 
            baseStylesheetParameters, extraStylesheetParameters);
        transformer.setStylesheetParameters(stylesheetParameters);
        destination = getDestination(webApp, req, resp, os, outputProperties, step, nextStep, errorListener);
        NodeInfo nodeInfo = (NodeInfo) makeNodeInfoSource(source, webApp, errorListener);
        transformer.setGlobalContextItem(new XdmNode(nodeInfo));
        transformer.setURIResolver(new XSLWebURIResolver(DefaultBehaviour.SAXON, req));
        transformer.applyTemplates(nodeInfo, destination);
      } else if (step instanceof QueryStep) {
        String xqueryPath = ((QueryStep) step).getXQueryPath();
        XQueryExecutable xquery = webApp.getQuery(xqueryPath, errorListener);
        XQueryEvaluator eval = xquery.load();
        eval.setErrorListener(errorListener);
        DebugUtils.setDebugTraceListener(webApp,  req, eval);
        Map<QName, XdmValue> stylesheetParameters = getStylesheetParameters((ParameterizablePipelineStep) step, 
            baseStylesheetParameters, extraStylesheetParameters);
        for (Map.Entry<QName, XdmValue> entry : stylesheetParameters.entrySet()) {
          eval.setExternalVariable(entry.getKey(), entry.getValue());
        }
        destination = getDestination(webApp, req, resp, os, outputProperties, step, nextStep, errorListener);
        NodeInfo nodeInfo = (NodeInfo) makeNodeInfoSource(source, webApp, errorListener);
        eval.setContextItem(new XdmNode(nodeInfo));
        eval.run(destination);
      } else if (step instanceof TransformerSTXStep) {
        String stxPath = ((TransformerSTXStep) step).getStxPath();
        Templates templates = webApp.getTemplates(stxPath, errorListener);
        Transformer transformer = templates.newTransformer();
        transformer.setErrorListener(errorListener);
        transformer.setURIResolver(new XSLWebURIResolver(DefaultBehaviour.STREAM, req));
        ((net.sf.joost.trax.TransformerImpl)transformer).getStxProcessor().setMessageEmitter(new MessageEmitter());
        Map<String, Object> stylesheetParameters = getStylesheetParametersJAXP((ParameterizablePipelineStep) step, 
            baseStylesheetParameters, extraStylesheetParameters);
        for (Map.Entry<String, Object> entry : stylesheetParameters.entrySet()) {
          transformer.setParameter(entry.getKey(), entry.getValue());
        }
        source = makeJAXPSource(source);
        Result result = getResult(webApp, req, resp, os, outputProperties, step, nextStep);
        transformer.transform(source, result);
        if (nextStep != null) {
          ByteArrayOutputStream baos = (ByteArrayOutputStream) ((StreamResult) result).getOutputStream();
          source = new StreamSource(new ByteArrayInputStream(baos.toByteArray()));
        }
      } else if (step instanceof SchemaValidatorStep) {
        SchemaValidatorStep svStep = (SchemaValidatorStep) step;
        List<String> schemaPaths = svStep.getSchemaPaths();
        Schema schema = webApp.getSchema(schemaPaths, errorListener);
        
        source = makeNodeInfoSource(source, webApp, errorListener);
        destination = null;
        
        Serializer serializer = webApp.getProcessor().newSerializer();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.setOutputStream(outputStream);
        serializer.setOutputProperty(Property.INDENT, "yes");
        serializer.serializeNode(new XdmNode((NodeInfo) source));
        Source validationSource = new StreamSource(new ByteArrayInputStream(outputStream.toByteArray()));
        
        Validator validator = schema.newValidator();
        ValidatorErrorHandler errorHandler = new ValidatorErrorHandler("Step: " + ((step.getName() != null) ? step.getName() : "noname"));
        validator.setErrorHandler(errorHandler);
        
        Properties properties = svStep.getProperties();
        if (properties != null) {
          @SuppressWarnings("rawtypes")
          Enumeration names = properties.propertyNames();
          while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            validator.setProperty(name, properties.getProperty(name));
          }
        }
        
        Properties features = svStep.getProperties();
        if (features != null) {
          @SuppressWarnings("rawtypes")
          Enumeration names = features.propertyNames();
          while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            validator.setProperty(name, features.getProperty(name));
          }
        }
        
        validator.validate(validationSource);
        
        Source resultsSource = errorHandler.getValidationResults();
        if (resultsSource != null) {
          NodeInfo resultsNodeInfo = webApp.getConfiguration().buildDocumentTree(resultsSource).getRootNode();
          String xslParamName = svStep.getXslParamName();
          if (xslParamName != null) {
            if (extraStylesheetParameters == null) {
              extraStylesheetParameters = new HashMap<QName, XdmValue>();
            }
            extraStylesheetParameters.put(new QName(svStep.getXslParamNamespace(), xslParamName), 
                new XdmNode(resultsNodeInfo));
          }
        }
      } else if (step instanceof SchematronValidatorStep) {
        SchematronValidatorStep svStep = (SchematronValidatorStep) step;
        
        source = makeNodeInfoSource(source, webApp, errorListener);
        destination = null;
       
        /* Execute schematron validation */
        XsltExecutable templates = webApp.getSchematron(svStep.getSchematronPath(), svStep.getPhase(), errorListener); 
        Xslt30Transformer transformer = templates.load30();
        SaxonUtils.setMessageEmitter(transformer.getUnderlyingController(), webApp.getConfiguration(), errorListener);
        transformer.setErrorListener(errorListener);    
        XdmDestination svrlDest = new XdmDestination();
    
        transformer.setGlobalContextItem(new XdmNode((NodeInfo) source)); 
        transformer.applyTemplates(source, svrlDest);
        
        String xslParamName = svStep.getXslParamName();
        if (xslParamName != null) {
          if (extraStylesheetParameters == null) {
            extraStylesheetParameters = new HashMap<QName, XdmValue>();
          }
          extraStylesheetParameters.put(new QName(svStep.getXslParamNamespace(), xslParamName), 
              svrlDest.getXdmNode());
        }
      } else if (step instanceof ResponseStep) {
        source = new StreamSource(new StringReader(((ResponseStep) step).getResponse()));
        continue;
      }
      
      if (destination instanceof SourceDestination) {
        /* Set source for next pipeline step: */
        source = ((SourceDestination) destination).asSource();
      }
    }
    
    if (developmentMode) {
      byte[] body = ((ByteArrayOutputStream) os).toByteArray();                         
      IOUtils.copy(new ByteArrayInputStream(body), respOs);
    }
    
  }
  
}