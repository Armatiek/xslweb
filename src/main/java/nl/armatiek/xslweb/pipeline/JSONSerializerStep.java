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

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.ErrorListener;

import org.xml.sax.Attributes;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.stax.XMLStreamWriterDestination;
import nl.armatiek.xslweb.configuration.WebApp;

/**
 * Pipeline step that comverts XML to a JSON representation
 * 
 * @author Maarten Kroon
 */
public class JSONSerializerStep extends SerializerStep {
  
  private boolean autoArray;
  private boolean autoPrimitive;
  private boolean multiplePI;
  private boolean namespaceDeclarations;
  private char namespaceSeparator;
  private boolean prettyPrint;
  private QName virtualRoot;
  private boolean repairingNamespaces;
  private Map<String, String> namespaceMappings = new HashMap<String, String>();
  
  public JSONSerializerStep(Attributes atts) {
    super(atts);
    this.autoArray = Boolean.parseBoolean(getAttribute(atts, "auto-array", "false"));
    this.autoPrimitive = Boolean.parseBoolean(getAttribute(atts, "auto-primitive", "false"));
    this.multiplePI = Boolean.parseBoolean(getAttribute(atts, "multi-pi", "true"));
    this.namespaceDeclarations = Boolean.parseBoolean(getAttribute(atts, "namespace-declarations", "true"));
    this.namespaceSeparator = getAttribute(atts, "namespace-separator", ":").charAt(0);
    this.prettyPrint = Boolean.parseBoolean(getAttribute(atts, "pretty-print", "false"));
    String name = getAttribute(atts, "virtual-root-name", null);
    if (name != null) {
      this.virtualRoot = new QName(getAttribute(atts, "virtual-root-namespace", XMLConstants.NULL_NS_URI), name);
    }
    this.repairingNamespaces = Boolean.parseBoolean(getAttribute(atts, "repairing-namespaces", "false"));
  }
  
  public void addNamespaceDeclaration(String namespace, String name) {
    if (namespaceMappings == null) {
      namespaceMappings = new HashMap<String, String>();
    }
    namespaceMappings.put(namespace, name);
  }
  
  @Override
  public Destination getDestination(WebApp webApp, HttpServletRequest req, HttpServletResponse resp, 
      OutputStream os, Properties outputProperties, ErrorListener errorListener) throws XMLStreamException {
    JsonXMLConfig config = new JsonXMLConfigBuilder()
        .autoArray(autoArray)
        .autoPrimitive(autoPrimitive)
        .multiplePI(multiplePI)
        .namespaceDeclarations(namespaceDeclarations)
        .namespaceSeparator(namespaceSeparator)
        .prettyPrint(prettyPrint)
        .virtualRoot(virtualRoot)
        .repairingNamespaces(repairingNamespaces)
        .namespaceMappings(namespaceMappings)
        .build();
    XMLOutputFactory factory = new JsonXMLOutputFactory(config);
    return new XMLStreamWriterDestination(factory.createXMLStreamWriter(os, outputProperties.getProperty("encoding", "UTF-8")));            
  }
  
}