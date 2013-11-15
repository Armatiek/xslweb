/****************************************************************************/
/*  File:       ZipFileCall.java                                            */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-08-09                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.zip.saxon;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.Base64BinaryValue;
import org.expath.zip.Element;
import org.expath.zip.ZipException;
import org.expath.zip.ZipFacade;

/**
 * TODO: Doc...
 *
 * @author Florent Georges
 * @date   2009-08-09
 */
public class ZipFileCall
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
        if ( params.length != 1 ) {
            throw new XPathException("There is not exactly 1 param: " + params.length);
        }
        // the first param
        Item item = params[0].next();
        if ( item == null ) {
            throw new XPathException("The param is an empty sequence");
        }
        if ( params[0].next() != null ) {
            throw new XPathException("The param sequence has more than one item");
        }
        if ( ! ( item instanceof NodeInfo ) ) {
            throw new XPathException("The param is not a node");
        }
        NodeInfo struct = (NodeInfo) item;
        // the actual call
        ZipFacade zip = new ZipFacade(myStaticContext.getBaseURI());
        byte[] bytes;
        try {
            Element elem = new SaxonElement(struct);
            bytes = zip.zipFile(elem);
        }
        catch ( ZipException ex ) {
            throw new XPathException("Error handling the zip:zip-file function", ex);
        }
        if ( bytes == null ) {
            return EmptyIterator.getInstance();
        }
        else {
            Base64BinaryValue res = new Base64BinaryValue(bytes);
            return SingletonIterator.makeIterator(res);
        }
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
