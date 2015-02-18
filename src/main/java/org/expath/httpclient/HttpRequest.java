/****************************************************************************/
/*  File:       HttpRequest.java                                            */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-02                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */

package org.expath.httpclient;

import org.apache.http.impl.client.CloseableHttpClient;
import org.expath.httpclient.model.Result;

/**
 * An HTTP request.
 * 
 * @author Florent Georges
 * @date 2009-02-02
 */
public interface HttpRequest {

  public HttpResponse send(Result result, HttpConnection conn, 
      HttpCredentials cred, CloseableHttpClient httpClient) throws HttpClientException;

  public String getMethod();

  public void setMethod(String method);

  public String getHref();

  public void setHref(String href);

  public String getHttpVersion();

  public void setHttpVersion(String ver) throws HttpClientException;

  public void setOverrideType(String type);

  public void setHeaders(HeaderSet headers);

  public void setBody(HttpRequestBody body) throws HttpClientException;

  public void setStatusOnly(boolean only);

  public void setFollowRedirect(boolean follow);

  public void setTimeout(Integer seconds);
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
