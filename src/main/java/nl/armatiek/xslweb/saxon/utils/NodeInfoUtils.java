package nl.armatiek.xslweb.saxon.utils;

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

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.LocalNameTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.type.Type;

public class NodeInfoUtils {
  
  public static NodeInfo getFirstChildElement(NodeInfo parentElement) {
    return parentElement.iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT).next();    
  }
  
  public static NodeInfo getNextSiblingElement(NodeInfo parentElement) {
    return parentElement.iterateAxis(AxisInfo.FOLLOWING_SIBLING, NodeKindTest.ELEMENT).next();    
  }
  
  public static String getValueOfChildElementByLocalName(NodeInfo parentElement, String localName, XPathContext context) {
    NodeInfo nodeInfo = parentElement.iterateAxis(AxisInfo.CHILD, new LocalNameTest(context.getNamePool(), Type.ELEMENT, localName)).next();
    if (nodeInfo != null) {
      return nodeInfo.getStringValue();
    }
    return null;
  }
  
  /*
  private static void cloneNodeInfo(NodeInfo nodeInfo, LinkedTreeBuilder builder) throws XPathException {
    if (nodeInfo.getNodeKind() == Type.TEXT) {
      builder.characters(nodeInfo.getStringValueCS(), ExplicitLocation.UNKNOWN_LOCATION, 0);
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
  
  public static NodeInfo cloneTinyTree2LinkedTree(NodeInfo node, PipelineConfiguration config) throws XPathException {
    
    
    
    LinkedTreeBuilder builder = (LinkedTreeBuilder) TreeModel.LINKED_TREE.makeBuilder(config);
    builder.setLineNumbering(false);
    builder.open();
    builder.startDocument(0);
    cloneNodeInfo(node, builder);
    builder.endDocument();
    builder.close();
    return builder.getCurrentRoot();
  }

  */
}