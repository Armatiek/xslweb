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

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import javax.xml.XMLConstants;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.fau.cs.osr.hddiff.tree.DiffNode;
import de.fau.cs.osr.hddiff.tree.NodeUpdate;
import de.fau.cs.osr.utils.ComparisonException;

public class NodeDiffNodeAdapter extends DiffNode {
  
  protected Node node;
  
  public NodeDiffNodeAdapter(Node node) {
    this.node = node;
  }

  @Override
  protected void appendOrInsertNativeOnly(DiffNode newChild_, DiffNode refChild_) {
    NodeDiffNodeAdapter newChild = (NodeDiffNodeAdapter) newChild_;
    NodeDiffNodeAdapter refChild = (NodeDiffNodeAdapter) refChild_;
    if (refChild != null)
      node.insertBefore(newChild.node, refChild.node);
    else
      node.appendChild(newChild.node);
  }

  @Override
  public void compareNativeDeep(DiffNode o) throws ComparisonException {
    compare(this.node, ((NodeDiffNodeAdapter) o).node);
  }
  
  private void compare(Node a, Node b) throws ComparisonException {
    if (a.getNodeType() != b.getNodeType()) {
      throw new ComparisonException(a, b);
    }
    if (!compareStrings(a.getNodeName(), b.getNodeName())) {
      throw new ComparisonException(a, b);
    }
    if (!compareStrings(a.getNamespaceURI(), b.getNamespaceURI())) {
      throw new ComparisonException(a, b);
    }
    if (!compareStrings(a.getNodeValue(), b.getNodeValue())) {
      throw new ComparisonException(a, b);
    }
    if (a.getNodeType() == Node.ELEMENT_NODE && !compareAttributes(DiffUtils.toCollection(a.getAttributes()), DiffUtils.toCollection(b.getAttributes()))) {
      throw new ComparisonException(a, b);
    }
    if (!compareChildren(a.getChildNodes(), b.getChildNodes())) {
      throw new ComparisonException(a, b);
    }
  }

  private boolean compareAttributes(Collection<Attr> a, Collection<Attr> b) {
    final int size = a.size();
    if (size != b.size())
      return false;
    Attr[] aa = a.toArray(new Attr[size]);
    Attr[] ba = b.toArray(new Attr[b.size()]);
    Comparator<Attr> cmp = new Comparator<Attr>() {
      @Override
      public int compare(Attr o1, Attr o2) {
        return o1.getName().compareTo(o2.getName());
      }
    };
    Arrays.sort(aa, cmp);
    Arrays.sort(ba, cmp);
    for (int i = 0, j = 0; i < aa.length && j < ba.length;) {
      Attr attrA = aa[i];
      Attr attrB = ba[i];
      if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attrA.getNamespaceURI()) && XMLConstants.XMLNS_ATTRIBUTE.equals(attrA.getName())) {
        ++i;
        continue;
      }
      if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attrB.getNamespaceURI()) && XMLConstants.XMLNS_ATTRIBUTE.equals(attrB.getName())) {
        ++j;
        continue;
      }
      if (!compareStrings(attrA.getName(), attrB.getName())) {
        return false;
      }
      if (!compareStrings(attrA.getValue(), attrB.getValue())) {
        return false;
      }
      ++i;
      ++j;
    }
    return true;
  }

  private boolean compareChildren(NodeList a, NodeList b) throws ComparisonException {
    if (a.getLength() != b.getLength()) {
      return false;
    }
    for (int i=0; i<a.getLength(); i++) {
      compare(a.item(i), b.item(i));
    }
    return true;
  }

  @Override
  public DiffNode createSame(DiffNode forRoot_) {
    NodeDiffNodeAdapter forRoot = (NodeDiffNodeAdapter) forRoot_;
    Document doc = forRoot.node.getOwnerDocument();
    if (doc == null)
      doc = (Document) forRoot.node;
    
    Node prototype = node;
    
    Element elem = (prototype.getNamespaceURI() == null) ?
        (Element) doc.createElement(prototype.getNodeName()) :
        (Element) doc.createElementNS(
            prototype.getNamespaceURI(),
            prototype.getNodeName());
  
    
    NamedNodeMap attrs = prototype.getAttributes();
    for (int i=0; i<attrs.getLength(); i++) {
      elem.setAttributeNode((Attr) doc.importNode(attrs.item(i).cloneNode(true), true));
    }
    
    return new NodeDiffNodeAdapter(elem);
  }
  
  @Override
  public String getLabel() {
    return node.getNodeName();
  }

  @Override
  public Object getNativeNode() {
    return node;
  }

  @Override
  public String getTextContent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getType() {
    String name = node.getLocalName();
    if (name == null)
      return node.getNodeName();
    return StringUtils.defaultIfEmpty(node.getNamespaceURI(), "") + node.getLocalName();
  }

  @Override
  public boolean isLeaf() {
    return node.getFirstChild() == null;
  }

  @Override
  public boolean isSameNodeType(DiffNode o) {
    return getType().equals(o.getType());
  }

  @Override
  public boolean isTextLeaf() {
    return false;
  }

  @Override
  protected void removeFromParentNativeOnly() {
    if (node.getParentNode() == null)
      throw new UnsupportedOperationException();
    node.getParentNode().removeChild(node);
  }

  @Override
  public void setNativeId(String id) {
    if (node.getNodeType() != Node.ELEMENT_NODE)
      throw new UnsupportedOperationException();
    ((Element) node).setAttribute("id", id);
  }

  @Override
  public DiffNode splitText(int pos) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public void applyUpdate(NodeUpdate value_) {
    MyNodeUpdate value = (MyNodeUpdate) value_;
    if (value.value != null)
      throw new IllegalArgumentException();
    
    Element elem = (Element) node;
    if (node.hasAttributes()) {
      NamedNodeMap attrs = node.getAttributes();
      while (attrs.getLength() > 0) {
        elem.removeAttributeNode((Attr) attrs.item(0));
      }
    }
    NamedNodeMap newAttrs = value.attributes;
    if (newAttrs != null) {
      for (int i=0; i<newAttrs.getLength(); i++) {
        Attr attr = (Attr) newAttrs.item(i);
        elem.setAttributeNode((Attr) elem.getOwnerDocument().importNode(attr.cloneNode(true), true));
      }
    }
  }

  @Override
  public NodeUpdate compareWith(DiffNode o) {
    if (!isSameNodeType(o))
      throw new IllegalArgumentException();

    Node a = this.node;
    Node b = ((NodeDiffNodeAdapter) o).node;

    Collection<Attr> aac = DiffUtils.toCollection(a.getAttributes());
    Collection<Attr> bac = DiffUtils.toCollection(b.getAttributes());

    if (!(aac.isEmpty() && bac.isEmpty())) {
      if (aac.size() != bac.size())
        return new MyNodeUpdate(b.getAttributes(), null);

      Iterator<Attr> aai = aac.iterator();
      Iterator<Attr> bai = bac.iterator();
      while (aai.hasNext()) {
        if (!attrEquals(aai.next(), bai.next()))
          return new MyNodeUpdate(b.getAttributes(), null);
      }
    }
    return null;
  }
  
  private boolean attrEquals(Attr a, Attr b) {
    return compareStrings(a.getNamespaceURI(), b.getNamespaceURI()) && 
        compareStrings(a.getPrefix(), b.getPrefix()) && 
        a.getNodeName().equals(b.getNodeName()) && 
        a.getNodeValue().equals(b.getNodeValue());
  }
  
  protected boolean compareStrings(String a, String b) {
    return (a != null) ? a.equals(b) : (b == null);
  }
  
  public static final class MyNodeUpdate implements NodeUpdate {
    
    public final NamedNodeMap attributes;
    public final String value;

    public MyNodeUpdate(NamedNodeMap attributes, String value) {
      super();
      this.attributes = attributes;
      this.value = value;
    }

    @Override
    public String toString() {
      if (value != null)
        return "NodeUpdate [value=" + value + "]";
      else
        return "NodeUpdate [attributes=" + Arrays.toString(DiffUtils.toCollection(attributes).toArray()) + "]";
    }
    
    @Override
    public void applyUpdates(Object node) {
      throw new UnsupportedOperationException();
    }
    
  }

}