package nl.armatiek.xslweb.saxon.functions.diff.util;

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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.parser.ExplicitLocation;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.linked.LinkedTreeBuilder;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.Whitespace;
import nl.armatiek.xslweb.saxon.functions.diff.DiffXML;
import nl.armatiek.xslweb.saxon.functions.diff.node.CDataNode;
import nl.armatiek.xslweb.saxon.functions.diff.node.CommentNode;
import nl.armatiek.xslweb.saxon.functions.diff.node.DocTypeNode;
import nl.armatiek.xslweb.saxon.functions.diff.node.Document;
import nl.armatiek.xslweb.saxon.functions.diff.node.ElementNode;
import nl.armatiek.xslweb.saxon.functions.diff.node.ProcessingInstructionNode;
import nl.armatiek.xslweb.saxon.functions.diff.node.TextNode;
import nl.armatiek.xslweb.saxon.functions.diff.node.TreeNode;

public class DiffUtils {
  
  public static String getClarkName(NodeInfo nodeInfo) {
    String uri = nodeInfo.getURI();
    if (uri == null || uri.equals("")) {
      return nodeInfo.getLocalPart();
    } else {
      return "{" + nodeInfo.getURI() + "}" + nodeInfo.getLocalPart();
    }
  }
  
  public static TreeNode createTreeNodeFromNodeInfo(NodeInfo nodeInfo, 
      Map<String, String> uriToPrefixMap, DiffXML.WhitespaceHandlingMethod whitespaceHandling) {
    TreeNode treeNode = null;
    switch (nodeInfo.getNodeKind()) {
    case Type.TEXT:
      String content = nodeInfo.getStringValue();
      boolean isWhitespace = Whitespace.isWhite(content);
      if (isWhitespace && whitespaceHandling.equals(DiffXML.WhitespaceHandlingMethod.ALL)) {
        break;
      }
      treeNode = new TextNode(content);
      break;
    case Type.WHITESPACE_TEXT:
      if (whitespaceHandling.equals(DiffXML.WhitespaceHandlingMethod.ALL)) {
        treeNode = new TextNode(nodeInfo.getStringValue());
      }
      break;
    case Type.ELEMENT:
      treeNode = new ElementNode(getClarkName(nodeInfo));
      AxisIterator attrs = nodeInfo.iterateAxis(AxisInfo.ATTRIBUTE);
      NodeInfo attr;
      while ((attr = attrs.next()) != null) {
        ((ElementNode) treeNode).setAttribute(getClarkName(attr), attr.getStringValue());
      }
      if (uriToPrefixMap != null) {
        AxisIterator namespaces = nodeInfo.iterateAxis(AxisInfo.NAMESPACE);
        NodeInfo ns;
        while ((ns = namespaces.next()) != null) {
          uriToPrefixMap.put(ns.getStringValue(), ns.getLocalPart());
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
    
    if (treeNode != null && nodeInfo.hasChildNodes()) {
      AxisIterator childs = nodeInfo.iterateAxis(AxisInfo.CHILD);
      NodeInfo childNodeInfo;
      while ((childNodeInfo = childs.next()) != null) {
        TreeNode newTreeNode = createTreeNodeFromNodeInfo(childNodeInfo, uriToPrefixMap, whitespaceHandling);
        if (newTreeNode != null) {
          treeNode.appendChild(newTreeNode);
        }
      }
    }
    
    return treeNode;
  }
  
  public static Document createDocumentFromNodeInfo(NodeInfo nodeInfo, 
      Map<String, String> uriToPrefixMap, DiffXML.WhitespaceHandlingMethod whitespaceHandling) {
    if (nodeInfo.getNodeKind() == Type.DOCUMENT) {
      nodeInfo = nodeInfo.iterateAxis(AxisInfo.CHILD).next();
    }
    Document doc = new Document();  
    doc.appendChild(createTreeNodeFromNodeInfo(nodeInfo, uriToPrefixMap, whitespaceHandling));
    return doc;
  }
  
  public static NodeName getNodeName(String expandedName, Map<String, String> uriToPrefixMap) {
    String prefix;
    String namespace;
    String localName;
    if (expandedName.charAt(0) == '{') {
      int closeBrace = expandedName.indexOf('}');
      if (closeBrace < 0) {
        throw new IllegalArgumentException("No closing '}' in Clark name");
      }
      namespace = expandedName.substring(1, closeBrace);
      if (closeBrace == expandedName.length()) {
        throw new IllegalArgumentException("Missing local part in Clark name");
      }
      localName = expandedName.substring(closeBrace + 1);
    } else {
      namespace = "";
      localName = expandedName;
    }
    
    if (namespace.equals("")) {
      return new NoNamespaceName(localName);
    } else if (uriToPrefixMap != null && (prefix = uriToPrefixMap.get(namespace)) != null) {
      return new FingerprintedQName(prefix, namespace, localName);
    } else {
      return new FingerprintedQName("", namespace, localName);
    }
  }
  
  public static void createNodeInfoFromTreeNode(TreeNode treeNode, LinkedTreeBuilder builder, 
      Map<String, String> uriToPrefixMap, boolean root) throws XPathException {
    if (treeNode.getNodeType() == Node.TEXT_NODE) {
      builder.characters(((TextNode)treeNode).getContent(), ExplicitLocation.UNKNOWN_LOCATION, 0);
    } else if (treeNode.getNodeType() == Node.ELEMENT_NODE) {
      builder.startElement(getNodeName(((ElementNode)treeNode).getElementName(), uriToPrefixMap), Untyped.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0); // TODO
      if (root && uriToPrefixMap != null) {
        for (Map.Entry<String, String> entry : uriToPrefixMap.entrySet()) {
          builder.namespace(new NamespaceBinding(entry.getValue(), entry.getKey()), 0);
        }
      }
      if (treeNode.hasAttributes()) {
        Iterator<String> attrs = treeNode.getAttributes().keySet().iterator();
        while (attrs.hasNext()) { 
          String name = attrs.next();
          builder.attribute(getNodeName(name, uriToPrefixMap), BuiltInAtomicType.UNTYPED_ATOMIC, treeNode.getAttributes().get(name), ExplicitLocation.UNKNOWN_LOCATION, 0); // TODO
        }
      }
      builder.startContent();
      if (treeNode.hasChildren()) {
        List<TreeNode> childs = treeNode.getChildren();
        for (TreeNode childNode : childs) {
          createNodeInfoFromTreeNode(childNode, builder, uriToPrefixMap, false);
        }
      }
      builder.endElement();
    } else if (treeNode.getNodeType() == Node.COMMENT_NODE) {
      builder.comment(((CommentNode)treeNode).getContent(), ExplicitLocation.UNKNOWN_LOCATION, 0);
    } else if (treeNode.getNodeType() == Node.CDATA_SECTION_NODE) {
      builder.characters(((CDataNode)treeNode).getContent(), ExplicitLocation.UNKNOWN_LOCATION, 0);
    } else if (treeNode.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
      builder.processingInstruction(((ProcessingInstructionNode)treeNode).getTarget(), ((ProcessingInstructionNode)treeNode).getContent(), ExplicitLocation.UNKNOWN_LOCATION, 0);
    } else if (treeNode.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
      // Do nothing
    } 
    
  }
  
  public static NodeInfo createNodeInfoFromDocument(Document doc, PipelineConfiguration config, 
      Map<String, String> uriToPrefixMap) throws XPathException {
    LinkedTreeBuilder builder = (LinkedTreeBuilder) TreeModel.LINKED_TREE.makeBuilder(config);
    builder.setLineNumbering(false);
    builder.open();
    builder.startDocument(0);
    /*
    if (namespaceBindings != null) {
      for (NamespaceBinding nsb : namespaceBindings) {
        uriToPrefixMap.put(nsb.getURI(), nsb.getPrefix());
      }
    }
    */
    createNodeInfoFromTreeNode(doc.getFirstChild(), builder, uriToPrefixMap, true);
    builder.endDocument();
    builder.close();
    return builder.getCurrentRoot();
  }
  
  public static TreeNode cloneTreeNode(TreeNode treeNode) {
    return cloneTreeNode(treeNode, true);
  }
  
  public static TreeNode cloneTreeNode(TreeNode treeNode, boolean deep) {
    TreeNode copyNode = null;
    if (treeNode.getNodeType() == Node.TEXT_NODE) {
      copyNode = new TextNode(((TextNode)treeNode).getContent());
    } else if (treeNode.getNodeType() == Node.ELEMENT_NODE) {
      copyNode = new ElementNode(((ElementNode)treeNode).getElementName());
      Iterator<String> attrs = treeNode.getAttributes().keySet().iterator();
      while (attrs.hasNext()) { 
        String name = attrs.next();
        copyNode.setAttribute(name, treeNode.getAttributes().get(name));
      } 
    } else if (treeNode.getNodeType() == Node.COMMENT_NODE) {
      copyNode = new CommentNode(((CommentNode)treeNode).getContent());
    } else if (treeNode.getNodeType() == Node.CDATA_SECTION_NODE) {
      copyNode = new CDataNode(((CDataNode)treeNode).getContent());
    } else if (treeNode.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
      copyNode = new ProcessingInstructionNode(((ProcessingInstructionNode)treeNode).getTarget(), ((ProcessingInstructionNode)treeNode).getContent());
    } else if (treeNode.getNodeType() == Node.DOCUMENT_NODE) {
      copyNode = new Document(((Document)treeNode).getVersion(), ((Document)treeNode).getEncoding());
    } else if (treeNode.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
      copyNode = new DocTypeNode(((DocTypeNode)treeNode).getContent());
    } 
    
    if (deep && copyNode != null && treeNode.hasChildren()) {
      List<TreeNode> childs = treeNode.getChildren();
      for (TreeNode childNode : childs) {
        copyNode.appendChild(childNode);
      }
    }
    
    return copyNode;
  }

}