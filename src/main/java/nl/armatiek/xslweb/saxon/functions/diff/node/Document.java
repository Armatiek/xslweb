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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;

import org.w3c.dom.Node;

import nl.armatiek.xslweb.saxon.functions.diff.xydiff.Hash32;

public class Document extends AbstractTreeNode {
  
  private static final long serialVersionUID = 1L;
  
  private boolean markDeletions;
  
  public Document() {
    this("1.0", null, false);
  }
  
  public Document(boolean markDeletions) {
    this("1.0", null, markDeletions);
  }
  
  public Document(String version, String encoding) {
    this(version, encoding, false);
  }
  
  public Document(String version, String encoding, boolean markDeletions) {
    super(true, true);

    if (encoding != null) {
      this.setAttribute("encoding", encoding);
    }

    if (version != null) {
      this.setAttribute("version", version);
    }
    
    this.markDeletions = markDeletions;
  }
  
  public int getNodeType() {
    return Node.DOCUMENT_NODE;
  }

  public String getVersion() {
    return (String) attributes.get("version");
  }

  public String getEncoding() {
    return (String) attributes.get("encoding");
  }

  public void setEncoding(String encoding) {
    attributes.put("encoding", encoding);
  }

  public String getStandalone() {
    return (String) attributes.get("standalone");
  }

  public void setStandalone(String standalone) {
    attributes.put("standalone", standalone);
  }

  public void setVersion(String version) {
    attributes.put("version", version);
  }

  public void save(String fileName) throws IOException {
    this.save(fileName, false);
  }

  public void save(String fileName, boolean split) throws IOException {
    FileOutputStream fos = new FileOutputStream(fileName);
    String charset = getEncoding();
    OutputStreamWriter writer;

    if (charset != null) {
      writer = new OutputStreamWriter(fos, charset);
    } else {
      writer = new OutputStreamWriter(fos, "UTF-8");
    }

    exportXML(writer, split);
    writer.flush();
    writer.close();
    fos.close();
  }

  public void exportXML(Writer writer, boolean split) throws IOException {
    // write header
    writer.write("<?xml version=\"");
    writer.write(getVersion());
    writer.write("\"");

    if (getEncoding() != null) {
      writer.write(" encoding=\"");
      writer.write(getEncoding());
      writer.write("\"");
    }

    if (getStandalone() != null) {
      writer.write(" standalone=\"");
      writer.write(getStandalone());
      writer.write("\"");
    }

    writer.write("?>");

    // write children
    for (Iterator<TreeNode> i = children.iterator(); i.hasNext();) {
      TreeNode node = (TreeNode) i.next();
      node.exportXML(writer, split);
    }

    writer.flush();
  }

  public Hash32 getHash32() {
    String s = getVersion() + "|" + getEncoding() + "|" + getStandalone();

    return new Hash32(s);
  }
  
  public boolean getMarkDeletions() {
    return this.markDeletions;
  }
  
}
