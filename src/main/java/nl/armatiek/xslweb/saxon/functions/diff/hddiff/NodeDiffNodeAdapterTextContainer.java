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

import org.w3c.dom.Node;

import de.fau.cs.osr.hddiff.tree.DiffNode;

public class NodeDiffNodeAdapterTextContainer extends NodeDiffNodeAdapter {
  
  public NodeDiffNodeAdapterTextContainer(Node node) {
    super(node);
  }

  @Override
  public void setNodeValue(Object value_) {
    NodeUpdate value = (NodeUpdate) value_;

    if (value.attributes != null)
      throw new IllegalArgumentException();
    
    String newValue = value.value;
    if (!compareStrings(node.getTextContent(), newValue))
      node.setTextContent(newValue);
  }

  @Override
  public Object getNodeValue() {
    return new NodeUpdate(null, node.getTextContent());
  }

  @Override
  public boolean isNodeValueEqual(DiffNode o) {
    if (!isSameNodeType(o))
      throw new IllegalArgumentException();

    Node a = this.node;
    Node b = ((NodeDiffNodeAdapter) o).node;

    return compareStrings(a.getTextContent(), b.getTextContent());
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  public boolean isTextLeaf() {
    return true;
  }

  @Override
  public String getTextContent() {
    return node.getTextContent();
  }
  
  /*
  @Override
  public void setNativeAttribute(String name, String value) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public void setNativeAttributeNS(String namespaceURI, String qualifiedName, String value) {
    throw new UnsupportedOperationException();
  }
  */
  
}