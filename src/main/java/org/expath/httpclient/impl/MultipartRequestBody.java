/****************************************************************************/
/*  File:       MultipartRequestBody.java                                   */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-04                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.Header;
import org.expath.httpclient.HeaderSet;
import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.HttpRequestBody;
import org.expath.httpclient.model.Element;
import org.expath.httpclient.model.Sequence;

/**
 * TODO<doc>: ...
 *
 * @author Florent Georges
 * @date   2009-02-04
 */
public class MultipartRequestBody
        extends HttpRequestBody
{
    public MultipartRequestBody(Element elem, Sequence bodies)
            throws HttpClientException
    {
        super(elem);
        // set up boundary
        myBoundary = elem.getAttribute("boundary");
        if ( myBoundary == null ) {
            throw new HttpClientException("@boundary is not on the multipart element");
        }
        myBoundaryBytes = myBoundary.getBytes();
        // check for not allowed attributes
        String[] attr_names = { "media-type", "boundary" };
        elem.noOtherNCNameAttribute(attr_names);
        // handle http:header & http:body childs
        myBodies = new ArrayList<Body>();
        accumulateBodies(elem, bodies);
        if ( myBodies.isEmpty() ) {
            throw new HttpClientException("http:multipart does not contain any http:body");
        }
    }

    @Override
    public void setHeaders(HeaderSet headers)
            throws HttpClientException
    {
        // set the Content-Type header (if not set by the user)
        if ( headers.getFirstHeader("Content-Type") == null ) {
            StringBuilder type = new StringBuilder(getContentType());
            type.append("; boundary=");
            type.append("\"");
            if ( myBoundary.contains("\"") ) {
                type.append(myBoundary.replace("\"", "\\\""));
            }
            else {
                type.append(myBoundary);
            }
            type.append("\"");
            headers.add("Content-Type", type.toString());
        }
    }

    // TODO: If getContent() != null, one part has to use it!
    @Override
    public void serialize(OutputStream out)
            throws HttpClientException
    {
        try {
            for ( Body body : myBodies ) {
                // the boundary
                out.write(DASHES);
                out.write(myBoundaryBytes);
                out.write(NEWLINE);
                // the headers if any
                body.myBody.setHeaders(body.myHeaders);
                serializePartHeaders(out, body.myHeaders);
                // an empty line between headers and body
                out.write(NEWLINE);
                // the body, followed by a newline
                body.myBody.serialize(out);
                out.write(NEWLINE);
            }
            // the last boundary (with extra dashes at the end)
            out.write(DASHES);
            out.write(myBoundaryBytes);
            out.write(DASHES);
            out.write(NEWLINE);
        }
        catch ( IOException ex ) {
            throw new HttpClientException("IO error serializing multipart content", ex);
        }
    }

    @Override
    public boolean isMultipart()
    {
        return true;
    }

    private void accumulateBodies(Element elem, Sequence bodies)
            throws HttpClientException
    {
        // check if there is any child element in no namespace
        if ( elem.hasNoNsChild() ) {
            String msg = "There is an element in no namespace as http:multipart child.";
            throw new HttpClientException(msg);
        }
        // iterate over child elements in http: namespace (ignore other qualified elements)
        HeaderSet headers = new HeaderSet();
        for ( Element b : elem.httpNsChildren() ) {
            if ( "header".equals(b.getLocalName()) ) {
                String[] attr_names = { "name", "value" };
                b.noOtherNCNameAttribute(attr_names);
                String name  = b.getAttribute("name");
                String value = b.getAttribute("value");
                headers.add(name, value);
            }
            else if ( "body".equals(b.getLocalName()) ) {
                // FIXME: Check when/where we must use BODIES here...
//                // TODO: Check if empty element happens once and only once.
//                Item s = b.iterateAxis(Axis.CHILD).moveNext() ? null : serial;
//                HttpRequestBody req_body = BodyFactory.makeRequestBody(b, s);
                HttpRequestBody req_body = BodyFactory.makeRequestBody(b, bodies);
                myBodies.add(new Body(headers, req_body));
                headers = new HeaderSet();
            }
            else {
                String name = b.getDisplayName();
                throw new HttpClientException("Unknown http:multipart child: " + name);
            }
        }
    }

    private void serializePartHeaders(OutputStream out, HeaderSet headers)
            throws IOException
    {
        for ( Header h : headers ) {
            out.write(h.getName().getBytes("US-ASCII"));
            out.write(COLON);
            out.write(h.getValue().getBytes("US-ASCII"));
            out.write(NEWLINE);
        }
    }

    private String myBoundary;
    private byte[] myBoundaryBytes;
    private List<Body> myBodies;
    private static final byte[] DASHES  = { 45, 45 }; // = "--"
    private static final byte[] NEWLINE = { 13, 10 }; // = "\r\n"
    private static final byte[] COLON   = { 58, 32 }; // = ": "

    private static class Body {
        public Body(HeaderSet headers, HttpRequestBody body) {
            myBody = body;
            myHeaders = headers;
        }
        public HeaderSet myHeaders;
        public HttpRequestBody myBody;
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
