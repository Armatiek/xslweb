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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import de.fau.cs.osr.hddiff.editscript.EditOp;
import de.fau.cs.osr.hddiff.editscript.EditOp.Operation;
import de.fau.cs.osr.hddiff.editscript.EditOpDelete;
import de.fau.cs.osr.hddiff.editscript.EditOpInsert;
import de.fau.cs.osr.hddiff.editscript.EditOpMove;
import de.fau.cs.osr.hddiff.editscript.EditOpUpdate;
import de.fau.cs.osr.hddiff.tree.DiffNode;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.diff.hddiff.DeltaInfo.DeleteInfo;
import nl.armatiek.xslweb.saxon.functions.diff.hddiff.NodeDiffNodeAdapter.NodeUpdate;
import nl.armatiek.xslweb.utils.XMLUtils;

public class EditScriptManager {
  
  private static final String DELTAINFO_KEY = "deltaInfo";
  
  // private static final boolean ASSERTIONS = false;

  private final List<EditOp> editScript;
  private final HashMap<DiffNode, Effect> effects;
  private Node root;

  public EditScriptManager(List<EditOp> editScript, Node root) {
    this.editScript = editScript;
    this.effects = new HashMap<>(editScript.size());
    this.root = root.getNodeType() == Node.DOCUMENT_NODE ? ((Document) root).getDocumentElement() : root;
    ((Element) this.root).setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + Definitions.PREFIX_DELTAXML, Definitions.NAMESPACEURI_DELTAXML);
    ((Element) this.root).setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + Definitions.PREFIX_DXA, Definitions.NAMESPACEURI_DXA);
    ((Element) this.root).setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + Definitions.PREFIX_DXX, Definitions.NAMESPACEURI_DXX);
    parse();
  }

  private void parse() {
    for (EditOp eo : editScript) {
      switch (eo.getType()) {
      case DELETE:
        addMapping((EditOpDelete) eo);
        break;
      case INSERT:
        addMapping((EditOpInsert) eo);
        break;
      case MOVE:
        EditOpMove mov = (EditOpMove) eo;
        addMapping(mov);
        break;
      case UPDATE:
        addMapping((EditOpUpdate) eo);
        break;
      case SPLIT:
        throw new UnsupportedOperationException();
      }
    }
  }

  private void addMapping(EditOpUpdate upd) {
    Effect e = addEffect(upd.getUpdatedNode());
    e.setIsUpdated(upd);
  }

  private void addMapping(EditOpMove mov) {
    Effect e = addEffect(mov.getToParent());
    e.addNewChild(mov);

    e = addEffect(mov.getMovedNode());
    e.setIsMoved(mov);
  }

  private void addMapping(EditOpInsert ins) {
    Effect e = addEffect(ins.getParent());
    e.addNewChild(ins);

    e = addEffect(ins.getInsertedNode());
    e.setIsInserted(ins);
  }

  private void addMapping(EditOpDelete del) {
    Effect e = addEffect(del.getDeletedNode());
    e.setIsDeleted(del);
  }

  private Effect addEffect(DiffNode key) {
    Effect l = effects.get(key);
    if (l == null)
      effects.put(key, l = new Effect());
    return l;
  }

  public void apply() {
    processRemoves();
    processNonRemoves();
    addEqualAttribute(root.getOwnerDocument().getDocumentElement());
    processDeltaInfo();
  }
  
  private void processNode(Node node, List<Node> nodesWithDeltaInfo) {
    DeltaInfo info = (DeltaInfo) node.getUserData(DELTAINFO_KEY);
    if (info != null) {
      nodesWithDeltaInfo.add(node);
    }
    Node childNode = node.getFirstChild();
    while (childNode != null) {
      processNode(childNode, nodesWithDeltaInfo);
      childNode = childNode.getNextSibling();
    }
  }
  
  private void processDeltaInfo() {
    ArrayList<Node> nodesWithDeltaInfo = new ArrayList<Node>();
    processNode(root, nodesWithDeltaInfo);
    for (Node node : nodesWithDeltaInfo) {
      DeltaInfo info = getDeltaInfo(node);
      
      /* deleted nodes */
      Iterator<DeleteInfo> deleteInfoIter = info.getDeletedNodes();
      if (deleteInfoIter != null) {
        while (deleteInfoIter.hasNext()) {
          DeleteInfo deletedInfo = deleteInfoIter.next();
          Node deletedNode = deletedInfo.deletedNode;
          
          Node nextSiblingNode = deletedInfo.nextSiblingNode;
          Node prevSiblingNode = deletedInfo.prevSiblingNode;
          if (nextSiblingNode == null) {
            node.appendChild(deletedNode);
          } else if (prevSiblingNode == null) {
            node.insertBefore(deletedNode, node.getFirstChild());
          } else if (XMLUtils.containsNode(node.getChildNodes(), nextSiblingNode)) {
            node.insertBefore(deletedNode, nextSiblingNode);
          } else if (XMLUtils.containsNode(node.getChildNodes(), prevSiblingNode)) {
            node.insertBefore(deletedNode, prevSiblingNode.getNextSibling());
          } else {
            int pos = deletedInfo.position;
            if (pos >= node.getChildNodes().getLength()-1) {
              node.appendChild(deletedNode);
            } else {
              int insertPos = Math.max(deletedInfo.position, node.getChildNodes().getLength()-1);
              Node refChild = node.getChildNodes().item(insertPos);
              node.insertBefore(deletedNode, refChild);
            }
          }
 
          DeltaInfo deletedNodeDeltaInfo = getDeltaInfo(deletedNode);
          if (deletedNodeDeltaInfo != null && deletedNodeDeltaInfo.getTextGroupElem() != null) {
            deletedNode.getParentNode().replaceChild(deletedNodeDeltaInfo.getTextGroupElem(), deletedNode);
          }
        }
      }
      
      /* textGroup elements: */
      Element textGroupElem = info.getTextGroupElem();
      if (textGroupElem != null) {
        node.getParentNode().replaceChild(textGroupElem, node);
      }
      
      /* attribute elements: */
      Element attrsElem = info.getAttrsElem();
      if (attrsElem != null) {
        node.insertBefore(attrsElem, node.getFirstChild());
      }
    }
  }
  
  private void markAncestorsChanged(Node node) {
    Node parent = (Node) node.getParentNode();
    while (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
      setDeltaV2Attr((Element) parent, "A!=B");
      parent = parent.getParentNode();
    }
  }
  
  private void addEqualAttribute(Element elem) {
    if (!elem.hasAttributeNS(Definitions.NAMESPACEURI_DELTAXML, "deltaV2"))
      elem.setAttributeNS(Definitions.NAMESPACEURI_DELTAXML, Definitions.PREFIX_DELTAXML + ":deltaV2", "A=B");
    Node child = elem.getFirstChild();
    while (child != null) {
      if (child.getNodeType() == Node.ELEMENT_NODE)
        addEqualAttribute((Element) child);
      child = child.getNextSibling();
    }
  }
  
  private void appendTextElem(Element textGroupElem, String text, String delta) {
    Element textElem = (Element) textGroupElem.appendChild(textGroupElem.getOwnerDocument().createElementNS(Definitions.NAMESPACEURI_DELTAXML, Definitions.PREFIX_DELTAXML + ":text"));
    textElem.appendChild(textGroupElem.getOwnerDocument().createTextNode(text));
    setDeltaV2Attr(textElem, delta);
  }
  
  private Element createTextGroupElem(Document doc, String oldValue, String newValue) {
    Element textGroupElem = doc.createElementNS(Definitions.NAMESPACEURI_DELTAXML, Definitions.PREFIX_DELTAXML + ":textGroup");
    String delta;
    if (oldValue != null && newValue == null)
      delta = "A";
    else if (oldValue == null && newValue != null)
      delta = "B";
    else
      delta = "A!=B";    
    setDeltaV2Attr(textGroupElem, delta);
    if (oldValue != null)
      appendTextElem(textGroupElem, oldValue, "A");
    if (newValue != null)
      appendTextElem(textGroupElem, newValue, "B");
    return textGroupElem;
  }
  
  private DeltaInfo getDeltaInfo(Node node) {
    DeltaInfo info = (DeltaInfo) node.getUserData(DELTAINFO_KEY);
    if (info == null) {
      info = new DeltaInfo();
      node.setUserData(DELTAINFO_KEY, info, null);
    }
    return info;
  }
  
  private String getLocalName(Node node) {
    return node.getNamespaceURI() == null ? node.getNodeName() : node.getLocalName();
  }
  
  private void setDeltaV2Attr(Element elem, String delta) {
    elem.setAttributeNS(Definitions.NAMESPACEURI_DELTAXML, Definitions.PREFIX_DELTAXML + ":deltaV2", delta);
  }
  
  private Element getAttributeContainer(Document doc, Attr attr, String delta) {
    Element elem = doc.createElementNS(attr.getNamespaceURI() == null ? Definitions.NAMESPACEURI_DXA : attr.getNamespaceURI(), 
        (attr.getNamespaceURI() == null) ? Definitions.PREFIX_DXA + ":" +  getLocalName(attr) : attr.getPrefix() + ":" + getLocalName(attr));
    setDeltaV2Attr(elem, delta);
    return elem;
  }
  
  private void appendAttrValueElem(Document doc, Element parent, String delta, String value) {
    Element attrValueElem = doc.createElementNS(Definitions.NAMESPACEURI_DELTAXML, Definitions.PREFIX_DELTAXML + ":attributeValue");
    setDeltaV2Attr(attrValueElem, delta);
    attrValueElem.appendChild(doc.createTextNode(value));
    parent.appendChild(attrValueElem);
  }
  
  private void processAttributeChanges(Element elem, NamedNodeMap newAttrs) {
    Collection<Pair<Attr, Attr>> updatedAttributes = new HashSet<Pair<Attr, Attr>>();
    Collection<Attr> insertedAttributes = new HashSet<Attr>();
    Collection<Attr> deletedAttributes = new HashSet<Attr>();
    
    NamedNodeMap oldAttrs = elem.getAttributes();
    for (int i=0; i<oldAttrs.getLength(); i++) {
      Attr oldAttr = (Attr) oldAttrs.item(i);
      Attr newAttr = (oldAttr.getNamespaceURI() == null) ? 
          (Attr) newAttrs.getNamedItem(oldAttr.getName()) : 
            (Attr) newAttrs.getNamedItemNS(oldAttr.getNamespaceURI(), oldAttr.getLocalName()); 
      if (newAttr == null)
        deletedAttributes.add(oldAttr);
      else if (!oldAttr.getValue().equals(newAttr.getValue()))
        updatedAttributes.add(new ImmutablePair<Attr, Attr>(oldAttr, newAttr));
    }
    
    for (int i=0; i<newAttrs.getLength(); i++) {
      Attr newAttr = (Attr) newAttrs.item(i);
      Attr oldAttr = (newAttr.getNamespaceURI() == null) ? 
          (Attr) oldAttrs.getNamedItem(newAttr.getName()) : 
            (Attr) oldAttrs.getNamedItemNS(newAttr.getNamespaceURI(), newAttr.getLocalName()); 
      if (oldAttr == null)
        insertedAttributes.add(newAttr);
    }

    if (!updatedAttributes.isEmpty() || !insertedAttributes.isEmpty() || !deletedAttributes.isEmpty()) {
      Document doc = elem.getOwnerDocument();
      Element attributesElem = doc.createElementNS(Definitions.NAMESPACEURI_DELTAXML, Definitions.PREFIX_DELTAXML + ":attributes");
      String delta;
      if (!insertedAttributes.isEmpty() && deletedAttributes.isEmpty())
        delta = "B";
      else if (insertedAttributes.isEmpty() && !deletedAttributes.isEmpty())
        delta = "A";
      else 
        delta = "A!=B";
      setDeltaV2Attr(attributesElem, delta);
      
      /* Updates: */
      for (Pair<Attr, Attr> attrs : updatedAttributes) {
        if (StringUtils.equals(attrs.getLeft().getNamespaceURI(), Definitions.NAMESPACEURI_DELTAXML))
          continue;
        Element attrElem = (Element) attributesElem.appendChild(getAttributeContainer(doc, attrs.getLeft(), "A!=B"));
        appendAttrValueElem(doc, attrElem, "A", attrs.getLeft().getValue());
        appendAttrValueElem(doc, attrElem, "B", attrs.getRight().getValue());
        elem.removeAttributeNode(attrs.getLeft());
      }
      
      /* Deletes */
      for (Attr attr : deletedAttributes) {
        if (StringUtils.equals(attr.getNamespaceURI(), Definitions.NAMESPACEURI_DELTAXML))
          continue;
        Element attrElem = (Element) attributesElem.appendChild(getAttributeContainer(doc, attr, "A"));
        appendAttrValueElem(doc, attrElem, "A", attr.getValue());
        elem.removeAttributeNode(attr);
      }
      
      /* Inserts */
      for (Attr attr : insertedAttributes) {
        if (StringUtils.equals(attr.getNamespaceURI(), Definitions.NAMESPACEURI_DELTAXML))
          continue;
        Element attrElem = (Element) attributesElem.appendChild(getAttributeContainer(doc, attr, "B"));
        appendAttrValueElem(doc, attrElem, "B", attr.getValue());
      }
      
      getDeltaInfo(elem).addAttributesElem(attributesElem);
    }
    
  }

  private void processRemoves() {
    ListIterator<EditOp> i = editScript.listIterator(editScript.size());
    while (i.hasPrevious()) {
      EditOp op = i.previous();
      Node deletedNode;
      if (op.getType() == Operation.DELETE)
        deletedNode = (Node) ((EditOpDelete) op).getDeletedNode().getNativeNode();
      else if (op.getType() == Operation.MOVE)
        deletedNode = (Node) ((EditOpMove) op).getMovedNode().getNativeNode();
      else
        continue;
      
      Node deltaInfoNode = (op.getType() == Operation.DELETE) ? deletedNode : deletedNode.cloneNode(true);
      
      getDeltaInfo(deletedNode.getParentNode()).addDeletedNode(deltaInfoNode);
      
      if (deletedNode.getNodeType() == Node.ELEMENT_NODE) {
        setDeltaV2Attr((Element) deltaInfoNode, "A");
      } else if (deletedNode.getNodeType() == Node.TEXT_NODE) {
        Element textGroupElem = createTextGroupElem(deltaInfoNode.getOwnerDocument(), deltaInfoNode.getTextContent(), null);
        getDeltaInfo(deltaInfoNode).addTextGroupElem(textGroupElem);
      }
      
      markAncestorsChanged(deletedNode);
      
      deletedNode.getParentNode().removeChild(deletedNode);
    }
  }

  private void processNonRemoves() {
    for (Entry<DiffNode, Effect> e : effects.entrySet()) {
      // DiffNode affectedParent = e.getKey();
      Node affectedParent = (Node) e.getKey().getNativeNode();
      Effect effect = e.getValue();

      if (effect.isUpdated()) {
        EditOpUpdate upd = effect.getUpdateOp();
        Node updatedNode = (Node) upd.getUpdatedNode().getNativeNode();
        NodeUpdate newValue = (NodeUpdate) upd.getNewNodeValue();
        if (updatedNode.getNodeType() == Node.ELEMENT_NODE) {
          processAttributeChanges((Element) updatedNode, newValue.attributes);
          setDeltaV2Attr((Element) updatedNode, "A!=B");
        } else if (updatedNode.getNodeType() == Node.TEXT_NODE) {
          Element textGroupElem = createTextGroupElem(updatedNode.getOwnerDocument(), updatedNode.getTextContent(), newValue.value);
          getDeltaInfo(updatedNode).addTextGroupElem(textGroupElem);
        }
        markAncestorsChanged(updatedNode);
      }

      Iterator<InsertOp> insIt = effect.getInserts().iterator();
      if (!insIt.hasNext())
        continue;

      int i = 0;
      InsertOp ins = insIt.next();
      // DiffNode child = affectedParent.getFirstChild();
      Node child = affectedParent.getFirstChild();

      L2: while ((ins != null) || (child != null)) {
        while ((ins != null) && (ins.getFinalPosition() == i)) {
          DiffNode newDiffNodeChild = ins.getInsertedNode();
          Node newNodeChild = (Node) newDiffNodeChild.getNativeNode();
          
          if (newNodeChild.getNodeType() == Node.ELEMENT_NODE) {
            setDeltaV2Attr((Element) newNodeChild, "B");
          } else if (newNodeChild.getNodeType() == Node.TEXT_NODE) {
            Element textGroupElem = createTextGroupElem(newNodeChild.getOwnerDocument(), null, newNodeChild.getTextContent());
            getDeltaInfo(newNodeChild).addTextGroupElem(textGroupElem);
          }
          
          // affectedParent.appendOrInsert(newDiffNodeChild, child);
          affectedParent.insertBefore(newNodeChild, child);

          markAncestorsChanged(newNodeChild);
          
          /*
          if (ASSERTIONS && (ins.getInsertedNode().indexOf() != ins.getFinalPosition()))
            throw new AssertionError();
          */

          if (!insIt.hasNext())
            // No more inserts, no point going on...
            break L2;

          ins = insIt.next();
          ++i;
        }

        if (child != null) {
          child = child.getNextSibling();
          ++i;
        }
      }
    }
  }

  public static final class Effect {
    
    private EditOpUpdate upd;
    private EditOpMove mov;
    private EditOpInsert ins;
    private EditOpDelete del;
    private boolean removed;
    private boolean sorted;
    private List<InsertOp> inserts = null;

    public void setIsUpdated(EditOpUpdate upd) {
      this.upd = upd;
    }

    public boolean isUpdated() {
      return upd != null;
    }

    public EditOpUpdate getUpdateOp() {
      return upd;
    }

    public void setIsMoved(EditOpMove mov) {
      this.mov = mov;
    }

    public boolean isMoved() {
      return mov != null;
    }

    public EditOpMove getMoveOp() {
      return mov;
    }

    public void setIsInserted(EditOpInsert ins) {
      this.ins = ins;
    }

    public boolean isInserted() {
      return ins != null;
    }

    public EditOpInsert getInsertOp() {
      return ins;
    }

    public void setIsDeleted(EditOpDelete del) {
      this.del = del;
    }

    public boolean isDeleted() {
      return del != null;
    }

    public EditOpDelete getDeleteOp() {
      return del;
    }

    public void addNewChild(EditOpInsert ins) {
      getInsertsForWriting().add(new InsertOp(ins, ins.getInsertedNode(), ins.getFinalPosition()));
    }

    public void addNewChild(EditOpMove mov) {
      getInsertsForWriting().add(new InsertOp(mov, mov.getMovedNode(), mov.getFinalPosition()));
    }

    private List<InsertOp> getInsertsForWriting() {
      sorted = false;
      if (inserts == null)
        inserts = new LinkedList<>();
      return inserts;
    }

    public Collection<InsertOp> getInserts() {
      if (inserts == null)
        return Collections.emptyList();
      if (!sorted) {
        Collections.sort(inserts);
        sorted = true;
      }
      return inserts;
    }

    public boolean isRemoved() {
      return removed;
    }

    public void setRemoved(boolean removed) {
      this.removed = removed;
    }
  }

  public static final class InsertOp implements Comparable<InsertOp> {
    
    private final EditOp op;
    private final int finalPosition;
    private final DiffNode insertedNode;

    public InsertOp(EditOp op, DiffNode insertedNode, int finalPosition) {
      this.op = op;
      this.finalPosition = finalPosition;
      this.insertedNode = insertedNode;
    }

    public EditOp getOp() {
      return op;
    }

    public int getFinalPosition() {
      return finalPosition;
    }

    public DiffNode getInsertedNode() {
      return insertedNode;
    }

    @Override
    public int compareTo(InsertOp o) {
      return Integer.compare(finalPosition, o.finalPosition);
    }
  }
}