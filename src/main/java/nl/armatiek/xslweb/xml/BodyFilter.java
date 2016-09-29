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

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

public class BodyFilter extends XMLFilterImpl {
  
  @Override
  public void characters(char[] ch, int start, int len) throws SAXException {    
    super.characters(ch, start, len);
  }

  @Override
  public void endDocument() throws SAXException { }
  
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {        
    super.endElement(uri, localName, qName);
  }

  @Override
  public void endPrefixMapping(String prefix) throws SAXException {    
    super.endPrefixMapping(prefix);
  }

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {    
    super.ignorableWhitespace(ch, start, length);
  }

  @Override
  public void processingInstruction(String target, String data) throws SAXException { }

  @Override
  public void setDocumentLocator(Locator locator) { 
    super.setDocumentLocator(locator);
  }

  @Override
  public void skippedEntity(String name) throws SAXException {    
    super.skippedEntity(name);
  }

  @Override
  public void startDocument() throws SAXException { }
  
  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {    
    super.startElement(uri, localName, qName, atts);
  }

  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {    
    super.startPrefixMapping(prefix, uri);    
  }

}
