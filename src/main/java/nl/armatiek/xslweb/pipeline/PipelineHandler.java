package nl.armatiek.xslweb.pipeline;

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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import javax.xml.transform.OutputKeys;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.Parameter;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.xml.SerializingContentHandler;

public class PipelineHandler implements ContentHandler {
  
  private Stack<PipelineStep> pipelineSteps = new Stack<PipelineStep>();
  private boolean cache;
  private String cacheKey;
  private int cacheTimeToLive = 60;
  private int cacheTimeToIdle = 60;
  private String cacheScope;
  private boolean cacheHeaders;
  private SerializingContentHandler serializingHandler;
  private OutputStream os;
  private StringBuilder chars = new StringBuilder();
  private Processor processor;
  private Configuration conf;
  private WebApp webApp;
  
  public PipelineHandler(WebApp webApp) {
    this.webApp = webApp;
    this.processor = webApp.getProcessor();
    this.conf = webApp.getConfiguration();
  }
    
  @Override
  public void characters(char[] ch, int start, int len) throws SAXException { 
    chars.append(ch, start, len);
    if (serializingHandler != null) {
      serializingHandler.characters(ch, start, len);
    }
  }

  @Override
  public void endDocument() throws SAXException {
    if (serializingHandler != null) {
      serializingHandler.endDocument();
    }    
  }
  
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException { 
    try {
      if (serializingHandler != null) {
        serializingHandler.endElement(uri, localName, qName);
      }    
      if (StringUtils.equals(uri, Definitions.NAMESPACEURI_XSLWEB_PIPELINE)) {
        if (localName.equals("value")) { 
          ((TransformerStep) pipelineSteps.peek()).getParameters().peek().addValue(chars.toString());
        } else if (localName.equals("schema-path")) {
          ((SchemaValidatorStep) pipelineSteps.peek()).addSchemaPath(chars.toString());
        }
      } else if (StringUtils.equals(uri, Definitions.NAMESPACEURI_XSLWEB_RESPONSE)) {
        if (localName.equals("response")) {
          serializingHandler.close();
          String response = IOUtils.toString(((ByteArrayOutputStream)os).toByteArray(), "UTF-8");
          pipelineSteps.add(new ResponseStep(response, "response", false));
          serializingHandler = null;                   
        }
      }
      chars.setLength(0);
    } catch (Exception e) {
      throw new SAXException(e);
    }
  }

  @Override
  public void endPrefixMapping(String prefix) throws SAXException { 
    if (serializingHandler != null) {
      serializingHandler.endPrefixMapping(prefix);
    }
  }

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException { 
    if (serializingHandler != null) {
      serializingHandler.ignorableWhitespace(ch, start, length);
    }
  }

  @Override
  public void processingInstruction(String target, String data) throws SAXException { 
    if (serializingHandler != null) {
      serializingHandler.processingInstruction(target, data);
    }
  }

  @Override
  public void setDocumentLocator(Locator locator) { 
    if (serializingHandler != null) {
      serializingHandler.setDocumentLocator(locator);
    }
  }

  @Override
  public void skippedEntity(String name) throws SAXException { 
    if (serializingHandler != null) {
      serializingHandler.skippedEntity(name);
    }
  }

  @Override
  public void startDocument() throws SAXException { 
    if (serializingHandler != null) {
      serializingHandler.startDocument();
    }
  }
  
  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {    
    if (serializingHandler != null) {
      serializingHandler.startElement(uri, localName, qName, atts);
    }    
    try {
      if (StringUtils.equals(uri, Definitions.NAMESPACEURI_XSLWEB_PIPELINE)) {
        if (localName.equals("transformer")) {         
          String xslPath = getAttribute(atts, "xsl-path", null);
          if (StringUtils.isBlank(xslPath)) {
            throw new SAXException("Transformer step must have an attribute \"xsl-path\"");
          }
          String name = getAttribute(atts, "name", "transformer-" + Integer.toString(pipelineSteps.size()+1));
          boolean log = getAttribute(atts, "log", "false").equals("true");
          pipelineSteps.add(new TransformerStep(webApp, xslPath, name, log));
        } else if (localName.equals("query")) {         
          String xqueryPath = getAttribute(atts, "xquery-path", null);
          if (StringUtils.isBlank(xqueryPath)) {
            throw new SAXException("Query step must have an attribute \"xquery-path\"");
          }
          String name = getAttribute(atts, "name", "query-" + Integer.toString(pipelineSteps.size()+1));
          boolean log = getAttribute(atts, "log", "false").equals("true");
          pipelineSteps.add(new QueryStep(webApp, xqueryPath, name, log));
        } else if (localName.equals("transformer-stx")) {         
          String stxPath = getAttribute(atts, "stx-path", null);
          if (StringUtils.isBlank(stxPath)) {
            throw new SAXException("Transformer STX step must have an attribute \"stx-path\"");
          }
          String name = getAttribute(atts, "name", "transformer-stx-" + Integer.toString(pipelineSteps.size()+1));
          boolean log = getAttribute(atts, "log", "false").equals("true");
          pipelineSteps.add(new TransformerSTXStep(stxPath, name, log));
        } else if (localName.equals("parameter")) {
          ParameterizablePipelineStep step = (ParameterizablePipelineStep) pipelineSteps.peek();
          String name = getAttribute(atts, "name", null);
          if (StringUtils.isBlank(name)) {
            throw new SAXException("Element \"parameter\" must have an attribute \"name\"");
          }                 
          Parameter parameter = new Parameter(
              processor,
              getAttribute(atts, "uri", null),
              name,
              getAttribute(atts, "type", "xs:string"));                
          step.addParameter(parameter);
        } else if (localName.equals("schema-validator")) {
          String name = getAttribute(atts, "name", "validator-" + Integer.toString(pipelineSteps.size()+1));
          boolean log = getAttribute(atts, "log", "false").equals("true");
          String xslParamName = getAttribute(atts, "xsl-param-name", null);
          String xslParamNamespace = getAttribute(atts, "xsl-param-namespace", null);
          pipelineSteps.add(new SchemaValidatorStep(name, log, xslParamNamespace, xslParamName));
        } else if (localName.equals("properties")) {
        } else if (localName.equals("property")) {
          ((SchemaValidatorStep) pipelineSteps.peek()).addProperty(getAttribute(atts, "name", null), getAttribute(atts, "value", null));
        } else if (localName.equals("features")) {
        } else if (localName.equals("feature")) {
          ((SchemaValidatorStep) pipelineSteps.peek()).addFeature(getAttribute(atts, "name", null), getAttribute(atts, "value", null));
        } else if (localName.equals("schematron-validator")) {
          String name = getAttribute(atts, "name", "validator-" + Integer.toString(pipelineSteps.size()+1));
          boolean log = getAttribute(atts, "log", "false").equals("true");
          String schematronPath = getAttribute(atts, "schematron-path", null);
          String phase = getAttribute(atts, "phase", null);
          String xslParamName = getAttribute(atts, "xsl-param-name", null);
          String xslParamNamespace = getAttribute(atts, "xsl-param-namespace", null);
          pipelineSteps.add(new SchematronValidatorStep(name, schematronPath, log, xslParamNamespace, xslParamName, phase));
        } else if (localName.equals("stylesheet-export-file")) {
          String xslPath = getAttribute(atts, "xsl-path", null);
          if (StringUtils.isBlank(xslPath)) {
            throw new SAXException("stylesheet-export-file step must have an attribute \"xsl-path\"");
          }
          String name = getAttribute(atts, "name", "stylesheet-export-file-" + Integer.toString(pipelineSteps.size()+1));
          boolean log = getAttribute(atts, "log", "false").equals("true");
          pipelineSteps.add(new StylesheetExportFileStep(xslPath, name, log));
        } else if (localName.equals("schema-path")) {
        } else if (localName.equals("schema-paths")) {
        } else if (localName.equals("pipeline")) {
          cache = getAttribute(atts, "cache", "false").equals("true");
          if (cache) {
            cacheKey = getAttribute(atts, "cache-key", null);          
            cacheTimeToLive = Integer.parseInt(getAttribute(atts, "cache-time-to-live", "60"));
            cacheTimeToIdle = Integer.parseInt(getAttribute(atts, "cache-time-to-idle", "60"));
            cacheScope = getAttribute(atts, "cache-scope", "webapp");
            cacheHeaders = getAttribute(atts, "cache-headers", "false").equals("true");
          }
        } else if (localName.equals("json-serializer")) {                                      
          JSONSerializerStep step = new JSONSerializerStep(atts);
          pipelineSteps.add(step);
        } else if (localName.equals("namespace-declarations")) {
        } else if (localName.equals("namespace-declaration")) {
          ((JSONSerializerStep) pipelineSteps.peek()).addNamespaceDeclaration(getAttribute(atts, "namespace-uri", null), getAttribute(atts, "name", null));
        } else if (localName.equals("zip-serializer")) {                                       
          ZipSerializerStep step = new ZipSerializerStep(atts);
          pipelineSteps.add(step);
        } else if (localName.equals("resource-serializer")) {
          ResourceSerializerStep step = new ResourceSerializerStep(atts);
          pipelineSteps.add(step);
        } else if (localName.equals("fop-serializer")) {
          FopSerializerStep step = new FopSerializerStep(atts);
          pipelineSteps.add(step);
        } else if (localName.equals("value")) {          
        } else {
          throw new SAXException(String.format("Pipeline element \"%s\" not supported", localName));
        }
      } else if (StringUtils.equals(uri, Definitions.NAMESPACEURI_XSLWEB_RESPONSE)) {
        if (localName.equals("response")) {
          Properties props = new Properties();
          props.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
          props.setProperty(OutputKeys.METHOD, "xml");
          props.setProperty(OutputKeys.INDENT, "no");     
          os = new ByteArrayOutputStream();
          serializingHandler = new SerializingContentHandler(os, conf, props);
          serializingHandler.startElement(uri, localName, qName, atts);
        }
      }
    } catch (Exception e) {
      throw new SAXException(e);
    }
  }

  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException { 
    if (serializingHandler != null) {
      serializingHandler.startPrefixMapping(prefix, uri);
    }
  }
  
  public List<PipelineStep> getPipelineSteps() {
    return this.pipelineSteps;
  }
  
  public boolean getCache() {
    return cache;
  }
  
  public String getCacheKey() {
    return cacheKey;
  }
  
  public int getCacheTimeToLive() {
    return cacheTimeToLive;
  }
  
  public int getCacheTimeToIdle() {
    return cacheTimeToIdle;
  }
  
  public String getCacheScope() {
    return cacheScope;
  }
  
  public boolean getCacheHeaders() {
    return cacheHeaders;
  }
  
  private String getAttribute(Attributes attr, String name, String defaultValue) {
    int index = -1;
    return ((index = attr.getIndex(name)) >= 0) ? attr.getValue(index) : defaultValue;
  }
  
}