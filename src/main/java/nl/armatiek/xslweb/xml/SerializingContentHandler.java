package nl.armatiek.xslweb.xml;

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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.StreamWriterToReceiver;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.trans.XPathException;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.sun.xml.ws.util.xml.ContentHandlerToXMLStreamWriter;

public class SerializingContentHandler implements ContentHandler {
      
  private ContentHandler writer;
  private StreamWriterToReceiver xsw;
  
  public SerializingContentHandler(OutputStream os, Configuration conf, 
      Properties outputProperties) throws IOException, XPathException {                
    SerializerFactory sf = new SerializerFactory(conf);    
    this.xsw = sf.getXMLStreamWriter(new StreamResult(os), outputProperties);    
    this.writer = new ContentHandlerToXMLStreamWriter(xsw);       
  }
  
  public void close() throws SAXException {
    try {
      xsw.close();     
    } catch (XMLStreamException e) {
      throw new SAXException("Could not close StreamWriterToReceiver", e);
    }
  }
  
  @Override
  public void characters(char[] ch, int start, int len) throws SAXException {        
    writer.characters(ch, start, len);
  }

  @Override
  public void endDocument() throws SAXException {        
    writer.endDocument(); 
    close();
  }
  
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {            
    writer.endElement(uri, localName, qName);
  }

  @Override
  public void endPrefixMapping(String prefix) throws SAXException {        
    writer.endPrefixMapping(prefix);
  }

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {        
    writer.ignorableWhitespace(ch, start, length);
  }

  @Override
  public void processingInstruction(String target, String data) throws SAXException {        
    writer.processingInstruction(target, data);
  }

  @Override
  public void setDocumentLocator(Locator locator) {        
    writer.setDocumentLocator(locator);
  }

  @Override
  public void skippedEntity(String name) throws SAXException {        
    writer.skippedEntity(name);
  }

  @Override
  public void startDocument() throws SAXException {        
    writer.startDocument();
  }
  
  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {        
    writer.startElement(uri, localName, qName, atts);
  }

  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {        
    writer.startPrefixMapping(prefix, uri);    
  }
  
}