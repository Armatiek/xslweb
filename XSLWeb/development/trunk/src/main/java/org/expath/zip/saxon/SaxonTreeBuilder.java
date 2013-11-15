/****************************************************************************/
/*  File:       SaxonTreeBuilder.java                                       */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-02-21                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.zip.saxon;

import net.sf.saxon.event.Builder;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Untyped;
import org.expath.zip.ZipConstants;
import org.expath.zip.ZipException;

/**
 * Implementation for Saxon of the processor-independent TreeBuilder.
 *
 * @author Florent Georges
 * @date   2011-02-21
 */
public class SaxonTreeBuilder
        implements org.expath.zip.TreeBuilder
{
    public SaxonTreeBuilder(XPathContext ctxt)
            throws XPathException
    {
        myBuilder = ctxt.getController().makeBuilder();
        myBuilder.open();
    }

    @Override
    public void startElement(String local_name)
            throws ZipException
    {
        final String prefix = ZipConstants.ZIP_NS_PREFIX;
        final String uri    = ZipConstants.ZIP_NS_URI;
        NodeName name = new FingerprintedQName(prefix, uri, local_name);
        try {
            myBuilder.startElement(name, Untyped.getInstance(), 0, 0);
        }
        catch ( XPathException ex ) {
            throw new ZipException("Error starting element on the Saxon tree builder", ex);
        }
    }

    @Override
    public void endElement()
            throws ZipException
    {
        try {
            myBuilder.endElement();
        }
        catch ( XPathException ex ) {
            throw new ZipException("Error ending element on the Saxon tree builder", ex);
        }
    }

    @Override
    public void startContent()
            throws ZipException
    {
        try {
            myBuilder.startContent();
        }
        catch ( XPathException ex ) {
            throw new ZipException("Error starting content on the Saxon tree builder", ex);
        }
    }

    @Override
    public void attribute(String local_name, String value)
            throws ZipException
    {
        NodeName name = new NoNamespaceName(local_name);
        try {
            myBuilder.attribute(name, BuiltInAtomicType.UNTYPED_ATOMIC, value, 0, 0);
        }
        catch ( XPathException ex ) {
            throw new ZipException("Error starting content on the Saxon tree builder", ex);
        }
    }

    public NodeInfo getRoot()
            throws XPathException
    {
        myBuilder.close();
        return myBuilder.getCurrentRoot();
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
