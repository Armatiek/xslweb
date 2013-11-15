/****************************************************************************/
/*  File:       DepSemverMinMax.java                                        */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-01-27                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo.deps;

import org.expath.pkg.repo.PackageException;

/**
 * A dependency version using {@code @semver-min} and {@code @semver-max}.
 * 
 * @author Florent Georges
 * @date   2011-27-27
 */
class DepSemverMinMax
        extends DependencyVersion
{
    public DepSemverMinMax(String min, String max)
            throws PackageException
    {
        myMin = new Semver(min);
        myMax = new Semver(max);
    }

    @Override
    public boolean isCompatible(String version)
            throws PackageException
    {
        Semver rhs = new Semver(version);
        return myMin.matchesMin(rhs) && myMax.matchesMax(rhs) && !myMax.matches(rhs);
    }

    private Semver myMin;
    private Semver myMax;
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
