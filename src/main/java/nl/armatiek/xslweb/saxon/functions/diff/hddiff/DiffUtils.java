package nl.armatiek.xslweb.saxon.functions.diff.hddiff;

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

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.Whitespace;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.error.XSLWebException;
import nl.armatiek.xslweb.saxon.functions.diff.DiffXML;
import nl.armatiek.xslweb.saxon.functions.diff.DiffXML.WhitespaceStrippingPolicy;


public class DiffUtils {
  
  public static Node createNodeFromNodeInfo(NodeInfo nodeInfo, 
      Document doc, DiffXML.WhitespaceStrippingPolicy whitespaceHandling) {
    Node node = null;
    switch (nodeInfo.getNodeKind()) {
    case Type.TEXT:
      String content = nodeInfo.getStringValue();
      boolean isWhitespace = Whitespace.isWhite(content);
      if (isWhitespace && whitespaceHandling.equals(DiffXML.WhitespaceStrippingPolicy.ALL)) {
        break;
      }
      node = doc.createTextNode(content);
      break;
    case Type.WHITESPACE_TEXT:
      content = nodeInfo.getStringValue();
      if (whitespaceHandling.equals(DiffXML.WhitespaceStrippingPolicy.ALL)) {
        node = doc.createTextNode(content);
      }
      break;
    case Type.ELEMENT:
      if (nodeInfo.getURI().equals("")) {
        node = doc.createElement(nodeInfo.getLocalPart());
      } else {
        node = doc.createElementNS(nodeInfo.getURI(), nodeInfo.getLocalPart());
      }
      
      AxisIterator namespaces = nodeInfo.iterateAxis(AxisInfo.NAMESPACE);
      NodeInfo ns;
      while ((ns = namespaces.next()) != null) {
        String localPart = ns.getLocalPart();
        String qualifiedName;
        if (localPart.equals(""))
          qualifiedName = "xmlns";
        else
          qualifiedName = "xmlns:" + localPart;
        ((Element) node).setAttributeNS("http://www.w3.org/2000/xmlns/", qualifiedName, ns.getStringValue());
      }
      
      AxisIterator attrs = nodeInfo.iterateAxis(AxisInfo.ATTRIBUTE);
      NodeInfo attr;
      while ((attr = attrs.next()) != null) {
        if (attr.getURI().equals("")) {
          ((Element) node).setAttribute(attr.getLocalPart(), attr.getStringValue());
        } else if (attr.getURI().equals(Definitions.NAMESPACEURI_DELTAXML) && attr.getLocalPart().equals("whitespace")) {
          String value = attr.getStringValue();
          try {
            whitespaceHandling = WhitespaceStrippingPolicy.valueOf(value);
          } catch (Exception e) {
            throw new XSLWebException("Value for whitespace handling not supported: \"" + value + "\"");
          }
        } else {
          ((Element) node).setAttributeNS(attr.getURI(), attr.getPrefix() + ":" + attr.getLocalPart(), attr.getStringValue());
        }
      }
      
      break;
    /*
    case Type.COMMENT:
      treeNode = new CommentNode(nodeInfo.getStringValue());
      break;
    case Type.PROCESSING_INSTRUCTION:
      treeNode = new ProcessingInstructionNode(nodeInfo.getStringValue()); // TODO
      break;
    */
    }
    
    if (node != null && nodeInfo.hasChildNodes()) {
      AxisIterator childs = nodeInfo.iterateAxis(AxisInfo.CHILD);
      NodeInfo childNodeInfo;
      while ((childNodeInfo = childs.next()) != null) {
        Node newNode = createNodeFromNodeInfo(childNodeInfo, doc, whitespaceHandling);
        if (newNode != null) {
          node.appendChild(newNode);
        }
      }
    }
    
    return node;
  }
  
  public static Document createDocumentFromNodeInfo(NodeInfo nodeInfo, 
      DiffXML.WhitespaceStrippingPolicy whitespaceHandling) throws ParserConfigurationException {
    if (nodeInfo.getNodeKind() == Type.DOCUMENT) {
      nodeInfo = nodeInfo.iterateAxis(AxisInfo.CHILD).next();
    }
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance(); 
    docFactory.setNamespaceAware(true);
    docFactory.setValidating(false);
    // docFactory.setExpandEntityReferences(true);
    Document doc = docFactory.newDocumentBuilder().newDocument();
    doc.setStrictErrorChecking(true); // TODO
    doc.appendChild(createNodeFromNodeInfo(nodeInfo, doc, whitespaceHandling));
    return doc;
  }
  
  public static Collection<Attr> toCollection(NamedNodeMap attrs) {
    ArrayList<Attr> c = new ArrayList<Attr>(attrs.getLength());
    for (int i=0; i<attrs.getLength(); i++) {
      c.add((Attr) attrs.item(i));
    }
    return c;
  }
  
}