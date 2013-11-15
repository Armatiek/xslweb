/****************************************************************************/
/*  File:       SaxonAttribute.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-03-10                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient.saxon;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.NumericValue;
import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.model.Attribute;

/**
 * Implementation of {@link Attribute} for Saxon.
 *
 * @author Florent Georges
 * @date   2011-03-10
 */
public class SaxonAttribute
        implements Attribute
{
    public SaxonAttribute(NodeInfo node)
    {
        myNode = node;
    }

    @Override
    public String getLocalName()
    {
        return myNode.getLocalPart();
    }

    @Override
    public String getNamespaceUri()
    {
        return myNode.getURI();
    }

    @Override
    public String getValue()
    {
        return myNode.getStringValue();
    }

    @Override
    public boolean getBoolean()
            throws HttpClientException
    {
        String str = myNode.getStringValue();
        AtomicValue val;
        try {
            val = BooleanValue.fromString(str).asAtomic();
        }
        catch ( XPathException ex ) {
            throw new HttpClientException("Error parse the attribute value as boolean", ex);
        }
        if ( ! ( val instanceof BooleanValue ) ) {
            throw new HttpClientException("@" + getLocalName() + " is not a boolean");
        }
        BooleanValue b = (BooleanValue) val;
        return b.getBooleanValue();
    }

    @Override
    public int getInteger()
            throws HttpClientException
    {
        String str = myNode.getStringValue();
        NumericValue val = NumericValue.parseNumber(str);
        if ( NumericValue.isInteger(val) ) {
            throw new HttpClientException("@" + getLocalName() + " is not an integer");
        }
        IntegerValue i = (IntegerValue) val;
        return i.asBigInteger().intValue();
    }

    private NodeInfo myNode;
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
