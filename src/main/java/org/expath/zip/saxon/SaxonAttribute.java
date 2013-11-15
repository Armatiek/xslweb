/****************************************************************************/
/*  File:       SaxonAttribute.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-02-21                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.zip.saxon;

import net.sf.saxon.om.NodeInfo;
import org.expath.zip.Attribute;

/**
 * The implementation for Saxon of an abstract attribute.
 *
 * @author Florent Georges
 * @date   2011-02-21
 */
public class SaxonAttribute
        implements Attribute
{
    public SaxonAttribute(NodeInfo node)
    {
        // TODO: Should we perform some checks (is it non-null?, is it an
        // element node?, etc.)  Or is it guaranteed by construction?
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
