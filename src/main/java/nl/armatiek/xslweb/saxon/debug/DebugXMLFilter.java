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

import java.util.UUID;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import nl.armatiek.xslweb.configuration.Definitions;

public class DebugXMLFilter extends XMLFilterImpl {

  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    if (uri.equals(Definitions.NAMESPACEURI_XSLT) && localName.equals("template") 
        && atts.getIndex("match") != -1 && atts.getIndex("name") == -1) {
      /* match template without name found */
      AttributesImpl newAttributes = new AttributesImpl(atts);
      newAttributes.addAttribute("", "name", "name", "CDATA", "name-" + UUID.randomUUID().toString());
      super.startElement(uri, localName, qName, newAttributes);
    } else {
      super.startElement(uri, localName, qName, atts);
    }
  }
  
}