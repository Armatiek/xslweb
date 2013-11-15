/****************************************************************************/
/*  File:       RequestParser.java                                          */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-03-10                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient.impl;

import org.expath.httpclient.HeaderSet;
import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.HttpConstants;
import org.expath.httpclient.HttpCredentials;
import org.expath.httpclient.HttpRequest;
import org.expath.httpclient.HttpRequestBody;
import org.expath.httpclient.model.Attribute;
import org.expath.httpclient.model.Element;
import org.expath.httpclient.model.Sequence;

/**
 * Parse the http:request element into a {@link HttpRequest} object.
 *
 * @author Florent Georges
 * @date   2011-03-10
 */
public class RequestParser
{
    public HttpCredentials getCredentials()
    {
        return myCredentials;
    }

    public boolean  getSendAuth()
    {
        return mySendAuth;
    }

    public HttpRequest parse(Element request, Sequence bodies, String href)
            throws HttpClientException
    {
        if ( ! "request".equals(request.getLocalName())
                  || ! HttpConstants.HTTP_CLIENT_NS_URI.equals(request.getNamespaceUri()) ) {
            throw new HttpClientException("$request is not an element(http:request)");
        }

        String username = null;
        String password = null;
        String auth_method = null;

        HttpRequest req = new HttpRequestImpl();
        req.setHref(href);

        // walk the attributes:
        //     method = NCName
        //     href? = anyURI
        //     status-only? = boolean
        //     username? = string
        //     password? = string
        //     auth-method? = string
        //     send-authorization? = boolean
        //     override-media-type? = string
        //     follow-redirect? = boolean
        for ( Attribute a : request.attributes() ) {
            String local = a.getLocalName();
            if ( !"".equals(a.getNamespaceUri()) ) {
                // ignore namespace qualified attributes
            }
            else if ( "method".equals(local) ) {
                req.setMethod(a.getValue());
            }
            else if ( "href".equals(local) ) {
                req.setHref(a.getValue());
            }
            else if ( "http".equals(local) ) {
                req.setHttpVersion(a.getValue().trim());
            }
            else if ( "status-only".equals(local) ) {
                req.setStatusOnly(a.getBoolean());
            }
            else if ( "username".equals(local) ) {
                username = a.getValue();
            }
            else if ( "password".equals(local) ) {
                password = a.getValue();
            }
            else if ( "auth-method".equals(local) ) {
                auth_method = a.getValue();
            }
            else if ( "send-authorization".equals(local) ) {
                mySendAuth = a.getBoolean();
            }
            else if ( "override-media-type".equals(local) ) {
                req.setOverrideType(a.getValue());
            }
            else if ( "follow-redirect".equals(local) ) {
                req.setFollowRedirect(a.getBoolean());
            }
            else if ( "timeout".equals(local) ) {
                req.setTimeout(a.getInteger());
            }
            else {
                throw new HttpClientException("Unknown attribute http:request/@" + local);
            }
        }
        if ( req.getMethod() == null ) {
            throw new HttpClientException("required @method has not been set on http:request");
        }
        if ( req.getHref() == null ) {
            throw new HttpClientException("required @href has not been set on http:request");
        }
        if ( username != null || password != null || auth_method != null ) {
            setAuthentication(username, password, auth_method);
        }

        // walk the elements
        // TODO: Check element structure validity (header*, (multipart|body)?)
        HeaderSet headers = new HeaderSet();
        req.setHeaders(headers);
        for ( Element child : request.children() ) {
            String local = child.getLocalName();
            String ns = child.getNamespaceUri();
            if ( "".equals(ns) ) {
                // elements in no namespace are an error
                throw new HttpClientException("Element in no namespace: " + local);
            }
            else if ( ! HttpConstants.HTTP_CLIENT_NS_URI.equals(ns) ) {
                // ignore elements in other namespaces
            }
            else if ( "header".equals(local) ) {
                addHeader(headers, child);
            }
            else if ( "body".equals(local) || "multipart".equals(local) ) {
                HttpRequestBody b = BodyFactory.makeRequestBody(child, bodies);
                req.setBody(b);
            }
            else {
                throw new HttpClientException("Unknown element: " + local);
            }
        }

        return req;
    }

    private void setAuthentication(String user, String pwd, String method)
            throws HttpClientException
    {
        if ( user == null || pwd == null || method == null ) {
            throw new HttpClientException("@username, @password and @auth-method must be all set");
        }
        if ( "basic".equals(method) ) {
            myCredentials = new HttpCredentials(user, pwd, method);
        }
        else if ( "digest".equals(method) ) {
            // FIXME: Wrong if HREF is not on http:request, but as a param, because
            // it will be set on myRequest after this method has been called.
            myCredentials = new HttpCredentials(user, pwd, method);
        }
        else {
            throw new HttpClientException("Unknown authentication method: " + method);
        }
    }

    private void addHeader(HeaderSet headers, Element e)
            throws HttpClientException
    {
        String name = null;
        String value = null;
        for ( Attribute a : e.attributes() ) {
            String local = a.getLocalName();
            if ( !"".equals(a.getNamespaceUri()) ) {
                // ignore namespace qualified attributes
            }
            else if ( "name".equals(local) ) {
                name = a.getValue();
            }
            else if ( "value".equals(local) ) {
                value = a.getValue();
            }
            else {
                throw new HttpClientException("Unknown attribute http:header/@" + local);
            }
        }
        // both are required
        if ( name == null || value == null ) {
            throw new HttpClientException("@name and @value are required on http:header");
        }
        // actually add the header
        headers.add(name, value);
    }

    private HttpCredentials myCredentials = null;
    private boolean mySendAuth = false;
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
