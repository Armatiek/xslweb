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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import nl.armatiek.xslweb.configuration.Definitions;

import javanet.staxutils.helpers.StreamWriterDelegate;

/**
 * StreamWriterDelegate that skips writing out XSLWeb related namespaces.
 * 
 * @author Maarten
 */
public class CleanupXMLStreamWriter extends StreamWriterDelegate {

  public CleanupXMLStreamWriter(XMLStreamWriter out) {
    super(out);    
  }
  
  @Override
  public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
    if (!namespaceURI.startsWith(Definitions.NAMESPACEURI_XSLWEB)) {
      out.writeNamespace(prefix, namespaceURI);
    }    
  }
  
}