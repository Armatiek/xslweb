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

import nl.armatiek.xslweb.saxon.functions.diff.node.ElementNode;
import nl.armatiek.xslweb.saxon.functions.diff.node.TreeNode;
import nl.armatiek.xslweb.saxon.functions.diff.util.DiffUtils;

public class InsertNode extends XMLCommand {
  
  private TreeNode node;
  private boolean isMoved;
  private boolean isUpdated;

  public InsertNode(String nodePath, TreeNode node) {
    super(nodePath);
    this.node = node;
    this.type = XMLCommand.INSERT_NODE;
    this.isMoved = false;
    this.isUpdated = false;
  }

  public void setIsMoved(boolean isMoved) {
    this.isMoved = isMoved;
  }

  public boolean getIsMoved() {
    return this.isMoved;
  }

  public void setIsUpdated(boolean isUpdated) {
    this.isUpdated = isUpdated;
  }

  public boolean getIsUpdated() {
    return this.isUpdated;
  }

  public TreeNode getNode() {
    return node;
  }

  public void setNode(TreeNode node) {
    this.node = node;
  }

  public String toString() {
    String s = ("InsertNode: " + this.node.toString() + " path " + this.nodePath);

    if (this.isMoved) {
      s += " (move)";
    }

    if (this.isUpdated) {
      s += " (update)";
    }

    return s;
  }

  public ElementNode toXML() {
    try {
      ElementNode i = new ElementNode("Inserted");
      i.setAttribute("pos", this.nodePath);

      if (this.isMoved) {
        i.setAttribute("move", "yes");
      }

      if (this.isUpdated) {
        i.setAttribute("update", "yes");
      }
      
      if (this.nodeId > -1) {
        i.setAttribute("id", Integer.toString(this.nodeId));
      }

      i.appendChild(DiffUtils.cloneTreeNode(node));

      return i;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }
}
