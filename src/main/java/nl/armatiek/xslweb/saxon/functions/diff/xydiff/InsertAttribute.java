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

public class InsertAttribute extends XMLCommand {
  
  private String name;
  private String value;

  public InsertAttribute(String nodePath, String name, String value) {
    super(nodePath);
    this.name = name;
    this.value = value;
    this.type = XMLCommand.INSERT_ATTRIBUTE;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String toString() {
    return ("InsertAttribute: name " + this.name + " value " + this.value + " path " + this.nodePath);
  }

  public ElementNode toXML() {
    try {
      ElementNode ai = new ElementNode("AttributeInserted");
      ai.setAttribute("pos", this.nodePath);
      ai.setAttribute("name", this.name);
      ai.setAttribute("value", this.value);

      return ai;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }
}
