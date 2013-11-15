/****************************************************************************/
/*  File:       ApacheHttpConnection.java                                   */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-02                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProxySelector;
import java.net.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.expath.httpclient.HeaderSet;
import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.HttpConnection;
import org.expath.httpclient.HttpConstants;
import org.expath.httpclient.HttpCredentials;
import org.expath.httpclient.HttpRequestBody;

/**
 * TODO<doc>: ...
 *
 * @author Florent Georges
 * @date   2009-02-02
 */
public class ApacheHttpConnection
        implements HttpConnection
{
    public ApacheHttpConnection(URI uri)
    {
        myUri = uri;
        myRequest = null;
        myResponse = null;
        myVersion = DEFAULT_HTTP_VERSION;
        myClient = null;
    }

    public void connect(HttpRequestBody body, HttpCredentials cred)
            throws HttpClientException
    {
        try {
            // make a new client
            myClient = makeClient();
            // set the credentials (if any)
            setCredentials(cred);
            // set the request entity body (if any)
            setRequestEntity(body);
            // log the request headers?
            if ( LOG.isDebugEnabled() ) {
                LOG.debug("METHOD: " + myRequest.getMethod());
                Header[] headers = myRequest.getAllHeaders();
                LoggerHelper.logHeaders(LOG, "REQ HEADERS", headers);
                LoggerHelper.logCookies(LOG, "COOKIES", COOKIES.getCookies());
            }
            // send the request
            myResponse = myClient.execute(myRequest);

            // TODO: Handle 'Connection' headers (for instance "Connection: close")
            // See for instance http://www.jmarshall.com/easy/http/.
            // ...

            // log the response headers?
            if ( LOG.isDebugEnabled() ) {
                Header[] headers = myResponse.getAllHeaders();
                LoggerHelper.logHeaders(LOG, "RESP HEADERS", headers);
                LoggerHelper.logCookies(LOG, "COOKIES", COOKIES.getCookies());
            }
        }
        catch ( IOException ex ) {
            throw new HttpClientException("Error executing the HTTP method: " + ex.getMessage(), ex);
        }
    }

    public void disconnect()
    {
        if ( myClient != null ) {
            myClient.getConnectionManager().shutdown();
        }
    }

    public void setHttpVersion(String ver)
            throws HttpClientException
    {
        if ( myClient != null ) {
            String msg = "Internal error, HTTP version cannot been "
                    + "set after connect() has been called.";
            throw new HttpClientException(msg);
        }
        if ( HttpConstants.HTTP_1_0.equals(ver) ) {
            myVersion = HttpVersion.HTTP_1_0;
        }
        else if ( HttpConstants.HTTP_1_1.equals(ver) ) {
            myVersion = HttpVersion.HTTP_1_1;
        }
        else {
            throw new HttpClientException("Internal error, unknown HTTP version: '" + ver + "'");
        }
    }

    public void setRequestHeaders(HeaderSet headers)
            throws HttpClientException
    {
        if ( myRequest == null ) {
            throw new HttpClientException("setRequestMethod has not been called before");
        }
        myRequest.setHeaders(headers.toArray());
    }

    public void setRequestMethod(String method, boolean with_content)
            throws HttpClientException
    {
        if ( LOG.isInfoEnabled() ) {
            LOG.debug("Request method: " + method + " (" + with_content + ")");
        }
        String uri = myUri.toString();
        String m = method.toUpperCase();
        if ( "DELETE".equals(m) ) {
            myRequest = new HttpDelete(uri);
        }
        else if ( "GET".equals(m) ) {
            myRequest = new HttpGet(uri);
        }
        else if ( "HEAD".equals(m) ) {
            myRequest = new HttpHead(uri);
        }
        else if ( "OPTIONS".equals(m) ) {
            myRequest = new HttpOptions(uri);
        }
        else if ( "POST".equals(m) ) {
            myRequest = new HttpPost(uri);
        }
        else if ( "PUT".equals(m) ) {
            myRequest = new HttpPut(uri);
        }
        else if ( "TRACE".equals(m) ) {
            myRequest = new HttpTrace(uri);
        }
        else if ( ! checkMethodName(method) ) {
            throw new HttpClientException("Invalid HTTP method name [" + method + "]");
        }
        else if ( with_content ) {
            myRequest = new AnyEntityMethod(m, uri);
        }
        else {
            myRequest = new AnyEmptyMethod(m, uri);
        }
    }

    public void setFollowRedirect(boolean follow)
    {
        myFollowRedirect = follow;
    }

    public void setTimeout(int seconds)
    {
        myTimeout = seconds;
    }

    /**
     * Check the method name does match the HTTP/1.1 production rules.
     *
     *     Method         = "OPTIONS"                ; Section 9.2
     *                    | "GET"                    ; Section 9.3
     *                    | "HEAD"                   ; Section 9.4
     *                    | "POST"                   ; Section 9.5
     *                    | "PUT"                    ; Section 9.6
     *                    | "DELETE"                 ; Section 9.7
     *                    | "TRACE"                  ; Section 9.8
     *                    | "CONNECT"                ; Section 9.9
     *                    | extension-method
     *
     *     extension-method = token
     *
     *     token          = 1*&lt;any CHAR except CTLs or separators>
     *
     *     CHAR           = &lt;any US-ASCII character (octets 0 - 127)>
     *
     *     CTL            = &lt;any US-ASCII control character
     *                      (octets 0 - 31) and DEL (127)>
     *
     *     separators     = "(" | ")" | "&lt;" | ">" | "@"
     *                    | "," | ";" | ":" | "\" | <">
     *                    | "/" | "[" | "]" | "?" | "="
     *                    | "{" | "}" | SP | HT
     */
    private boolean checkMethodName(String method)
    {
        for ( char c : method.toCharArray() ) {
            if ( c > 127 || ! METHOD_CHARS[c] ) {
                return false;
            }
        }
        return true;
    }

    private static final boolean[] METHOD_CHARS = new boolean[128];
    static {
        // SP = 32, HT = 9, so any char between 33 and 126 incl., minus
        // explicitly excluded chars...
        String excl = "()<>@,;:\\\"/[]?={}";
        for ( char c = 0; c < 128; ++ c ) {
            if ( c < 33 || c == 127 ) {
                METHOD_CHARS[c] = false;
            }
            else if ( excl.indexOf(c) == -1 ) {
                METHOD_CHARS[c] = true;
            }
            else {
                METHOD_CHARS[c] = false;
            }
        }
    }

    public int getResponseStatus()
    {
        return myResponse.getStatusLine().getStatusCode();
    }

    public String getResponseMessage()
    {
        return myResponse.getStatusLine().getReasonPhrase();
    }

    public HeaderSet getResponseHeaders()
            throws HttpClientException
    {
        return new HeaderSet(myResponse.getAllHeaders());
    }

    /**
     * TODO: How to use Apache HTTP Client facilities for response content
     * handling, instead of parsing this stream myself?
     */
    public InputStream getResponseStream()
            throws HttpClientException
    {
        try {
            HttpEntity entity = myResponse.getEntity();
            return entity == null ? null : entity.getContent();
        }
        catch ( IOException ex ) {
            throw new HttpClientException("Error getting the HTTP response stream", ex);
        }
    }

    /**
     * Make a new Apache HTTP client, in order to serve this request.
     */
    private AbstractHttpClient makeClient()
    {
        AbstractHttpClient client = new DefaultHttpClient();
        HttpParams params = client.getParams();
        // use the default JVM proxy settings (http.proxyHost, etc.)
        HttpRoutePlanner route = new ProxySelectorRoutePlanner(
                client.getConnectionManager().getSchemeRegistry(),
                ProxySelector.getDefault());
        client.setRoutePlanner(route);
        // do follow redirections?
        params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, myFollowRedirect);
        // set the timeout if any
        if ( myTimeout != null ) {
            // See http://blog.jayway.com/2009/03/17/configuring-timeout-with-apache-httpclient-40/
            HttpConnectionParams.setConnectionTimeout(params, myTimeout * 1000);
            HttpConnectionParams.setSoTimeout(params, myTimeout * 1000);
        }
        // the shared cookie store
        client.setCookieStore(COOKIES);
        // the HTTP version (1.0 or 1.1)
        params.setParameter("http.protocol.version", myVersion);
        // return the just built client
        return client;
    }

    /**
     * Set the credentials on the client, based on the {@link HttpCredentials} object.
     */
    private void setCredentials(HttpCredentials cred)
            throws HttpClientException
    {
        if ( cred == null ) {
            return;
        }
        URI uri = myRequest.getURI();
        int port = uri.getPort();
        if ( port == -1 ) {
            String scheme = uri.getScheme();
            if ( "http".equals(scheme) ) {
                port = 80;
            }
            else if ( "https".equals(scheme) ) {
                port = 443;
            }
            else {
                throw new HttpClientException("Unknown scheme: " + uri);
            }
        }
        String host = uri.getHost();
        String user = cred.getUser();
        String pwd = cred.getPwd();
        if ( LOG.isDebugEnabled() ) {
            LOG.debug("Set credentials for " + host + ":" + port
                    + " - " + user + " - ***");
        }
        Credentials c = new UsernamePasswordCredentials(user, pwd);
        AuthScope scope = new AuthScope(host, port);
        myClient.getCredentialsProvider().setCredentials(scope, c);
    }

    /**
     * Configure the request to get its entity body from the {@link HttpRequestBody}.
     */
    private void setRequestEntity(HttpRequestBody body)
            throws HttpClientException
    {
        if ( body == null ) {
            return;
        }
        // make the entity from a new producer
        HttpEntity entity;
        if ( myVersion == HttpVersion.HTTP_1_1 ) {
            // Take advantage of HTTP 1.1 chunked encoding to stream the
            // payload directly to the request.
            ContentProducer producer = new RequestBodyProducer(body);
            EntityTemplate template = new EntityTemplate(producer);
            template.setContentType(body.getContentType());
            entity = template;
        }
        else {
            // With HTTP 1.0, chunked encoding is not supported, so first
            // serialize into memory and use the resulting byte array as the
            // entity payload.
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            body.serialize(buffer);
            entity = new ByteArrayEntity(buffer.toByteArray());
        }
        // cast the request
        HttpEntityEnclosingRequestBase req = null;
        if ( ! (myRequest instanceof HttpEntityEnclosingRequestBase) ) {
            String msg = "Body not allowed on a " + myRequest.getMethod() + " request";
            throw new HttpClientException(msg);
        }
        else {
            req = (HttpEntityEnclosingRequestBase) myRequest;
        }
        // set the entity on the request
        req.setEntity(entity);
    }

    /** The target URI. */
    private URI myUri;
    /** The Apache request. */
    private HttpUriRequest myRequest;
    /** The Apache response. */
    private HttpResponse myResponse;
    /** The HTTP protocol version. */
    private HttpVersion myVersion;
    /** The Apache client. */
    private AbstractHttpClient myClient;
    /** Follow HTTP redirect? */
    private boolean myFollowRedirect = true;
    /** The timeout to use, in seconds, or null for default. */
    private Integer myTimeout = null;
    /**
     * The shared cookie store.
     *
     * TODO: Make it possible to serialize the cookies to disk?
     */
    private static final CookieStore COOKIES = new BasicCookieStore();
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(ApacheHttpConnection.class);

    /**
     * The HTTP version (1.0 or 1.1) to use by default.
     * 
     * Configurable by the system property {@code org.expath.hc.http.version}.
     * By default, use HTTP 1.1.  Can be set on a per-request basis, by setting
     * the {@code http:request/@http} attribute.
     */
    private static HttpVersion DEFAULT_HTTP_VERSION = HttpVersion.HTTP_1_1;
    static {
        String ver = System.getProperty("org.expath.hc.http.version");
        if ( ver != null ) {
            ver = ver.trim();
            if ( "1.0".equals(ver) ) {
                DEFAULT_HTTP_VERSION = HttpVersion.HTTP_1_0;
            }
            else if ( "1.1".equals(ver) ) {
                DEFAULT_HTTP_VERSION = HttpVersion.HTTP_1_1;
            }
            else {
                String msg = "Wrong HTTP version: " + ver + " (check org.expath.hc.http.version)";
                throw new RuntimeException(msg);
            }
        }
    }

    /**
     * A request entity producer, generating content from an {@link HttpRequestBody}.
     */
    private static class RequestBodyProducer
            implements ContentProducer
    {
        public RequestBodyProducer(HttpRequestBody body)
        {
            myBody = body;
        }

        public void writeTo(OutputStream out)
                throws IOException
        {
            try {
                myBody.serialize(out);
            }
            catch ( HttpClientException ex ) {
                throw new IOException("Error serializing the body content", ex);
            }
        }

        private HttpRequestBody myBody;
    }
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
