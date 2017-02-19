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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import de.fau.cs.osr.hddiff.tree.DiffNode;

public class NodeToDiffNodeConverter {

  public static DiffNode preprocess(Node node) {
    Document ownerDocument = node.getOwnerDocument();
    if (ownerDocument == null)
      ownerDocument = (Document) node;
    ownerDocument.setStrictErrorChecking(false);
    return new NodeToDiffNodeConverter().dispatch(node);
  }

  public DiffNode dispatch(Node node) {
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      return iterate((Element) node);
    } else if (node.getNodeType() == Node.TEXT_NODE) {
      return visit((Text) node);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  private DiffNode iterate(Element elem) {
    return iterate(new NodeDiffNodeAdapter(elem), elem);
  }

  private DiffNode iterate(NodeDiffNodeAdapter dn, Node node) {
    NodeList childNodes = node.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node childNode = childNodes.item(i);
      DiffNode dnChild = dispatch(childNode);
      if (dnChild != null)
        dn.appendChildDiffOnly(dnChild);
    }
    return dn;
  }

  private DiffNode visit(Text node) {
    return new NodeDiffNodeAdapterText(node);
  }

}