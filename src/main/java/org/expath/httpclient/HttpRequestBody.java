/****************************************************************************/
/*  File:       HttpRequestBody.java                                        */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-02                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient;

import java.io.OutputStream;
import org.expath.httpclient.model.Element;

/**
 * The body of an HTTP request.
 *
 * The body of a multipart request is a composite of several bodies.
 *
 * A request body encapsulates the NodeInfo of http:body (or http:multipart.)
 * This element won't change during the extension execution anyway.  The
 * idea is to be able to serialize it only when we need to, directly to the
 * connection output stream (so avoiding to parse it to an intermediary format.)
 * 
 * @author Florent Georges
 * @date   2009-02-02
 */
public abstract class HttpRequestBody
{
    public abstract void serialize(OutputStream out)
            throws HttpClientException;

    public abstract void setHeaders(HeaderSet headers)
            throws HttpClientException;

    public abstract boolean isMultipart();

    public HttpRequestBody(Element elem)
            throws HttpClientException
    {
        myElem = elem;
        myContentType = myElem.getAttribute("media-type");
        if ( myContentType == null ) {
            throw new HttpClientException("@media-type is not on the body or multipart element");
        }
    }

    public String getContentType()
    {
        return myContentType;
    }

    protected Element getBodyElement()
    {
        return myElem;
    }

    private Element myElem;
    private String myContentType;
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
