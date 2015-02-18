/****************************************************************************/
/*  File:       TextResponseBody.java                                       */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-02                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */

package org.expath.httpclient.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
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
 * @date 2009-02-02
 */
public class TextResponseBody implements HttpResponseBody {
  
  public TextResponseBody(Result result, InputStream in, ContentType type, 
      HeaderSet headers) throws HttpClientException {
    // FIXME: ...
    String charset = "utf-8";
    try {
      Reader reader = new InputStreamReader(in, charset);
      init(result, reader, type, headers);
    } catch (UnsupportedEncodingException ex) {
      String msg = "not supported charset reading HTTP response: " + charset;
      throw new HttpClientException(msg, ex);
    }
  }

  public TextResponseBody(Result result, Reader in, ContentType type, HeaderSet headers) throws HttpClientException {
    init(result, in, type, headers);
  }

  private void init(Result result, Reader in, ContentType type, HeaderSet headers) throws HttpClientException {
    myContentType = type;
    myHeaders = headers;
    // BufferedReader handles the ends of line (all \n, \r, and \r\n are
    // transformed to \n)
    try {
      StringBuilder builder = new StringBuilder();
      BufferedReader buf_in = new BufferedReader(in);
      String buf = null;
      while ((buf = buf_in.readLine()) != null) {
        builder.append(buf);
        builder.append('\n');
      }
      String value = builder.toString();
      result.add(value);
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
