/****************************************************************************/
/*  File:       HttpRequestImpl.java                                        */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-02                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.expath.httpclient.ContentType;
import org.expath.httpclient.HeaderSet;
import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.HttpRequestBody;
import org.expath.httpclient.HttpConnection;
import org.expath.httpclient.HttpConstants;
import org.expath.httpclient.HttpCredentials;
import org.expath.httpclient.HttpRequest;
import org.expath.httpclient.HttpResponse;
import org.expath.httpclient.HttpResponseBody;
import org.expath.httpclient.model.Result;

/**
 * TODO<doc>: ...
 *
 * @author Florent Georges
 * @date   2009-02-02
 */
public class HttpRequestImpl
        implements HttpRequest
{
    public HttpResponse send(Result result, HttpConnection conn, HttpCredentials cred)
            throws HttpClientException
    {
        if ( myHeaders == null ) {
            myHeaders = new HeaderSet();
        }
        conn.setRequestMethod(myMethod, myBody != null);
        conn.setRequestHeaders(myHeaders);
        if ( myHttpVer != null ) {
            conn.setHttpVersion(myHttpVer);
        }
        if ( myTimeout != null ) {
            conn.setTimeout(myTimeout);
        }
        conn.setFollowRedirect(myFollowRedirect);
        conn.connect(myBody, cred);
        int status = conn.getResponseStatus();
        String msg = conn.getResponseMessage();
        HttpResponseBody body = null;
        if ( ! myStatusOnly ) {
            ContentType type = getContentType(conn.getResponseHeaders());
            if ( type == null ) {
                // FIXME: We should probably rather fall back to octet-stream...
                LOG.debug("There is no Content-Type, we assume there is no content");
            }
            else {
                body = BodyFactory.makeResponseBody(result, type, conn);
            }
        }
        HttpResponse resp = new HttpResponse(status, msg, conn.getResponseHeaders(), body);
        result.add(resp);
        return resp;
    }

    private ContentType getContentType(HeaderSet headers)
            throws HttpClientException
    {
        if ( myOverrideType == null ) {
            Header h = headers.getFirstHeader("Content-Type");
            if ( h == null ) {
                return null;
            }
            else {
                return new ContentType(h);
            }
        }
        else {
            return new ContentType(myOverrideType, null);
        }
    }

    @Override
    public String getMethod()
    {
        return myMethod;
    }

    @Override
    public void setMethod(String method)
    {
        myMethod = method;
    }

    @Override
    public String getHref()
    {
        return myHref;
    }

    @Override
    public void setHref(String href)
    {
        myHref = href;
    }

    @Override
    public String getHttpVersion()
    {
        return myHttpVer;
    }

    @Override
    public void setHttpVersion(String ver)
            throws HttpClientException
    {
        if ( HttpConstants.HTTP_1_0.equals(ver) ) {
            myHttpVer = HttpConstants.HTTP_1_0;
        }
        else if ( HttpConstants.HTTP_1_1.equals(ver) ) {
            myHttpVer = HttpConstants.HTTP_1_1;
        }
        else {
            throw new HttpClientException("Unknown HTTP version: '" + ver + "'");
        }
    }

    @Override
    public void setOverrideType(String type)
    {
        myOverrideType = type;
    }

    @Override
    public void setHeaders(HeaderSet headers)
    {
        myHeaders = headers;
    }

    @Override
    public void setBody(HttpRequestBody body)
            throws HttpClientException
    {
        myBody = body;
        body.setHeaders(myHeaders);
    }

    @Override
    public void setStatusOnly(boolean only)
    {
        myStatusOnly = only;
    }

    @Override
    public void setFollowRedirect(boolean follow)
    {
        myFollowRedirect = follow;
    }

    @Override
    public void setTimeout(Integer seconds)
    {
        myTimeout = seconds;
    }

    private String myMethod;
    private String myHref;
    private String myHttpVer;
    private String myOverrideType;
    private boolean myStatusOnly;
    private boolean myFollowRedirect = true;
    private Integer myTimeout = null;
    private HeaderSet myHeaders;
    private HttpRequestBody myBody;
    private static final Log LOG = LogFactory.getLog(HttpRequestImpl.class);
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
