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

public class UpdateAttribute extends XMLCommand {
  
  private String attributeName;
  private String oldValue;
  private String newValue;

  public UpdateAttribute(String nodePath, String name, String oldValue, String value) {
    super(nodePath);
    this.attributeName = name;
    this.oldValue = oldValue;
    this.newValue = value;
    this.type = XMLCommand.UPDATE_ATTRIBUTE;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  public String getNewValue() {
    return newValue;
  }

  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }

  public String getOldValue() {
    return oldValue;
  }

  public void setOldValue(String oldValue) {
    this.oldValue = oldValue;
  }

  public String toString() {
    return ("UpdateAttribute: name " + attributeName + " path " + this.nodePath + " from " + oldValue + " to " + newValue);
  }

  public ElementNode toXML() {
    ElementNode ua = new ElementNode("AttributeUpdated");
    ua.setAttribute("pos", nodePath);
    ua.setAttribute("name", attributeName);
    ua.setAttribute("ov", oldValue);
    ua.setAttribute("nv", newValue);

    return ua;
  }
}
