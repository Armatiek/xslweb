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
package nl.armatiek.xslweb.saxon.functions.diff.node;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import nl.armatiek.xslweb.saxon.functions.diff.xydiff.Hash32;

public interface TreeNode {
  
  // Tree manipulation
  public int getNodeType();
  
  public TreeNode getParent();

  public void setParent(TreeNode parent);

  public List<TreeNode> getChildren();
  
  public ArrayList<TreeNode> getDeletedChildren();

  public TreeNode getChild(int childPos);

  public void insertChild(int pos, TreeNode child);

  public boolean removeChild(TreeNode child);

  public TreeNode removeChild(int pos);

  public void appendChild(TreeNode child);

  public int getChildPosition(TreeNode child);

  public void computePath();

  public String getPath();

  public String getLastComputedPath();

  public TreeNode getNextSibling();

  public TreeNode getPreviousSibling();

  public TreeNode getFirstChild();

  public TreeNode getLastChild();

  public Hashtable<String, String> getAttributes();

  public String getAttribute(String name);

  public void setAttribute(String name, String value);

  public void removeAttribute(String name);

  public boolean hasAttributes();

  public boolean hasChildren();

  // XyDiff requirement
  public double getWeight();

  public Hash32 getHash32();

  public String getId();

  // Xml export
  public void exportXML(Writer writer, boolean split) throws IOException;

  // Node content comparison
  public boolean equalsContent(Object node);

  // Node content
  public boolean allowAttributes();

  public boolean allowChildren();
  
  public TreeNode getDescendantByPos(String pos);
  
  public void setDeletedFromPos(int pos);
  
  public int getDeletedFromPos();
  
  public void setDeltaXmlAttributes(ElementNode attrs);
  
  public ElementNode getDeltaXmlAttributes();
  
  public void markAsUpdateOld();
  
  public void markAsUpdateNew();
  
  public boolean isMarkedAsUpdateOld();
  
  public boolean isMarkedAsUpdateNew();
  
}