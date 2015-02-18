/****************************************************************************/
/*  File:       Attribute.java                                              */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-03-10                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */

package org.expath.httpclient.model;

import org.expath.httpclient.HttpClientException;

/**
 * An abstract representation of an attribute.
 * 
 * @author Florent Georges
 * @date 2011-03-10
 */
public interface Attribute {
  /**
   * Return the local part of the name of the attribute.
   */
  public String getLocalName();

  /**
   * Return the namespace URI part of the name of the attribute.
   * 
   * Return the empty string if the name is in no namespace (never return
   * {@code null}).
   */
  public String getNamespaceUri();

  /**
   * Return the string value of the attribute.
   */
  public String getValue();

  /**
   * Return the boolean value of the attribute.
   */
  public boolean getBoolean() throws HttpClientException;

  /**
   * Return the integer value of the attribute.
   */
  public int getInteger() throws HttpClientException;
}

/* ------------------------------------------------------------------------ */
/*  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS COMMENT.               */
/*                                                                          */
/*  The contents of this file are subject to the Mozilla Public License     */
/*  Version 1.0 (the "License"); you may not use this file except in        */
/*  compliance with the License. You may obtain a copy of the License at    */
/*  http://www.mozilla.org/MPL/.                                            */
/*                                                                          */
/*  Software distributed under the License is distributed on an "AS IS"     */
/*  basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See    */
/*  the License for the specific language governing rights and limitations  */
/*  under the License.                                                      */
/*                                                                          */
/*  The Original Code is: all this file.                                    */
/*                                                                          */
/*  The Initial Developer of the Original Code is Florent Georges.          */
/*                                                                          */
/*  Contributor(s): none.                                                   */
/* ------------------------------------------------------------------------ */
