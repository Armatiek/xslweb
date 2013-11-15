/****************************************************************************/
/*  File:       PkgURIResolver.java                                         */
/*  Author:     F. Georges                                                  */
/*  Company:    H2O Consulting                                              */
/*  Date:       2010-05-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo.resolver;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.URISpace;
import org.expath.pkg.repo.Universe;

/**
 * Implementation of {@link URIResolver} based on a repository and a URI space.
 *
 * @author Florent Georges
 * @date   2010-05-12
 */
public class PkgURIResolver
        implements URIResolver
{
    public PkgURIResolver(Universe universe, URISpace space)
    {
        myUniverse = universe;
        mySpace = space;
    }

    @Override
    public Source resolve(String href, String base)
            throws TransformerException
    {
        try {
            return myUniverse.resolve(href, mySpace);
        }
        catch ( PackageException ex ) {
            throw new TransformerException("Error resolving the URI", ex);
        }
    }

    public Universe getUniverse()
    {
        return myUniverse;
    }

    public URISpace getSpace()
    {
        return mySpace;
    }

    private Universe myUniverse;
    private URISpace mySpace;
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
