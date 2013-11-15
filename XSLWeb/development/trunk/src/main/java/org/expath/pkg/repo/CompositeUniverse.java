/****************************************************************************/
/*  File:       CompositeUniverse.java                                      */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-01-30                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo;

import java.util.ArrayList;
import java.util.Collection;
import javax.xml.transform.stream.StreamSource;
import org.expath.pkg.repo.util.Logger;

/**
 * Create a universe by assembling several sub-universes.
 *
 * The "dependency-recursive" property is set at construction, i.e. do the
 * resolving mechanism look into the declared dependencies, recursively?
 *
 * @author Florent Georges
 * @date   2011-01-30
 */
public class CompositeUniverse
        implements Universe
{
    public CompositeUniverse(boolean transitive)
    {
        myTransitive = transitive;
    }

    public void addUniverse(Universe universe)
    {
        mySubUniverses.add(universe);
    }

    @Override
    public StreamSource resolve(String href, URISpace space)
            throws PackageException
    {
        return resolve(href, space, myTransitive);
    }

    @Override
    public StreamSource resolve(String href, URISpace space, boolean transitive)
            throws PackageException
    {
        LOG.fine("Composite universe, resolve in {0}: ''{1}'' ({2})", space, href, transitive);
        for ( Universe sub : mySubUniverses ) {
            // TODO: Because this composite universe has been created explicitly
            // to be transitive or not, maybe we should actually pass down this
            // property instead, i.e. 'myTransitive' instead of 'transitive'.
            StreamSource src = sub.resolve(href, space, transitive);
            if ( src != null ) {
                return src;
            }
        }
        return null;
    }

    private boolean myTransitive;
    private Collection<Universe> mySubUniverses = new ArrayList<Universe>();
    private static final Logger LOG = Logger.getLogger(CompositeUniverse.class);
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
