/****************************************************************************/
/*  File:       HttpClient.java                                             */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-01                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.saxon;

import java.net.URI;
import java.net.URISyntaxException;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.HttpConnection;
import org.expath.httpclient.HttpRequest;
import org.expath.httpclient.HttpResponse;
import org.expath.httpclient.impl.ApacheHttpConnection;
import org.expath.httpclient.impl.RequestParser;
import org.expath.httpclient.model.Element;
import org.expath.httpclient.model.Sequence;
import org.expath.httpclient.saxon.SaxonElement;
import org.expath.httpclient.saxon.SaxonResult;
import org.expath.httpclient.saxon.SaxonSequence;


/**
 * EXPath module http-client, for Saxon.
 *
 * @author Florent Georges
 * @date   2009-02-01
 */
public class HttpClient
{
    /**
     * Implement the EXPath function http:send-request().
     *
     * <pre>
     * http:send-request($request as element(http:request)?) as item()+
     * </pre>
     */
    public static SequenceIterator sendRequest(XPathContext ctxt, NodeInfo request)
            throws XPathException
    {
        return sendRequest(ctxt, request, null, null);
    }

    /**
     * Implement the EXPath function http:send-request().
     *
     * <pre>
     * http:send-request($request as element(http:request)?,
     *                   $href as xs:string?) as item()+
     * </pre>
     */
    public static SequenceIterator sendRequest(XPathContext ctxt,
                                               NodeInfo request,
                                               String href)
            throws XPathException
    {
        return sendRequest(ctxt, request, href, null);
    }

    /**
     * Implement the EXPath function http:send-request().
     *
     * <pre>
     * http:send-request($request as element(http:request)?,
     *                   $href as xs:string?,
     *                   $bodies as item()*) as item()+
     * </pre>
     */
    public static SequenceIterator sendRequest(XPathContext ctxt,
                                               NodeInfo request,
                                               String href,
                                               SequenceIterator bodies)
            throws XPathException
    {
        HttpClient client = new HttpClient();
        try {
            return client.doSendRequest(ctxt, request, href, bodies);
        }
        catch ( HttpClientException ex ) {
            throw new XPathException("Error sending the HTTP request", ex);
        }
    }

    // TODO: Within the latest draft, $content has been changed to $bodies...
    // This is now an item()* instead of an item().
    //
    // TODO: Theoretically, the SequenceIterator should allow the
    // implementation to be streamable (for instance to not parse the
    // response content if the user does: http:send-request(...)[1],
    // that is, if he/she does not actually access the content).  See
    // if we can use that...
    private SequenceIterator doSendRequest(XPathContext ctxt,
                                           NodeInfo request,
                                           String href,
                                           SequenceIterator bodies)
            throws HttpClientException
                 , XPathException
    {
        Sequence b = new SaxonSequence(bodies, ctxt);
        Element r = new SaxonElement(request, ctxt);
        RequestParser parser = new RequestParser();
        HttpRequest req = parser.parse(r, b, href);
        // override anyway it href exists
        if ( href != null && ! "".equals(href) ) {
            req.setHref(href);
        }
        try {
            URI uri = new URI(req.getHref());
            SaxonResult result = sendOnce(uri, req, parser, ctxt);
            return result.newIterator();
        }
        catch ( URISyntaxException ex ) {
            throw new HttpClientException("Href is not valid: " + req.getHref(), ex);
        }
    }

    /**
     * Send one request, not following redirect but handling authentication.
     * 
     * Authentication may require to reply to an authentication challenge,
     * by sending again the request, with credentials.
     */
    private SaxonResult sendOnce(URI uri, HttpRequest request, RequestParser parser, XPathContext ctxt)
            throws HttpClientException
    {
        SaxonResult result = new SaxonResult(ctxt);
        HttpConnection conn = new ApacheHttpConnection(uri);
        try {
            if ( parser.getSendAuth() ) {
                request.send(result, conn, parser.getCredentials());
            }
            else {
                HttpResponse response = request.send(result, conn, null);
                if ( response.getStatus() == 401 ) {
                    conn.disconnect();
                    conn = new ApacheHttpConnection(uri);
                    // create a new result, and throw the old one away
                    result = new SaxonResult(ctxt);
                    request.send(result, conn, parser.getCredentials());
                }
            }
        }
        finally {
            conn.disconnect();
        }
        return result;
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
