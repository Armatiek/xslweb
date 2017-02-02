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
import java.util.Enumeration;
import java.util.Iterator;

import org.w3c.dom.Node;

import nl.armatiek.xslweb.saxon.functions.diff.xydiff.Hash32;

public class ElementNode extends AbstractTreeNode {
  
  private static final long serialVersionUID = 1L;
  
  private String elementName;

  public ElementNode(String elementName) {
    super(true, true);
    this.elementName = elementName;
  }
  
  public int getNodeType() {
    return Node.ELEMENT_NODE;
  }

  public void setElementName(String name) {
    this.elementName = name;
  }

  public String getElementName() {
    return this.elementName;
  }

  public void exportXML(Writer writer, boolean split) throws IOException {
    // write elements
    writer.write("<");
    writer.write(elementName);

    // write attributes
    for (Enumeration<String> e = attributes.keys(); e.hasMoreElements();) {
      String key = (String) e.nextElement();
      writer.write(" ");
      writer.write(key);

      String attrValue = (String) attributes.get(key);

      if (attrValue.indexOf("\"") == -1) {
        writer.write("=\"");
        writer.write(attrValue);
        writer.write("\"");
      } else {
        writer.write("='");
        writer.write(attrValue);
        writer.write("'");
      }
    }

    if (children.isEmpty()) {
      // close element
      writer.write("/>");
    } else {
      writer.write(">");

      // write children
      for (Iterator<TreeNode> i = children.iterator(); i.hasNext();) {
        TreeNode node = (TreeNode) i.next();
        node.exportXML(writer, split);
      }

      // close elements
      writer.write("</");
      writer.write(elementName);
      writer.write(">");
    }

    writer.flush();
  }

  public boolean equalsContent(Object obj) {
    if (obj instanceof ElementNode) {
      ElementNode elementObj = (ElementNode) obj;

      // check element name
      if (!elementObj.elementName.equals(elementName)) {
        return false;
      }

      // check attributes
      if (elementObj.attributes.size() != attributes.size()) {
        return false;
      }

      for (Enumeration<String> e = attributes.keys(); e.hasMoreElements();) {
        String key = (String) e.nextElement();

        if (!((String) elementObj.attributes.get(key)).equals((String) attributes.get(key))) {
          return false;
        }
      }

      // check children
      return super.equalsContent(obj);
    } else {
      return false;
    }
  }

  public Hash32 getHash32() {
    String s = getElementName();

    return new Hash32(s);
  }
}
