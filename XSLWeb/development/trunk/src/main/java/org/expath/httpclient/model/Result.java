/****************************************************************************/
/*  File:       Result.java                                                 */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-03-10                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */

package org.expath.httpclient.model;

import javax.xml.transform.Source;
import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.HttpResponse;

/**
 * An abstract representation of the result sequence.
 * 
 * Accumulate result items from strings, bytes, JAXP sources and HTTP response
 * objects.
 * 
 * A specific implementation is obviously supposed to setup a way to provide the
 * caller with the final result sequence within the processor's own object
 * model.
 * 
 * The items are added in order to the result sequence (in the same order than
 * the method calls). Except for the HTTP response objects, which will be called
 * once per result sequence, and always must be added to the front of the
 * sequence.
 * 
 * @author Florent Georges
 * @date 2011-03-10
 */
public interface Result {
  /**
   * Add an {@code xs:string} to the result sequence.
   */
  public void add(String string) throws HttpClientException;

  /**
   * Add an {@code xs:base64Binary} to the result sequence.
   */
  public void add(byte[] bytes) throws HttpClientException;

  /**
   * Add a document node to the result sequence.
   */
  public void add(Source src) throws HttpClientException;

  /**
   * Add the http:response element to the result sequence.
   * 
   * The implementation for a specific processor is supposed to call the method
   * {@link HttpResponse#makeResultElement(TreeBuilder)} with a tree builder for
   * the same processor. This must be added at the front of the sequence,
   * always, even if it is called after other methods.
   */
  public void add(HttpResponse response) throws HttpClientException;
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
