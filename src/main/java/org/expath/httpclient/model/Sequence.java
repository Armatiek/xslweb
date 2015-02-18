/****************************************************************************/
/*  File:       Sequence.java                                               */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-03-09                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */

package org.expath.httpclient.model;

import java.io.OutputStream;
import java.util.Properties;
import org.expath.httpclient.HttpClientException;

/**
 * An abstract representation of a sequence (just provide the HTTP Client
 * needs).
 * 
 * @author Florent Georges
 * @date 2011-03-09
 */
public interface Sequence {
  /**
   * Return true if the sequence is empty.
   */
  public boolean isEmpty() throws HttpClientException;

  /**
   * Return the next item in the sequence, as a sequence itself (a singleton).
   * 
   * Each call to this method increment the current position, so the first call
   * returns the first item in the sequence, then the second call returns the
   * second item, etc. Return null if there is no more item to consume in the
   * sequence.
   */
  public Sequence next() throws HttpClientException;

  /**
   * Serialize the sequence to the output stream, using the serialization
   * parameters.
   * 
   * See JAXP's OutputKeys for the serialization parameters, as well as the
   * recommendation "XSLT and XQuery Serialization".
   */
  public void serialize(OutputStream out, Properties params) throws HttpClientException;
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
