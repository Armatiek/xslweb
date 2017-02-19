/**
 * JXyDiff: An XML Diff Written in Java
 *
 * Contact: pascal.molli@loria.fr
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of QPL/CeCill
 *
 * See licences details in QPL.txt and CeCill.txt
 *
 * Initial developer: Raphael Tani
 * Initial Developer: Gregory Cobena
 * Initial Developer: Gerald Oster
 * Initial Developer: Pascal Molli
 * Initial Developer: Serge Abiteboul
 * 
 * Adaptions for XSLWeb by Maarten Kroon (maarten.kroon@armatiek.nl)
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package nl.armatiek.xslweb.saxon.functions.diff.xydiff;

import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import nl.armatiek.xslweb.saxon.functions.diff.node.Document;
import nl.armatiek.xslweb.saxon.functions.diff.node.ElementNode;
import nl.armatiek.xslweb.saxon.functions.diff.node.TreeNode;
import nl.armatiek.xslweb.saxon.functions.diff.util.DiffUtils;

public class DeltaConstructor {
  
  private TreeNode v0XML;
  private TreeNode v1XML;
  private NodesManager nodesManager;
  private int moveCount;
  private int updateCount;
  private Vector<XMLCommand> table;

  public DeltaConstructor(NodesManager incMappings, TreeNode fromXDD, TreeNode toXDD) throws Exception {
    this.v0XML = fromXDD;
    this.v1XML = toXDD;
    this.nodesManager = incMappings;
    this.moveCount = 0;
    this.updateCount = 0;
    table = new Vector<XMLCommand>();

    //
    v0XML.computePath();
    v1XML.computePath();
  }

  public Document getDeltaDocument() {
    Document delta = new Document();
    ElementNode root = new ElementNode("delta");

    for (Enumeration<XMLCommand> e = table.elements(); e.hasMoreElements();) {
      XMLCommand command = (XMLCommand) e.nextElement();
      root.appendChild(command.toXML());
    }

    delta.appendChild(root);

    return delta;
  }

  public void constructDeltaDocument() throws Exception {
    // ---- Clear Tables ----
    int v0rootID = nodesManager.sourceNumberOfNodes;
    int v1rootID = nodesManager.resultNumberOfNodes;

    // ---- Construct DELETE Operations script ----
    nodesManager.markOldTree(v0rootID);

    // ---- Construct INSERT Operations script ----
    nodesManager.markNewTree(v1rootID);

    // ---- Save Resulting XID-map ----
    TreeNode v1rootElement = v1XML;

    // ---- Compute WEAK MOVE Operations ----
    nodesManager.computeWeakMove(v0rootID);

    // ---- Detect UPDATE Operations ----
    nodesManager.detectUpdate(v0rootID);

    // ---- Add Attribute Operations ----
    addAttributeOperations(v1rootID);

    // ---- Add Delete & Insert operations to Delta ----
    constructDeleteScript(v0rootID, false);
    constructInsertScript(v1rootID, false);

    if (this.moveCount != 0) {
      throw new Exception("constructDelta: moveCount is not null");
    }

    if (this.updateCount != 0) {
      throw new Exception("constructDelta: updateCount is not null");
    }

    // ---- Done ----
    // ---- Reorder operation ----
    Vector<XMLCommand> tempo = new Vector<XMLCommand>();
    Object[] toSort = table.toArray();
    Arrays.sort(toSort);

    for (int i = 0; i < toSort.length; i++) {
      XMLCommand cmd = (XMLCommand) toSort[i];
      tempo.addElement(cmd);
    }

    table = tempo;
  }

  private void constructDeleteScript(int v0nodeID, boolean ancestorDeleted) throws Exception {
    AtomicInfo myAtomicInfo = this.nodesManager.getV0NodeInfo(v0nodeID);
    TreeNode node = this.nodesManager.getV0NodeByID(v0nodeID);

    // Apply to children first - note that they must be enumerated in reverse
    // order
    Vector<Integer> childList = new Vector<Integer>();
    int child = myAtomicInfo.firstChild;

    while (child != 0) {
      childList.addElement(new Integer(child));
      child = this.nodesManager.getV0NodeInfo(child).nextSibling;
    }

    for (int i = childList.size() - 1; i >= 0; i--) {
      constructDeleteScript(((Integer) childList.elementAt(i)).intValue(), myAtomicInfo.myEvent == AtomicInfo.NODEEVENT_DELETED);
    }

    // Apply to Self
    switch (myAtomicInfo.myEvent) {
    case AtomicInfo.NODEEVENT_NOP:
      break;

    case AtomicInfo.NODEEVENT_DELETED:
    case AtomicInfo.NODEEVENT_STRONGMOVE:
    case AtomicInfo.NODEEVENT_WEAKMOVE:
    case AtomicInfo.NODEEVENT_UPDATE_OLD:

      if (((myAtomicInfo.myEvent != AtomicInfo.NODEEVENT_DELETED) || (!ancestorDeleted))) {
        TreeNode parentNode = node.getParent();

        int myPosition = myAtomicInfo.myPosition;

        switch (myAtomicInfo.myEvent) {
        case AtomicInfo.NODEEVENT_DELETED:
        case AtomicInfo.NODEEVENT_UPDATE_OLD:

          TreeNode deleted = this.deltaDocImportDeleteNode(v0nodeID);
          DeleteNode d = new DeleteNode(node.getLastComputedPath(), deleted);
          d.setPos(myPosition);

          if (myAtomicInfo.myEvent == AtomicInfo.NODEEVENT_UPDATE_OLD) {
            this.updateCount++;
            d.setIsUpdated(true);
          }
          d.setNodeId(myAtomicInfo.myID);

          table.addElement(d);

          break;

        case AtomicInfo.NODEEVENT_STRONGMOVE:
        case AtomicInfo.NODEEVENT_WEAKMOVE:
          d = new DeleteNode(node.getLastComputedPath(), DiffUtils.cloneTreeNode(node));
          d.setIsMoved(true);
          d.setPos(myAtomicInfo.myPosition);
          d.setNodeId(myAtomicInfo.myID);
          table.addElement(d);
          this.moveCount++;

          break;

        default:
          throw new Exception("constructDeleteScript: Program can't possibly be here");
        }
      }

      break;

    case AtomicInfo.NODEEVENT_UPDATE_NEW:
    case AtomicInfo.NODEEVENT_INSERTED:
    default:
      System.err.println("Bad value myEvent=" + myAtomicInfo.myEvent + " in old tree");

      break;
    }
  }

  private void constructInsertScript(int v1nodeID, boolean ancestorInserted) throws Exception {
    AtomicInfo myAtomicInfo = this.nodesManager.getV1NodeInfo(v1nodeID);
    TreeNode node = this.nodesManager.getV1NodeByID(v1nodeID);

    // apply to Self first
    switch (myAtomicInfo.myEvent) {
    case AtomicInfo.NODEEVENT_NOP:
      break;

    case AtomicInfo.NODEEVENT_INSERTED:
    case AtomicInfo.NODEEVENT_STRONGMOVE:
    case AtomicInfo.NODEEVENT_WEAKMOVE:
    case AtomicInfo.NODEEVENT_UPDATE_NEW:

      if (((myAtomicInfo.myEvent != AtomicInfo.NODEEVENT_INSERTED) || (!ancestorInserted))) {
        int myPosition = myAtomicInfo.myPosition;
        TreeNode parentNode = node.getParent();

        switch (myAtomicInfo.myEvent) {
        case AtomicInfo.NODEEVENT_INSERTED:
        case AtomicInfo.NODEEVENT_UPDATE_NEW:

          TreeNode inserted = this.deltaDocImportInsertNode(v1nodeID);
          InsertNode i = new InsertNode(node.getLastComputedPath(), inserted);
          i.setPos(myPosition);
          i.setNodeId(myAtomicInfo.myID);

          if (myAtomicInfo.myEvent == AtomicInfo.NODEEVENT_UPDATE_NEW) {
            this.updateCount--;
            i.setIsUpdated(true);
          }

          table.addElement(i);

          break;

        case AtomicInfo.NODEEVENT_STRONGMOVE:
        case AtomicInfo.NODEEVENT_WEAKMOVE:
          i = new InsertNode(node.getLastComputedPath(), DiffUtils.cloneTreeNode(node));
          i.setPos(myAtomicInfo.myPosition);
          i.setNodeId(myAtomicInfo.myID);
          i.setIsMoved(true);
          table.addElement(i);
          this.moveCount--;
          addAttributeOperations(v1nodeID);

          break;

        default:
          throw new Exception("constructInsertScript: program can't possibly here");
        }
      }

      break;

    case AtomicInfo.NODEEVENT_DELETED:
    case AtomicInfo.NODEEVENT_UPDATE_OLD:
    default:
      System.err.println("Bad value myEvent=" + myAtomicInfo.myEvent + " in new tree");

      break;
    }

    // now apply to children
    int child = myAtomicInfo.firstChild;

    while (child != 0) {
      constructInsertScript(child, (myAtomicInfo.myEvent == AtomicInfo.NODEEVENT_INSERTED));
      child = this.nodesManager.getV1NodeInfo(child).nextSibling;
    }
  }

  private TreeNode deltaDocImportInsertNode(int v1nodeID) throws Exception {
    AtomicInfo myAtomicInfo = this.nodesManager.getV1NodeInfo(v1nodeID);
    TreeNode node = this.nodesManager.getV1NodeByID(v1nodeID);
    node = DiffUtils.cloneTreeNode(node, false);

    int child = myAtomicInfo.firstChild;

    while (child != 0) {
      AtomicInfo myChildInfo = this.nodesManager.getV1NodeInfo(child);

      if ((myChildInfo.myEvent == AtomicInfo.NODEEVENT_INSERTED) || (myChildInfo.myEvent == AtomicInfo.NODEEVENT_UPDATE_NEW)) {
        TreeNode childNode = this.deltaDocImportInsertNode(child);
        node.appendChild(childNode);
      }

      child = myChildInfo.nextSibling;
    }

    return node;
  }

  private TreeNode deltaDocImportDeleteNode(int v0nodeID) throws Exception {
    AtomicInfo myAtomicInfo = this.nodesManager.getV0NodeInfo(v0nodeID);
    TreeNode node = this.nodesManager.getV0NodeByID(v0nodeID);
    node = DiffUtils.cloneTreeNode(node, false);

    int child = myAtomicInfo.firstChild;

    while (child != 0) {
      AtomicInfo myChildInfo = this.nodesManager.getV0NodeInfo(child);

      if ((myChildInfo.myEvent == AtomicInfo.NODEEVENT_DELETED) || (myChildInfo.myEvent == AtomicInfo.NODEEVENT_UPDATE_OLD)) {
        TreeNode childNode = this.deltaDocImportDeleteNode(child);
        node.appendChild(childNode);
      }

      child = myChildInfo.nextSibling;
    }

    return node;
  }

  private void addAttributeOperations(int v1nodeID) throws Exception {
    TreeNode node = this.nodesManager.getV1NodeByID(v1nodeID);

    if (!this.nodesManager.v1Assigned(v1nodeID)) {
      return;
    } else if (!node.allowAttributes()) {
      return;
    }

    int v0nodeID = this.nodesManager.getV1NodeInfo(v1nodeID).myMatchID;
    TreeNode oldnode = this.nodesManager.getV0NodeByID(v0nodeID);
    Hashtable attributes = node.getAttributes();

    for (Enumeration e = attributes.keys(); e.hasMoreElements();) {
      String attr = (String) e.nextElement();
      String value = (String) attributes.get(attr);

      if (!oldnode.getAttributes().containsKey(attr)) {
        InsertAttribute ia = new InsertAttribute(node.getPath(), attr, value);
        ia.setPos(nodesManager.getV1NodeInfo(v1nodeID).myPosition);
        table.addElement(ia);
      } else {
        String oldValue = (String) oldnode.getAttributes().get(attr);

        if (!value.equals(oldValue)) {
          UpdateAttribute ua = new UpdateAttribute(node.getPath(), attr, oldValue, value);
          ua.setPos(nodesManager.getV1NodeInfo(v1nodeID).myPosition);
          table.addElement(ua);
        }
      }
    }

    Hashtable oldAttributes = oldnode.getAttributes();

    for (Enumeration e = oldAttributes.keys(); e.hasMoreElements();) {
      String oldName = (String) e.nextElement();
      String oldValue = (String) oldAttributes.get(oldName);

      if (!node.getAttributes().containsKey(oldName)) {
        DeleteAttribute da = new DeleteAttribute(node.getPath(), oldName);
        da.setPos(nodesManager.getV1NodeInfo(v1nodeID).myPosition);
        table.addElement(da);
      }
    }

    // now apply to Children
    int child = this.nodesManager.getV1NodeInfo(v1nodeID).firstChild;

    while (child != 0) {
      addAttributeOperations(child);
      child = this.nodesManager.getV1NodeInfo(child).nextSibling;
    }
  }

  public Collection getXMLCommand() {
    return table;
  }
}
