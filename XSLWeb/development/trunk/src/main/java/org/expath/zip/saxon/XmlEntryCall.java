/****************************************************************************/
/*  File:       XmlEntryCall.java                                           */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-08-09                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.zip.saxon;

import javax.xml.transform.Source;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingleNodeIterator;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.StringValue;
import org.expath.zip.ZipException;
import org.expath.zip.ZipFacade;

/**
 * TODO: Doc...
 *
 * @author Florent Georges
 * @date   2009-08-09
 */
public class XmlEntryCall
        extends ExtensionFunctionCall
{
    @Override
    public void supplyStaticContext(StaticContext ctxt, int location, Expression[] args)
            throws XPathException
    {
        myStaticContext = ctxt;
    }

    @Override
    public SequenceIterator call(SequenceIterator[] params, XPathContext ctxt)
            throws XPathException
    {
        // num of params
        if ( params.length != 2 ) {
            throw new XPathException("There is not exactly 2 params: " + params.length);
        }
        // the first param
        Item first = params[0].next();
        if ( first == null ) {
            throw new XPathException("The first param is an empty sequence");
        }
        if ( params[0].next() != null ) {
            throw new XPathException("The first param sequence has more than one item");
        }
        if ( ! ( first instanceof StringValue ) ) {
            throw new XPathException("The first param is not a string (or anyURI)");
        }
        String file = first.getStringValue();
        // the second param
        Item second = params[1].next();
        if ( second == null ) {
            throw new XPathException("The second param is an empty sequence");
        }
        if ( params[1].next() != null ) {
            throw new XPathException("The second param sequence has more than one item");
        }
        if ( ! ( second instanceof StringValue ) ) {
            throw new XPathException("The second param is not a string (or anyURI)");
        }
        String entry = second.getStringValue();
        // the actual call
        ZipFacade zip = new ZipFacade(myStaticContext.getBaseURI());
        Source src;
        try {
            src = zip.xmlEntry(file, entry);
        }
        catch ( ZipException ex ) {
            throw new XPathException("Error handling the zip:xml-entry function", ex);
        }
        if ( src == null ) {
            return EmptySequence.getInstance().iterate();
        }
        NodeInfo res = ctxt.getConfiguration().buildDocument(src);
        return SingleNodeIterator.makeIterator(res);
    }

    private StaticContext myStaticContext;
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
