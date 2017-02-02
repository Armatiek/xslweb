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

public class DeleteAttribute extends XMLCommand {
  
  private String name;

  public DeleteAttribute(String nodePath, String name) {
    super(nodePath);
    this.name = name;
    this.type = XMLCommand.DELETE_ATTRIBUTE;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String toString() {
    return ("DeleteAttribute: " + this.name + " path " + this.nodePath);
  }

  public ElementNode toXML() {
    try {
      ElementNode ad = new ElementNode("AttributeDeleted");
      ad.setAttribute("pos", this.nodePath);
      ad.setAttribute("name", name);

      return ad;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }
}