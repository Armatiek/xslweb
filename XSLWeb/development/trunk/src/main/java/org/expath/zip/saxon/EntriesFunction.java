/****************************************************************************/
/*  File:       EntriesFunction.java                                        */
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
public class EntriesFunction
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
        return 1;
    }

    @Override
    public SequenceType[] getArgumentTypes()
    {
        final int      cardinality = StaticProperty.EXACTLY_ONE;
        // TODO: Is this taking xs:anyURI into account?
        final ItemType itype       = BuiltInAtomicType.STRING;
        SequenceType   stype       = SequenceType.makeSequenceType(itype, cardinality);
        return new SequenceType[]{ stype };
    }

    @Override
    public SequenceType getResultType(SequenceType[] params)
    {
        final int      cardinality = StaticProperty.EXACTLY_ONE;
        final int      kind        = Type.ELEMENT;
        final String   uri         = ZipConstants.ZIP_NS_URI;
        final NamePool pool        = myConfig.getNamePool();
        final ItemType itype       = new NameTest(kind, uri, "file", pool);
        return SequenceType.makeSequenceType(itype, cardinality);
    }

    @Override
    public ExtensionFunctionCall makeCallExpression()
    {
        return new EntriesCall();
    }

    private static final String LOCAL_NAME = "entries";
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
