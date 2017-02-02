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

import org.w3c.dom.Node;

import nl.armatiek.xslweb.saxon.functions.diff.xydiff.Hash32;

public class TextNode extends AbstractTreeNode {
  
  private static final long serialVersionUID = 1L;
  
  protected StringBuffer content;

  public TextNode() {
    this("");
  }

  public TextNode(String content) {
    super(false, false);
    this.content = new StringBuffer(content);
  }
  
  public int getNodeType() {
    return Node.TEXT_NODE;
  }

  public void appendContent(String content) {
    this.content.append(content);
  }

  public void setContent(String newContent) {
    this.content = new StringBuffer(newContent);
  }

  public String getContent() {
    return content.toString();
  }

  public void exportXML(Writer writer, boolean split) throws IOException {
    writer.write(content.toString());

    if (split) {
      writer.write("<!--SPLIT-->");
    }

    writer.flush();
  }

  public boolean equalsContent(Object obj) {
    if (obj instanceof TextNode) {
      return ((TextNode) obj).content.toString().equals(content.toString());
    } else {
      return false;
    }
  }

  public double getWeight() {
    if (getContent().length() == 0) {
      // prevent to detect operations on an empty node
      return 0.00001;
    }

    return Math.log(getContent().length() + 1);
  }

  public Hash32 getHash32() {
    return new Hash32(getContent().toString());
  }
}
