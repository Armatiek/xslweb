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

public class CDataNode extends TextNode {

  private static final long serialVersionUID = 1L;

  public CDataNode() {
    super();
  }
  
  public int getNodeType() {
    return Node.CDATA_SECTION_NODE;
  }

  public CDataNode(String content) {
    super(content);
  }

  public void exportXML(Writer writer, boolean split) throws IOException {
    writer.write("<![CDATA[");
    writer.write(content.toString());
    writer.write("]]>");
    writer.flush();
  }
}
