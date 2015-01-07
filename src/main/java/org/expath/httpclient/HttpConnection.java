/****************************************************************************/
/*  File:       HttpConnection.java                                         */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-02                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */

package org.expath.httpclient;

import java.io.InputStream;

import org.apache.http.impl.client.CloseableHttpClient;

/**
 * Abstract the services needed on the actual HTTP connection.
 * 
 * I should be able to use either HttpURLConnection or Apache HTTP Client to
 * implement it.
 * 
 * @author Florent Georges
 * @date 2009-02-02
 */
public interface HttpConnection {
  
  public void connect(HttpRequestBody body, HttpCredentials cred, 
      CloseableHttpClient httpClient) throws HttpClientException;

  public void disconnect() throws HttpClientException;

  public void setHttpVersion(String ver) throws HttpClientException;

  // requests...
  public void setRequestHeaders(HeaderSet headers) throws HttpClientException;

  public void setRequestMethod(String method, boolean with_content) throws HttpClientException;

  public void setFollowRedirect(boolean follow);

  public void setTimeout(int seconds);

  // responses...
  public int getResponseStatus() throws HttpClientException;

  public String getResponseMessage() throws HttpClientException;

  public HeaderSet getResponseHeaders() throws HttpClientException;

  public InputStream getResponseStream() throws HttpClientException;
}

/* ------------------------------------------------------------------------ */
/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS COMMENT. */
/*                                                                          */
/* The contents of this file are subject to the Mozilla Public License */
/* Version 1.0 (the "License"); you may not use this file except in */
/* compliance with the License. You may obtain a copy of the License at */
/* http://www.mozilla.org/MPL/. */
/*                                                                          */
/* Software distributed under the License is distributed on an "AS IS" */
/* basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See */
/* the License for the specific language governing rights and limitations */
/* under the License. */
/*                                                                          */
/* The Original Code is: all this file. */
/*                                                                          */
/* The Initial Developer of the Original Code is Florent Georges. */
/*                                                                          */
/* Contributor(s): none. */
/* ------------------------------------------------------------------------ */
