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

/*
 * This class implements the hash functions describes by Bob Jenkins (December, 1996). 
 * see http://burtleburtle.net/bob/hash/evahash.html
 *
 */
public class Hash32 {
  
  public int value;

  public Hash32() {
    this.value = 0;
  }

  public Hash32(String str) {
    // at the moment use Java String Hash functions...
    this.value = str.hashCode();
  }

  public Hash32(int initialvalue) {
    this.value = initialvalue;
  }

  public Hash32(Hash32 toCopy) {
    this.value = toCopy.value;
  }

  public Hash32(long[] buf) {
    String str = buf[0] + "!" + buf[1];
    this.value = str.hashCode();
  }

  public Object clone() {
    return new Hash32(this.value);
  }

  public String toHexString() {
    return Integer.toHexString(this.value);
  }

  public boolean equals(Object o) {
    if (o instanceof Hash32) {
      return this.value == ((Hash32) o).value;
    }

    return false;
  }

  public String toString() {
    return toHexString();
  }
  
}
