/****************************************************************************/
/*  File:       SaxonSequence.java                                          */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-03-10                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient.saxon;

import java.io.OutputStream;
import java.util.Properties;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.model.Sequence;

/**
 * Saxon implementation of {@link Sequence}, relying on {@link SequenceIterator}.
 *
 * @author Florent Georges
 * @date   2011-03-10
 */
public class SaxonSequence
        implements Sequence
{
    public SaxonSequence(SequenceIterator it, XPathContext ctxt)
    {
        myIt = it;
        myCtxt = ctxt;
    }

    @Override
    public boolean isEmpty()
            throws HttpClientException
    {
        try {
            return myIt == null || myIt.getAnother().next() == null;
        }
        catch ( XPathException ex ) {
            throw new HttpClientException("Error getting another iterator", ex);
        }
    }

    @Override
    public Sequence next()
            throws HttpClientException
    {
        Item item;
        try {
            item = myIt == null ? null : myIt.next();
        }
        catch ( XPathException ex ) {
            throw new HttpClientException("Error getting the next item in the sequence", ex);
        }
        SequenceIterator it = SingletonIterator.makeIterator(item);
        return new SaxonSequence(it, myCtxt);
    }

    @Override
    public void serialize(OutputStream out, Properties params)
            throws HttpClientException
    {
        // TODO: childs can be childs of http:body.  Even if the 'http' prefix is
        // in @exclude-result-prefixes, its namespace declaration is serialized
        // in the output (I guess because http:body is still the parent of
        // childs, so ns normalization requires it.  How to do?
        // TODO: Look for what Norm uses in Calabash.  I know I saw a post from
        // him on the Saxon mailing list on that subject...
        Configuration config = myCtxt.getConfiguration();
        try {
            QueryResult.serializeSequence(myIt, config, out, params);
        }
        catch ( XPathException ex ) {
            throw new HttpClientException("Error serializing the single part body", ex);
        }
    }

    private SequenceIterator myIt;
    private XPathContext myCtxt;
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
