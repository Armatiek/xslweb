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
package nl.armatiek.xslweb.saxon.debug;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

public class DebugXMLReader implements XMLReader {
  
  private XMLReader reader;
  
  public DebugXMLReader() throws ParserConfigurationException, SAXException {
    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setNamespaceAware(true);
    spf.setXIncludeAware(true);
    spf.setValidating(false);
    SAXParser parser = spf.newSAXParser();
    XMLReader reader = parser.getXMLReader(); 
    DebugXMLFilter debugXMLFilter = new DebugXMLFilter();
    debugXMLFilter.setParent(reader);
    this.reader = debugXMLFilter;
  }

  public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
    return reader.getFeature(name);
  }

  public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
    reader.setFeature(name, value);
  }

  public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
    return reader.getProperty(name);
  }

  public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
    reader.setProperty(name, value);
  }

  public void setEntityResolver(EntityResolver resolver) {
    reader.setEntityResolver(resolver);
  }

  public EntityResolver getEntityResolver() {
    return reader.getEntityResolver();
  }

  public void setDTDHandler(DTDHandler handler) {
    reader.setDTDHandler(handler);
  }

  public DTDHandler getDTDHandler() {
    return reader.getDTDHandler();
  }

  public void setContentHandler(ContentHandler handler) {
    reader.setContentHandler(handler);
  }

  public ContentHandler getContentHandler() {
    return reader.getContentHandler();
  }

  public void setErrorHandler(ErrorHandler handler) {
    reader.setErrorHandler(handler);
  }

  public ErrorHandler getErrorHandler() {
    return reader.getErrorHandler();
  }

  public void parse(InputSource input) throws IOException, SAXException {
    reader.parse(input);
  }

  public void parse(String systemId) throws IOException, SAXException {
    reader.parse(systemId);
  }
  
}