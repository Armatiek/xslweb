/****************************************************************************/
/*  File:       PkgLSResourceResolver.java                                  */
/*  Author:     F. Georges                                                  */
/*  Company:    H2O Consulting                                              */
/*  Date:       2009-10-19                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo.resolver;

import java.io.IOException;
import org.expath.pkg.repo.URISpace;
import org.expath.pkg.repo.Universe;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Implementation of {@link LSResourceResolver} based on a repository and a URI space.
 *
 * @author Florent Georges
 * @date   2009-10-19
 */
public class PkgLSResourceResolver
        implements LSResourceResolver
{
    public PkgLSResourceResolver(Universe universe, URISpace space)
    {
        myResolver = new PkgEntityResolver(universe, space);
    }

    @Override
    public LSInput resolveResource(String type, String ns, String pub, String sys, String base)
    {
        try {
            InputSource src = null;
            // first try @namespace if any
            if (ns != null) {
                src = myResolver.resolveEntity(null, ns);
            }
            // then @schemaLocation if any (and @namespace failed)
            if (src == null && sys != null) {
                src = myResolver.resolveEntity(null, sys);
            }
            // if not resolved, return null, or return a DOM input
            if (src == null) {
                return null;
            }
            else {
                return new PkgLSInput(src);
            }
        }
        catch (SAXException ex) {
            // TODO: ...
            System.err.println("TODO: Error management. Something wrong in DOM resolver...");
            ex.printStackTrace();
            return null;
        }
        catch (IOException ex) {
            // TODO: ...
            System.err.println("TODO: Error management. Something wrong in DOM resolver...");
            ex.printStackTrace();
            return null;
        }
    }

    private EntityResolver myResolver;
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
