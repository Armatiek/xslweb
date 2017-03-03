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

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.fau.cs.osr.hddiff.tree.DiffNode;

public class NodeDiffNodeAdapterText extends NodeDiffNodeAdapterTextContainer {
  
  public NodeDiffNodeAdapterText(Node node) {
    super(node);
  }

  @Override
  public DiffNode createSame(DiffNode forRoot_) {
    NodeDiffNodeAdapter forRoot = (NodeDiffNodeAdapter) forRoot_;
    Document doc = forRoot.node.getOwnerDocument();
    if (doc == null)
      doc = (Document) forRoot.node;
    return new NodeDiffNodeAdapterText(doc.createTextNode(node.getTextContent()));
  }

  @Override
  public DiffNode splitText(int pos) {
    String text = node.getTextContent();
    String ta = text.substring(0, pos);
    String tb = text.substring(pos);
    
    node.setTextContent(ta);
    
    Node nb = node.getOwnerDocument().createTextNode(tb);
    NodeDiffNodeAdapterText newNode = new NodeDiffNodeAdapterText(nb);
    
    getParent().appendOrInsert(newNode, getNextSibling());
    
    return newNode;
  }
  
}