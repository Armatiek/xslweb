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
import de.fau.cs.osr.hddiff.tree.NodeMetricsInterface;

public final class NodeMetrics implements NodeMetricsInterface {
  
  @Override
  public int computeHash(DiffNode node) {
    Node n = (Node)((NodeDiffNodeAdapter) node).getNativeNode();
    int hash = (21613 * n.getNodeType()) ^ n.getNodeName().hashCode() ^ getNamespaceUriHashCode(n);
    if (node.isTextLeaf())
      hash ^= node.getTextContent().hashCode();
    return hash;
  }

  private int getNamespaceUriHashCode(Node node) {
    String uri = node.getNamespaceURI();
    if (uri == null)
      return 62401;
    return uri.hashCode();
  }

  @Override
  public int computeWeight(DiffNode node_) {
    NodeDiffNodeAdapter node = (NodeDiffNodeAdapter) node_;
    if (node.isTextLeaf()) {
      return (int) node.getTextContent().length();
    } else {
      return 3;
    }
  }

  @Override
  public boolean verifyHashEquality(DiffNode n1__, DiffNode n2__) {
    NodeDiffNodeAdapter n1_ = (NodeDiffNodeAdapter) n1__;
    NodeDiffNodeAdapter n2_ = (NodeDiffNodeAdapter) n2__;
    Node n1 = (Node) n1_.getNativeNode();
    Node n2 = (Node) n2_.getNativeNode();
    if (n1.getNodeType() != n2.getNodeType())
      return false;
    if (!n1.getNodeName().equals(n2.getNodeName()))
      return false;
    if (!compareStrings(n1.getNamespaceURI(), n2.getNamespaceURI()))
      return false;
    if (n1_.isTextLeaf() && !compareStrings(n1.getTextContent(), n2.getTextContent()))
      return false;
    return true;
  }

  private boolean compareStrings(String a, String b) {
    return (a == null) ? (b == null) : a.equals(b);
  }
  
}