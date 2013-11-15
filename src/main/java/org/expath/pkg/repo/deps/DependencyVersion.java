/****************************************************************************/
/*  File:       DependencyVersion.java                                      */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-01-27                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo.deps;

import org.expath.pkg.repo.PackageException;

/**
 * A specific version a dependency depends on.
 * 
 * @author Florent Georges
 * @date   2011-27-27
 */
public abstract class DependencyVersion
{
    public abstract boolean isCompatible(String version)
            throws PackageException;

    public static DependencyVersion makeVersion(String versions, String semver, String min, String max)
            throws PackageException
    {
        if (versions != null) {
            checkNull(semver, "semver", "versions");
            checkNull(min, "semver-min", "versions");
            checkNull(max, "semver-max", "versions");
            return new DepVersions(versions);
        }
        if (semver != null) {
            checkNull(min, "semver-min", "semver");
            checkNull(max, "semver-max", "semver");
            return new DepSemver(semver);
        }
        if (min != null) {
            if (max == null) {
                return new DepSemverMin(min);
            }
            return new DepSemverMinMax(min, max);
        }
        if (max != null) {
            return new DepSemverMax(max);
        }
        return new DepNoVersion();
    }

    private static void checkNull(String val, String referee, String reference)
            throws PackageException
    {
        if (val != null) {
            throw new PackageException(referee + " cannot be set when " + reference + " is set");
        }
    }
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
