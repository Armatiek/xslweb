/****************************************************************************/
/*  File:       Universe.java                                               */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-01-26                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo;

import javax.xml.transform.stream.StreamSource;

/**
 * Represent the universe to use to compile a component.
 *
 * The universe is the entire set of packages visible to the processor.  This
 * concept is similar to Java's classpath, Perl's {@code INC} list or Python's
 * module search path.
 *
 * TODO: Returning a {@link StreamSource} in return of {@link #resolve()} is
 * probably not what we want.  What for instance if the matched component is an
 * extension written in, say, Java. It makes not sense to return a stream source
 * then.  We should rather use a listener mechanism.
 *
 * @author Florent Georges
 * @date   2011-01-26
 */
public interface Universe
{
    /**
     * Resolve a specific URI, in a specific space, into a stream source.
     *
     * Whether package dependencies are used to resolve the href has to be
     * defined by each implementation of this interface.
     */
    public StreamSource resolve(String href, URISpace space)
            throws PackageException;

    /**
     * Resolve a specific URI, in a specific space, into a stream source.
     *
     * Whether package dependencies are used to resolve the href is set by
     * {@code transitive}.
     */
    public StreamSource resolve(String href, URISpace space, boolean transitive)
            throws PackageException;
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
