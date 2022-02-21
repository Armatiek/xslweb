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
package nl.armatiek.xslweb.serializer;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;

/**
 * 
 * @author Maarten Kroon
 */
public class BinarySerializer extends AbstractSerializer {
  
  protected static final Logger logger = LoggerFactory.getLogger(BinarySerializer.class);
  
  private static final String CONTENT_DISPOSITION_HEADER = "%s;filename=\"%2$s\"; filename*=UTF-8''%2$s";
  
  private OutputStream bos;    
  private StringBuilder sb;
  private static final URLCodec codec = new URLCodec();
  
  public BinarySerializer(WebApp webApp, HttpServletRequest req, HttpServletResponse resp, OutputStream os) {    
    super(webApp, req, resp, os);       
  }
  
  public BinarySerializer(WebApp webApp) {
    this(webApp, null, null, null);    
  }
  
  @SuppressWarnings("deprecation")
  @Override
  public void close() throws IOException {
    IOUtils.closeQuietly(bos);      
  }
  
  private void processBinarySerializer(String uri, String localName, String qName, Attributes attributes) throws Exception {    
    
    final String contentType = attributes.getValue("", "content-type");
    resp.setContentType(contentType != null ? contentType : Definitions.MIMETYPE_BINARY);
    
    final String contentDispositionFilename = attributes.getValue("", "content-disposition-filename");
    boolean attachment = StringUtils.isNotEmpty(contentDispositionFilename);
    String fileName = attachment ?  contentDispositionFilename : "unknown";
    resp.setHeader("Content-Disposition", 
        format(CONTENT_DISPOSITION_HEADER, (attachment ? "attachment" : "inline"), codec.encode(fileName, "UTF-8")));
    
    final String expireTimeSeconds = attributes.getValue("", "expire-time");
    if (expireTimeSeconds != null) {
      long expires = Long.parseLong(expireTimeSeconds);
      if (expires > 0) {
        resp.setHeader("Cache-Control", "public,max-age=" + expires + ",must-revalidate");
        resp.setDateHeader("Expires", System.currentTimeMillis() + SECONDS.toMillis(expires));
        resp.setHeader("Pragma", ""); // Explicitly set pragma to prevent container from overriding it.
      } else {
        resp.setHeader("Cache-Control", "no-cache,no-store,must-revalidate");
        resp.setDateHeader("Expires", 0);
        resp.setHeader("Pragma", "no-cache"); // Backwards compatibility for HTTP 1.0.
      }
    }
    
    final String path = attributes.getValue("", "path");
    if (path == null) {
      /* Write to HTTP response: */
      if (resp == null) {
        throw new SAXException("No attribute \"path\" specified on zip-serializer element");
      }                
      this.bos = os;              
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
      this.bos = new BufferedOutputStream(new FileOutputStream(outputFile));
    }
  }
    
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {       
    try {
      if (sb == null) {
        this.sb = new StringBuilder();
        processBinarySerializer(uri, localName, qName, attributes);
      } else {
        throw new SAXException("Binary serializer output has incorrect format");
      }
    } catch (SAXException e) {
      throw e;
    } catch (Exception e) {
      throw new SAXException(e);
    }
  }
  
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {        
    try {
      bos.write(Base64.getDecoder().decode(sb.toString()));
    } catch (IOException e) {
      throw new SAXException("Error writing binary serializer output to output stream", e);
    }
  }
  
  @Override
  public void endDocument() throws SAXException {
    try {
      close();
    } catch (IOException ioe) {
      throw new SAXException("Could not close OutputStream", ioe);
    }
  }
  
  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (sb != null) {
      sb.append(ch, start, length);
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