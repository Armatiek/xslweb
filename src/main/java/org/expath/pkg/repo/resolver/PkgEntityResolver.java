/****************************************************************************/
/*  File:       PkgEntityResolver.java                                      */
/*  Author:     F. Georges                                                  */
/*  Company:    H2O Consulting                                              */
/*  Date:       2010-05-15                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo.resolver;

import java.io.IOException;
import javax.xml.transform.stream.StreamSource;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.URISpace;
import org.expath.pkg.repo.Universe;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Implementation of {@link EntityResolver} based on a repository and a URI space.
 *
 * @author Florent Georges
 * @date   2010-05-15
 */
public class PkgEntityResolver
        implements EntityResolver
{
    public PkgEntityResolver(Universe universe, URISpace space)
    {
        myUniverse = universe;
        mySpace    = space;
    }

    // TODO: What to do with the public ID?  Ignore it, check it is null?
    // Try to resolve it as well?  I would say ignore, as for me the packaging
    // system is based more on system IDs...
    @Override
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException
                 , IOException
    {
        // try to resolve the system ID as a file in the repo
        StreamSource resolved;
        try {
            resolved = myUniverse.resolve(systemId, mySpace);
        }
        catch ( PackageException ex ) {
            // TODO: ...
            System.err.println("TODO: Error management. Something wrong in entity resolver...");
            ex.printStackTrace();
            return null;
        }
        // use it if it is there
        if ( resolved == null ) {
            return null;
        }
        InputSource src = new InputSource(resolved.getInputStream());
        src.setSystemId(resolved.getSystemId());
        return src;
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
