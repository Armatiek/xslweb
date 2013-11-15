/****************************************************************************/
/*  File:       Packages.java                                               */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-11-18                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo;

import org.expath.pkg.repo.deps.Semver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.xml.transform.stream.StreamSource;
import org.expath.pkg.repo.util.Logger;

/**
 * One abstract package, possible containing several versions.
 *
 * If it contains several versions, they are ordered following the SemVer spec.
 * If the versionning scheme does not follow SemVer, then the order is undefined.
 *
 * @author Florent Georges
 * @date   2010-11-18
 */
public class Packages
        implements Universe
{
    public Packages(String name)
    {
        myName = name;
    }

    public String name()
    {
        return myName;
    }

    public Collection<Package> packages()
    {
        return myPackages;
    }

    public Package latest()
    {
        if ( myPackages.isEmpty() ) {
            return null;
        }
        return myPackages.get(0);
    }

    public void add(Package pkg)
    {
        int i = 0;
        Iterator<Package> it = myPackages.iterator();
        while ( it.hasNext() ) {
            Package curr = it.next();
            if ( lower(curr, pkg) ) {
                break;
            }
            i++;
        }
        myPackages.add(i, pkg);
    }

    /**
     * Return a specific version of the package.
     * 
     * Note this does not implement the SemVer semantics (i.e. it does not try
     * to "match" a version pattern, it looks for an exact match, char by char).
     */
    public Package version(String version)
    {
        for ( Package p : myPackages ) {
            if ( p.getVersion().equals(version) ) {
                return p;
            }
        }
        return null;
    }

    public void remove(Package pkg)
    {
        myPackages.remove(pkg);
    }

    @Override
    public StreamSource resolve(String href, URISpace space)
            throws PackageException
    {
        // by default, look into the declared dependencies
        return resolve(href, space, true);
    }

    @Override
    public StreamSource resolve(String href, URISpace space, boolean transitive)
            throws PackageException
    {
        LOG.fine("Package, resolve in {0}: ''{1}'' ({2})", space, href, transitive);
        return latest().resolve(href, space, transitive);
    }

    private static boolean lower(Package lhs, Package rhs)
    {
        try {
            Semver lver = new Semver(lhs.getVersion());
            Semver rver = new Semver(rhs.getVersion());
            return lver.matchesMin(rver);
        }
        catch ( PackageException ex ) {
            // the versionning scheme does not follow SemVer (the version
            // numbers are not syntactly valid SemVer numbers)
            return true;
        }
    }

    private String myName;
    private List<Package> myPackages = new ArrayList<Package>();
    private static final Logger LOG = Logger.getLogger(Packages.class);
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
