package nl.armatiek.xslweb.serializer;

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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;

/**
 * 
 * @author Maarten Kroon
 */
public class FopSerializer extends AbstractSerializer {
  
  protected static final Logger logger = LoggerFactory.getLogger(FopSerializer.class);
  
  private ContentHandler serializingHandler;
  private ByteArrayOutputStream nsos = null;
  private boolean exceptionThrown = false;
  
  public FopSerializer(WebApp webApp, HttpServletRequest req, HttpServletResponse resp, OutputStream os) {    
    super(webApp, req, resp, os);      
  }
  
  public FopSerializer(WebApp webApp) {
    super(webApp);
  }
  
  @Override
  public void close() throws IOException {
    if (nsos != null) {
      if (!exceptionThrown)
        IOUtils.copy(new ByteArrayInputStream(nsos.toByteArray()), os);
      IOUtils.closeQuietly(nsos);
    }
    IOUtils.closeQuietly(os);
  }
  
  @SuppressWarnings("unchecked")
  private void processFopSerializer(String uri, String localName, String qName, Attributes attributes) throws Exception {    
    try {
      String path = attributes.getValue("", "path");               
      if (path == null) {
        /* Write to HTTP response: */
        if (resp == null) {
          throw new SAXException("No attribute \"path\" specified on fop-serializer element");
        }             
      } else {
        /* Write to file: */
        File outputFile = new File(path);
        File parentDir = outputFile.getParentFile();
        if (!parentDir.isDirectory()) {
          if (!parentDir.mkdirs()) {
            throw new SAXException("Could not create directory \"" + parentDir.getAbsolutePath() + "\"");
          }
        }
        
        if (outputFile.exists()) {
          if (outputFile.isDirectory()) {
            throw new SAXException("File \"" + parentDir.getAbsolutePath() + "\" already exists as directory");
          } else if (!outputFile.delete()) {
            throw new SAXException("Could not delete output file \"" + outputFile.getAbsolutePath() + "\"");
          }
        }                
        this.os = new BufferedOutputStream(new FileOutputStream(outputFile));      
      }    
      String configName = attributes.getValue("config-name");
      if (configName == null) {
        throw new SAXException("No attribute \"config-name\" specified on fop-serializer element");
      }        
      FopFactory fopFactory = webApp.getFopFactory(configName);
      FOUserAgent userAgent = fopFactory.newFOUserAgent();
      String mode = attributes.getValue("pdf-a-mode");        
      if (mode != null) {
        userAgent.getRendererOptions().put("pdf-a-mode", mode);
      }
      String outputFormat = attributes.getValue("output-format");
      boolean nonStreaming = StringUtils.equals(attributes.getValue("non-streaming"), "true"); 
      nsos = (nonStreaming) ? new ByteArrayOutputStream() : null;
      Fop fop = fopFactory.newFop(outputFormat == null ? MimeConstants.MIME_PDF : outputFormat, 
          userAgent, (nsos != null) ? nsos : os);
      this.serializingHandler = fop.getDefaultHandler();
    } catch (Exception e) {
      exceptionThrown = true;
      throw e;
    }
  }
  
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {        
    try {
      if (altHandler != null) {
        altHandler.startElement(uri, localName, qName, attributes);
        return;
      }    
      if (serializingHandler != null) {
        serializingHandler.startElement(uri, localName, qName, attributes);
        return;
      }
      if (!StringUtils.equals(uri, Definitions.NAMESPACEURI_XSLWEB_FOP_SERIALIZER)) {
        getAltHandler().startElement(uri, localName, qName, attributes);     
      }
      if (StringUtils.equals(localName, "fop-serializer")) {
        processFopSerializer(uri, localName, qName, attributes);
        this.serializingHandler.startDocument();
      }
    } catch (SAXException se) {
      exceptionThrown = true;
      throw se;
    } catch (Exception e) {
      exceptionThrown = true;
      throw new SAXException(e);
    }
  }
  
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {        
    try {
      if (altHandler != null) {
        altHandler.endElement(uri, localName, qName);
        return;
      }
      boolean isFopSerializer = StringUtils.equals(uri, Definitions.NAMESPACEURI_XSLWEB_FOP_SERIALIZER) && 
          StringUtils.equals(localName, "fop-serializer");    
      if (isFopSerializer) {        
        this.serializingHandler.endDocument();
        this.serializingHandler = null;        
      } else if (serializingHandler != null) {
        serializingHandler.endElement(uri, localName, qName);      
      }
    } catch (SAXException se) {
      exceptionThrown = true;
      throw se;
    } catch (Exception e) {
      exceptionThrown = true;
      throw new SAXException(e);
    }        
  }
  
  @Override
  public void endDocument() throws SAXException {
    try {
      if (altHandler != null) {
        altHandler.endDocument();;
        return;
      }
      close();
    } catch (SAXException se) {
      exceptionThrown = true;
      throw se;
    } catch (IOException ioe) {
      exceptionThrown = true;
      throw new SAXException("Could not close OutputStream", ioe);
    }
  }
  
  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    try {
      if (altHandler != null) {
        altHandler.characters(ch, start, length);      
      } else if (serializingHandler != null) {
        serializingHandler.characters(ch, start, length);      
      } 
    } catch (SAXException se) {
      exceptionThrown = true;
      throw se;
    }
  }
  
  @Override
  public void endPrefixMapping(String prefix) throws SAXException {
    try { 
      if (altHandler != null) {
        altHandler.endDocument();;      
      } else if (serializingHandler != null) {
        serializingHandler.endPrefixMapping(prefix);
      }
    } catch (SAXException se) {
      exceptionThrown = true;
      throw se;
    }
    
  }
  
  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {        
    try {
      if (altHandler != null) {
        altHandler.ignorableWhitespace(ch, start, length);      
      } else if (serializingHandler != null) {
        serializingHandler.ignorableWhitespace(ch, start, length);
      }
    } catch (SAXException se) {
      exceptionThrown = true;
      throw se;
    }
    
  }
  
  @Override
  public void processingInstruction(String target, String data) throws SAXException {
    try {
      if (altHandler != null) {
        altHandler.processingInstruction(target, data);      
      } else if (serializingHandler != null) {
        serializingHandler.processingInstruction(target, data);
      }
    } catch (SAXException se) {
      exceptionThrown = true;
      throw se;
    }
  }
  
  @Override
  public void setDocumentLocator(Locator locator) { 
    if (altHandler != null) {
      altHandler.setDocumentLocator(locator);      
    } else if (serializingHandler != null) {
      serializingHandler.setDocumentLocator(locator);
    }    
  }
  
  @Override
  public void skippedEntity(String name) throws SAXException {
    try {
      if (altHandler != null) {
        altHandler.skippedEntity(name);      
      } else if (serializingHandler != null) {
        serializingHandler.skippedEntity(name);
      }
    } catch (SAXException se) {
      exceptionThrown = true;
      throw se;
    }
  }
  
  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    try {
      if (altHandler != null) {
        altHandler.startPrefixMapping(prefix, uri);      
      } else if (serializingHandler != null) {
        serializingHandler.startPrefixMapping(prefix, uri);
      }
    } catch (SAXException se) {
      exceptionThrown = true;
      throw se;
    }
  }

  @Override
  public void warning(SAXParseException e) throws SAXException {
    logger.warn(e.getMessage(), e);
  }
  
  @Override
  public void error(SAXParseException e) throws SAXException {
    logger.error(e.getMessage(), e);
  }
  
  @Override
  public void fatalError(SAXParseException e) throws SAXException {
    logger.error(e.getMessage(), e);
  }
  
}