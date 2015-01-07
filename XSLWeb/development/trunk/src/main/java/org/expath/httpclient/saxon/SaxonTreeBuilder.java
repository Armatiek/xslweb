/****************************************************************************/
/*  File:       TreeBuilderHelper.java                                      */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-02                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient.saxon;

import net.sf.saxon.event.Builder;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Untyped;

import org.apache.http.Header;
import org.expath.httpclient.HeaderSet;
import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.HttpConstants;
import org.expath.httpclient.model.TreeBuilder;


/**
 * Implementation of {@link TreeBuilder} for Saxon.
 *
 * @author Florent Georges
 * @date   2009-02-02
 */
public class SaxonTreeBuilder
        implements TreeBuilder
{
    public SaxonTreeBuilder(XPathContext ctxt)
            throws HttpClientException
    {
        myBuilder = ctxt.getController().makeBuilder();
        myBuilder.open();
    }

    /**
     * Provide the result in Saxon's object model.
     */
    public NodeInfo getCurrentRoot()
            throws HttpClientException
    {
        try {
            myBuilder.close();
        }
        catch ( XPathException ex ) {
            throw new HttpClientException("Error closing the Saxon tree builder", ex);
        }
        return myBuilder.getCurrentRoot();
    }

    @Override
    public void outputHeaders(HeaderSet headers)
            throws HttpClientException
    {
        for ( Header h : headers ) {
            assert h.getName() != null : "Header name cannot be null";
            startElem("header");
            attribute("name", h.getName().toLowerCase());
            attribute("value", h.getValue());
            startContent();
            endElem();
        }
    }

    @Override
    public void startElem(String localname)
            throws HttpClientException
    {
        final String prefix = HttpConstants.HTTP_CLIENT_NS_PREFIX;
        final String uri    = HttpConstants.HTTP_CLIENT_NS_URI;
        NodeName name = new FingerprintedQName(prefix, uri, localname);
        try {
            myBuilder.startElement(name, Untyped.getInstance(), 0, 0);
        }
        catch ( XPathException ex ) {
            throw new HttpClientException("Error starting element on the Saxon tree builder", ex);
        }
    }

    @Override
    public void attribute(String localname, CharSequence value)
            throws HttpClientException
    {
        if ( value != null ) {
            NodeName name = new NoNamespaceName(localname);
            try {
                myBuilder.attribute(name, BuiltInAtomicType.UNTYPED_ATOMIC, value, 0, 0);
            }
            catch ( XPathException ex ) {
                throw new HttpClientException("Error creating attribute on the Saxon tree builder", ex);
            }
        }
    }

    @Override
    public void startContent()
            throws HttpClientException
    {
        try {
            myBuilder.startContent();
        }
        catch ( XPathException ex ) {
            throw new HttpClientException("Error starting content on the Saxon tree builder", ex);
        }
    }

    @Override
    public void endElem()
            throws HttpClientException
    {
        try {
            myBuilder.endElement();
        }
        catch ( XPathException ex ) {
            throw new HttpClientException("Error ending element on the Saxon tree builder", ex);
        }
    }

    private Builder myBuilder;
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
