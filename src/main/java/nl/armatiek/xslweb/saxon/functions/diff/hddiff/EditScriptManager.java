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
import nl.armatiek.xslweb.saxon.functions.diff.hddiff.DeltaInfo.AttrInfo;
import nl.armatiek.xslweb.saxon.functions.diff.hddiff.DeltaInfo.DeleteInfo;
import nl.armatiek.xslweb.saxon.functions.diff.hddiff.DeltaInfo.TextInfo;
import nl.armatiek.xslweb.saxon.functions.diff.hddiff.NodeDiffNodeAdapter.NodeUpdate;
import nl.armatiek.xslweb.utils.XMLUtils;

public class EditScriptManager {
  
  private static final String USERDATA_KEY_DELTAINFO = "deltaInfo";
  
  private static final boolean ASSERTIONS = true;
  
  private final List<EditOp> editScript;
  private final HashMap<DiffNode, Effect> effects;
  private Node root;

  public EditScriptManager(List<EditOp> editScript, Node root) {
    this.editScript = editScript;
    this.effects = new HashMap<>(editScript.size());
    this.root = root.getNodeType() == Node.DOCUMENT_NODE ? ((Document) root).getDocumentElement() : root;
    ((Element) this.root).setAttributeNS(Definitions.NAMESPACEURI_XMLNS, "xmlns:" + Definitions.PREFIX_DELTAXML, Definitions.NAMESPACEURI_DELTAXML);
    ((Element) this.root).setAttributeNS(Definitions.NAMESPACEURI_XMLNS, "xmlns:" + Definitions.PREFIX_DXA, Definitions.NAMESPACEURI_DXA);
    ((Element) this.root).setAttributeNS(Definitions.NAMESPACEURI_XMLNS, "xmlns:" + Definitions.PREFIX_DXX, Definitions.NAMESPACEURI_DXX);
    parse();
  }
  
  private DiffNode getNodeFromEditOp(EditOp eo) {
    switch (eo.getType()) {
    case DELETE:
      return ((EditOpDelete) eo).getDeletedNode();
    case INSERT:
      return ((EditOpInsert) eo).getInsertedNode();
    case MOVE:
      return ((EditOpMove) eo).getMovedNode();
    case UPDATE:
      return null;
      // return ((EditOpUpdate) eo).getUpdatedNode();
    default:
      throw new UnsupportedOperationException();
    }
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
        addMapping((EditOpMove) eo);
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
    processDeltaInfo(root);
    complementDeltaAttributes(root.getOwnerDocument().getDocumentElement());
  }
  
  private void getNodesToProcess(Node node, List<Node> nodes) {
    DeltaInfo info = (DeltaInfo) node.getUserData(USERDATA_KEY_DELTAINFO);
    if (info != null) {
      nodes.add(node);
      if (info.hasInsertInfo())
        return;
    }
    Node childNode = node.getFirstChild();
    while (childNode != null) {
      getNodesToProcess(childNode, nodes);
      childNode = childNode.getNextSibling();
    }
  }
  
  private void removeDeltaInfoFromDescendants(Node node) {
    Node childNode = node.getFirstChild();
    while (childNode != null) {
      childNode.setUserData(USERDATA_KEY_DELTAINFO, null, null);
      if (childNode.getNodeType() == Node.ELEMENT_NODE)
        ((Element) childNode).removeAttributeNS(Definitions.NAMESPACEURI_DELTAXML, Definitions.ATTRNAME_DELTA);
      removeDeltaInfoFromDescendants(childNode);
      childNode = childNode.getNextSibling();
    }
  }
  
  private int processDeltaInfo(Node root) {
    ArrayList<Node> nodesToProcess = new ArrayList<Node>();
    getNodesToProcess(root, nodesToProcess);
    for (Node node : nodesToProcess) {
      DeltaInfo info = (DeltaInfo) node.getUserData(USERDATA_KEY_DELTAINFO);
      
      /* Process inserted node: */
      if (info.hasInsertInfo()) {
        setDeltaAttr((Element) node, "B");
        removeDeltaInfoFromDescendants(node);
        continue;
      }
      
      /* Process deleted child nodes: */
      if (info.hasDeleteInfo()) {
        /* Process deleted nodes */
        Iterator<DeleteInfo> deleteInfoIter = info.getDeletedNodes();
        if (deleteInfoIter != null) {
          while (deleteInfoIter.hasNext()) {
            DeleteInfo deletedInfo = deleteInfoIter.next();
            Node deletedNode = deletedInfo.deletedNode;
            Node nextSiblingNode = deletedInfo.nextSiblingNode;
            Node prevSiblingNode = deletedInfo.prevSiblingNode;
            
            if (nextSiblingNode != null && nextSiblingNode.getParentNode() != null && XMLUtils.containsNode(node, nextSiblingNode)) {
              node.insertBefore(deletedNode, nextSiblingNode);
            } else if (prevSiblingNode != null 
                && ((prevSiblingNode.getNextSibling() == null || prevSiblingNode.getNextSibling().getParentNode() != null) 
                    && XMLUtils.containsNode(node, prevSiblingNode))) {
              node.insertBefore(deletedNode, prevSiblingNode.getNextSibling());
            } else if (nextSiblingNode == null) {
              node.appendChild(deletedNode);
            } else if (prevSiblingNode == null) {
              node.insertBefore(deletedNode, node.getFirstChild());
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
          }
          deleteInfoIter = info.getDeletedNodes();
          while (deleteInfoIter.hasNext()) {
            DeleteInfo deletedInfo = deleteInfoIter.next();
            Node deletedNode = deletedInfo.deletedNode;
            removeDeltaInfoFromDescendants(deletedNode);
            if (deletedNode.getNodeType() == Node.ELEMENT_NODE) {
              setDeltaAttr((Element) deletedNode, "A");
            } else {
              Element textGroupElem = createTextGroupElem(node.getOwnerDocument(), deletedNode.getTextContent(), null);
              node.replaceChild(textGroupElem, deletedNode);
            }
          }
        }
      }
      
      /* Process inserted or changed text nodes */
      if (info.hasTextInfo()) {
        TextInfo textInfo = info.getTextInfo();
        Element textGroupElem  = createTextGroupElem(node.getOwnerDocument(), textInfo.oldValue, textInfo.newValue);
        node.getParentNode().replaceChild(textGroupElem, node);
      }
      
      /* Process changed atribute elements: */
      if (info.hasAttrInfo()) {
        AttrInfo attrInfo = info.getAttrInfo();
        replaceAttributes((Element) node, attrInfo.elem.getAttributes());
        Element attrsElem = createAttributesElement((Element) node, attrInfo.newAttrs);
        node.insertBefore(attrsElem, node.getFirstChild());
        setDeltaAttr((Element) node, "A!=B");
      }
      
      node.setUserData(USERDATA_KEY_DELTAINFO, null, null);
    }
    return nodesToProcess.size();
  }
  
  private void markAncestorsChanged(Node node) {
    Node parent = (Node) node.getParentNode();
    while (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
      setDeltaAttr((Element) parent, "A!=B");
      parent = parent.getParentNode();
    }
  }
  
  private void markChanged(Node node) {
    String value = getDeltaAttr(node);
    if (value != null && (value.equals("A") || value.equals("B"))) {
      markAncestorsChanged(node);
      return;
    }
    Node child = node.getFirstChild();
    while (child != null) {
      if (child.getNodeType() == Node.ELEMENT_NODE)
        markChanged(child);
      child = child.getNextSibling();
    }
  }
  
  private void markUnchanged(Node node) {
    String value = getDeltaAttr(node);
    if (value != null && (value.equals("A") || value.equals("B")))
      return;
    if (getDeltaAttr(node) == null) {
      setDeltaAttr((Element) node, "A=B");
      return;
    }
    Node child = node.getFirstChild();
    while (child != null) {
      if (child.getNodeType() == Node.ELEMENT_NODE)
        markUnchanged(child);
      child = child.getNextSibling();
    }
  }
  
  private void complementDeltaAttributes(Node node) {
    markChanged(node);
    markUnchanged(node);
  }
  
  private void appendTextElem(Element textGroupElem, String text, String delta) {
    Element textElem = (Element) textGroupElem.appendChild(textGroupElem.getOwnerDocument().createElementNS(Definitions.NAMESPACEURI_DELTAXML, Definitions.PREFIX_DELTAXML + ":text"));
    textElem.appendChild(textGroupElem.getOwnerDocument().createTextNode(text));
    setDeltaAttr(textElem, delta);
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
    setDeltaAttr(textGroupElem, delta);
    if (oldValue != null)
      appendTextElem(textGroupElem, oldValue, "A");
    if (newValue != null)
      appendTextElem(textGroupElem, newValue, "B");
    return textGroupElem;
  }
  
  private DeltaInfo getDeltaInfo(Node node) {
    DeltaInfo info = (DeltaInfo) node.getUserData(USERDATA_KEY_DELTAINFO);
    if (info == null) {
      info = new DeltaInfo();
      node.setUserData(USERDATA_KEY_DELTAINFO, info, null);
    }
    return info;
  }
  
  private String getLocalName(Node node) {
    return node.getNamespaceURI() == null ? node.getNodeName() : node.getLocalName();
  }
  
  private String getDeltaAttr(Node node) {
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      String value = ((Element) node).getAttributeNS(Definitions.NAMESPACEURI_DELTAXML, Definitions.ATTRNAME_DELTA);
      return value.equals("") ? null : value;
    }
    return null;
  }
  
  private void setDeltaAttr(Element elem, String delta, boolean overwrite) {
    boolean overwriteAllowed = overwrite || !elem.hasAttributeNS(Definitions.NAMESPACEURI_DELTAXML, Definitions.ATTRNAME_DELTA);
    if (overwriteAllowed)
      elem.setAttributeNS(Definitions.NAMESPACEURI_DELTAXML, Definitions.PREFIX_DELTAXML + ":" + Definitions.ATTRNAME_DELTA, delta);
  }
  
  private void setDeltaAttr(Element elem, String delta) {
    setDeltaAttr(elem, delta, true); 
  }
  
  private Element getAttributeContainer(Document doc, Attr attr, String delta) {
    Element elem = doc.createElementNS(attr.getNamespaceURI() == null ? Definitions.NAMESPACEURI_DXA : attr.getNamespaceURI(), 
        (attr.getNamespaceURI() == null) ? Definitions.PREFIX_DXA + ":" +  getLocalName(attr) : attr.getPrefix() + ":" + getLocalName(attr));
    setDeltaAttr(elem, delta);
    return elem;
  }
  
  private void appendAttrValueElem(Document doc, Element parent, String delta, String value) {
    Element attrValueElem = doc.createElementNS(Definitions.NAMESPACEURI_DELTAXML, Definitions.PREFIX_DELTAXML + ":attributeValue");
    setDeltaAttr(attrValueElem, delta);
    attrValueElem.appendChild(doc.createTextNode(value));
    parent.appendChild(attrValueElem);
  }
  
  private void replaceAttributes(Element elem, NamedNodeMap newAttrs) {
    NamedNodeMap attrs = elem.getAttributes();
    while (attrs.getLength() > 0) {
      elem.removeAttributeNode((Attr) attrs.item(0));
    }
    for (int i=0; i<newAttrs.getLength(); i++) {
      Attr attr = (Attr) newAttrs.item(i);
      elem.setAttributeNode((Attr) elem.getOwnerDocument().importNode(attr.cloneNode(true), true));
    }
  }
  
  private Element createAttributesElement(Element elem, NamedNodeMap newAttrs) {
    Collection<Pair<Attr, Attr>> updatedAttributes = new HashSet<Pair<Attr, Attr>>();
    Collection<Attr> insertedAttributes = new HashSet<Attr>();
    Collection<Attr> deletedAttributes = new HashSet<Attr>();
    
    /* Process deleted and updated attributes: */
    NamedNodeMap oldAttrs = elem.getAttributes();
    for (int i=0; i<oldAttrs.getLength(); i++) {
      Attr oldAttr = (Attr) oldAttrs.item(i);
      if (StringUtils.equals(oldAttr.getNamespaceURI(), Definitions.NAMESPACEURI_XMLNS))
        continue;
      Attr newAttr = (oldAttr.getNamespaceURI() == null) ? 
          (Attr) newAttrs.getNamedItem(oldAttr.getName()) : 
            (Attr) newAttrs.getNamedItemNS(oldAttr.getNamespaceURI(), oldAttr.getLocalName()); 
      if (newAttr == null)
        deletedAttributes.add(oldAttr);
      else if (!oldAttr.getValue().equals(newAttr.getValue()))
        updatedAttributes.add(new ImmutablePair<Attr, Attr>(oldAttr, newAttr));
    }
    
    /* Process inserted attributes: */
    for (int i=0; i<newAttrs.getLength(); i++) {
      Attr newAttr = (Attr) newAttrs.item(i);
      if (StringUtils.equals(newAttr.getNamespaceURI(), Definitions.NAMESPACEURI_XMLNS))
        continue;
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
      if (!insertedAttributes.isEmpty() && deletedAttributes.isEmpty() && updatedAttributes.isEmpty())
        delta = "B";
      else if (insertedAttributes.isEmpty() && !deletedAttributes.isEmpty() && updatedAttributes.isEmpty())
        delta = "A";
      else 
        delta = "A!=B";
      setDeltaAttr(attributesElem, delta);
      
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
      
      return attributesElem;
      
    }
    return null;
  }
  
  private boolean hasAncestorDelete(Node deleteNode, HashSet<Node> deleteNodes) {
    Node parentNode = deleteNode.getParentNode();
    while (parentNode != null) {
      if (deleteNodes.contains(parentNode)) 
        return true;
      parentNode = parentNode.getParentNode();
    }
    
    return false;
  }
  
  private void processRemoves() {
    /* Gather all nodes to be deleted and moved */
    HashSet<Node> deleteNodes = new HashSet<Node>();
    ListIterator<EditOp> i = editScript.listIterator();
    while (i.hasNext()) {
      EditOp op = i.next();
      if (op.getType() == Operation.DELETE || op.getType() == Operation.MOVE)
        deleteNodes.add((Node)getNodeFromEditOp(op).getNativeNode());
    }
    
    /* Make mapping of top ancestor nodes that will be deleted or moved and their clone */
    HashMap<Node, Node> topLevelClones = new HashMap<Node, Node>();
    for (Node node: deleteNodes) {
      if (!hasAncestorDelete(node, deleteNodes))
        topLevelClones.put(node, node.cloneNode(true));
    }
    
    i = editScript.listIterator(editScript.size());
    while (i.hasPrevious()) {
      EditOp op = i.previous();    
      switch (op.getType()) {
      case DELETE:
        EditOpDelete del = (EditOpDelete) op;
        DiffNode deletedNode = del.getDeletedNode();
        Node deletedNativeNode = (Node) deletedNode.getNativeNode();
        Node clonedNativeNode = topLevelClones.get(deletedNativeNode);
        if (clonedNativeNode == null)
          clonedNativeNode = deletedNativeNode.cloneNode(true); // TODO : is this necessary?
        getDeltaInfo(deletedNativeNode.getParentNode()).addDeletedInfo(clonedNativeNode, deletedNativeNode.getNextSibling(), 
            deletedNativeNode.getPreviousSibling(), XMLUtils.getNodePosition(deletedNativeNode));
        deletedNode.removeFromParent();
        break;
      case MOVE:
        EditOpMove mov = (EditOpMove) op;
        DiffNode movedNode = mov.getMovedNode();
        Node movedNativeNode = (Node) movedNode.getNativeNode();
        clonedNativeNode = topLevelClones.get(movedNativeNode);
        if (clonedNativeNode == null)
          clonedNativeNode = movedNativeNode.cloneNode(true); // TODO : is this necessary?
        getDeltaInfo(movedNativeNode.getParentNode()).addDeletedInfo(clonedNativeNode, movedNativeNode.getNextSibling(), 
            movedNativeNode.getPreviousSibling(), XMLUtils.getNodePosition(movedNativeNode));
        movedNode.removeFromParent();
        break;
      default:
        break;
      }
    }
  }

  private void processNonRemoves() {
    for (Entry<DiffNode, Effect> e : effects.entrySet()) {
      DiffNode affectedParent = e.getKey();
      Effect effect = e.getValue();

      if (effect.isUpdated()) {
        EditOpUpdate upd = effect.getUpdateOp();
        DiffNode updatedNode = upd.getUpdatedNode();
        Node updatedNativeNode = (Node) updatedNode.getNativeNode();
        NodeUpdate newValue = (NodeUpdate) upd.getNewNodeValue();
        DeltaInfo info = getDeltaInfo(updatedNativeNode);
        int nodeType = updatedNativeNode.getNodeType();
        if (nodeType == Node.ELEMENT_NODE) {
          info.addAttrInfo((Element) updatedNativeNode, newValue.attributes);
        } else if (nodeType == Node.TEXT_NODE) {
          info.addTextInfo(updatedNativeNode.getTextContent(), newValue.value);
        }
        updatedNode.setNodeValue(upd.getNewNodeValue());
      }
      
      Iterator<InsertOp> insIt = effect.getInserts().iterator();
      if (!insIt.hasNext())
        continue;

      int i = 0;
      InsertOp ins = insIt.next();
      DiffNode child = affectedParent.getFirstChild();

      L2: while ((ins != null) || (child != null)) {
        while ((ins != null) && (ins.getFinalPosition() == i)) {
          DiffNode newDiffNodeChild = ins.getInsertedNode();
          affectedParent.appendOrInsert(newDiffNodeChild, child);
          
          Node newNodeChild = (Node) newDiffNodeChild.getNativeNode();
          DeltaInfo info = getDeltaInfo(newNodeChild);
          if (newNodeChild.getNodeType() == Node.ELEMENT_NODE) {
            info.addInsertInfo(true);
          } else if (newNodeChild.getNodeType() == Node.TEXT_NODE) {
            info.addTextInfo(null, newNodeChild.getTextContent());
          }
          
          if (ASSERTIONS && (ins.getInsertedNode().indexOf() != ins.getFinalPosition()))
            throw new AssertionError();

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