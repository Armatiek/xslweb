/****************************************************************************/
/*  File:       UpdateEntriesFunction.java                                  */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-08-09                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.zip.saxon;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.SequenceType;
import org.expath.pkg.saxon.EXPathFunctionDefinition;
import org.expath.zip.ZipConstants;

/**
 * TODO: Doc...
 *
 * @author Florent Georges
 * @date   2009-08-09
 */
public class UpdateEntriesFunction
        extends EXPathFunctionDefinition
{
    @Override
    public void setConfiguration(Configuration config)
    {
        myConfig = config;
    }

    @Override
    public StructuredQName getFunctionQName()
    {
        final String uri    = ZipConstants.ZIP_NS_URI;
        final String prefix = ZipConstants.ZIP_NS_PREFIX;
        return new StructuredQName(prefix, uri, LOCAL_NAME);
    }

    @Override
    public int getMinimumNumberOfArguments()
    {
        return 2;
    }

    @Override
    public SequenceType[] getArgumentTypes()
    {
        final int      one     = StaticProperty.EXACTLY_ONE;
        final int      kind    = Type.ELEMENT;
        final String   uri     = ZipConstants.ZIP_NS_URI;
        final NamePool pool    = myConfig.getNamePool();
        final ItemType ifirst  = new NameTest(kind, uri, "file", pool);
        SequenceType   sfirst  = SequenceType.makeSequenceType(ifirst, one);
        final ItemType isecond = BuiltInAtomicType.STRING;
        SequenceType   ssecond = SequenceType.makeSequenceType(isecond, one);
        return new SequenceType[]{ sfirst, ssecond };
    }

    @Override
    public SequenceType getResultType(SequenceType[] params)
    {
        // TODO: FIXME: How to represent "empty()" ?
        final int      zero  = StaticProperty.ALLOWS_ZERO_OR_ONE;
        final ItemType itype = AnyItemType.getInstance();
        return SequenceType.makeSequenceType(itype, zero);
    }

    @Override
    public ExtensionFunctionCall makeCallExpression()
    {
        return new UpdateEntriesCall();
    }

    private static final String LOCAL_NAME = "update-entries";
    private Configuration myConfig;
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
