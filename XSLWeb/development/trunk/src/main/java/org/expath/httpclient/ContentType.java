/****************************************************************************/
/*  File:       ContentType.java                                            */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-22                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;

/**
 * Represent a Content-Type header.
 *
 * Provide the ability to get the boundary param in case of a multipart
 * content type on the one hand, and the ability to get only the MIME type
 * string without any param on the other hand.
 *
 * @author Florent Georges
 * @date   2009-02-22
 */
public class ContentType
{
    public ContentType(String type, String boundary)
    {
        myHeader = null;
        myType = type;
        myBoundary = boundary;
    }

    public ContentType(Header h)
            throws HttpClientException
    {
        if ( h == null ) {
            throw new HttpClientException("Header is null");
        }
        if ( ! "Content-Type".equalsIgnoreCase(h.getName()) ) {
            throw new HttpClientException("Header is not content type");
        }
        myHeader = h;
        myType = HeaderSet.getHeaderWithoutParam(myHeader);
        HeaderElement[] elems = h.getElements();
        if ( elems != null ) {
            for ( HeaderElement e : elems ) {
                for ( NameValuePair p : e.getParameters() ) {
                    if ( "boundary".equals(p.getName()) ) {
                        myBoundary = p.getValue();
                    }
                }
            }
        }
    }

    @Override
    public String toString()
    {
        if ( myHeader == null ) {
            return "Content-Type: " + getValue();
        }
        else {
            return myHeader.toString();
        }
    }

    public String getType()
    {
        return myType;
    }

    public String getBoundary()
    {
        return myBoundary;
    }

    public String getValue()
    {
        // TODO: Why did I add the boundary before...?
//        if ( myHeader == null ) {
//            StringBuilder b = new StringBuilder();
//            b.append(myType);
//            if ( myBoundary != null ) {
//                b.append("; boundary=\"");
//                // TODO: Is that correct escaping sequence?
//                b.append(myBoundary.replace("\"", "\\\""));
//                b.append("\"");
//            }
//            return b.toString();
//        }
        if ( myType != null ) {
            return myType;
        }
        if ( myHeader != null ) {
            return myHeader.getValue();
        }
        else {
            return null;
        }
    }

    private Header myHeader;
    private String myType;
    private String myBoundary;
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
