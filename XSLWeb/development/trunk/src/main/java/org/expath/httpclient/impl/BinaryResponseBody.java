/****************************************************************************/
/*  File:       BinaryResponseBody.java                                     */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-03                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */

package org.expath.httpclient.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.expath.httpclient.ContentType;
import org.expath.httpclient.HeaderSet;
import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.HttpResponseBody;
import org.expath.httpclient.model.Result;
import org.expath.httpclient.model.TreeBuilder;

/**
 * TODO<doc>: ...
 * 
 * @author Florent Georges
 * @date 2009-02-03
 */
public class BinaryResponseBody implements HttpResponseBody {
  public BinaryResponseBody(Result result, byte[] value, ContentType type, HeaderSet headers) throws HttpClientException {
    myContentType = type;
    myHeaders = headers;
    result.add(value);
  }

  // TODO: Work only for binary response.  What if the response is encoded
  //   with base64?
  // -> see my recent email on this subject on xproc-comments ("p:http-request
  //    content-type and encoding" on 2009-02-10,) I think base64 should either
  //    not be supported at all, or be implemented as a wrapper InputStream
  //    that is set earlier on the InputStream to decode base64 (for binary,
  //    text or whatever.)
  public BinaryResponseBody(Result result, InputStream in, ContentType type, HeaderSet headers) throws HttpClientException {
    myContentType = type;
    myHeaders = headers;
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buf = new byte[4096];
      int read = 0;
      while ((read = in.read(buf)) > 0) {
        out.write(buf, 0, read);
      }
      byte[] bytes = out.toByteArray();
      result.add(bytes);
    } catch (IOException ex) {
      throw new HttpClientException("error reading HTTP response", ex);
    }
  }

  @Override
  public void outputBody(TreeBuilder b) throws HttpClientException {
    if (myHeaders != null) {
      b.outputHeaders(myHeaders);
    }
    b.startElem("body");
    b.attribute("media-type", myContentType.getValue());
    // TODO: Support other attributes as well?
    b.startContent();
    b.endElem();
  }

  private ContentType myContentType;
  private HeaderSet myHeaders;
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