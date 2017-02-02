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

public class DeleteNode extends XMLCommand {
  
  private TreeNode node;
  private boolean isMoved;
  private boolean isUpdated;

  public DeleteNode(String nodePath, TreeNode node) {
    super(nodePath);
    this.node = node;
    this.type = XMLCommand.DELETE_NODE;
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
    String s = ("DeleteNode: " + node.toString() + " path " + this.nodePath);

    if (isMoved) {
      s += " (move)";
    }

    if (this.isUpdated) {
      s += " (update)";
    }

    return s;
  }

  public ElementNode toXML() {
    try {
      ElementNode d = new ElementNode("Deleted");
      d.setAttribute("pos", this.nodePath);

      if (this.isMoved) {
        d.setAttribute("move", "yes");
      }

      if (this.isUpdated) {
        d.setAttribute("update", "yes");
      }

      d.appendChild(node);

      return d;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }
}
