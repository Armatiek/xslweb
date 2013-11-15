/****************************************************************************/
/*  File:       Package.java                                                */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-09-18                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo;

import org.expath.pkg.repo.deps.DependencyVersion;
import org.expath.pkg.repo.deps.ProcessorDependency;
import org.expath.pkg.repo.deps.PkgDependency;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.transform.stream.StreamSource;
import org.expath.pkg.repo.util.Logger;

/**
 * Represent a particular package, with a particular version.
 *
 * @author Florent Georges
 * @date   2010-09-18
 */
public class Package
        implements Universe
{
    public Package(Repository repo, Storage.PackageResolver resolver, String name, String abbrev, String version, String title, String home)
    {
        myRepo = repo;
        myResolver = resolver;
        myName = name;
        myAbbrev = abbrev;
        myVersion = version;
        myTitle = title;
        myHome = home;
        myInfos = new HashMap<String, PackageInfo>();
        myPublicUris = new EnumMap<URISpace, Map<String, String>>(URISpace.class);
    }

    /**
     * Actually remove the package from the storage layer.
     */
    public void removeContent()
            throws PackageException
    {
        myResolver.removePackage();
    }

    /**
     * {@code relative} is the filename relative to the module dir.
     */
    public void addPublicUri(URISpace space, String href, String relative)
            throws PackageException
    {
        LOG.fine("Package ''{0}'', add URI in {1}: ''{2}'', to map to ''({3})''", myName, space, href, relative);
        Map<String, String> map = myPublicUris.get(space);
        if ( map == null ) {
            map = new HashMap<String, String>();
            myPublicUris.put(space, map);
        }
        if ( map.get(href) != null ) {
            String msg = "Public URI already exists in this package: " + href + " (" + relative + ")";
            throw new PackageException(msg);
        }
        map.put(href, relative);
    }

    /**
     * Resolve the href if it exists in this package, or return null.
     *
     * Do not "recurse" into the declared dependencies.
     */
    private StreamSource resolveInThisPackage(String href, URISpace space)
            throws PackageException
    {
        // first, try to resolve in the extensions
        for ( PackageInfo info : myInfos.values() ) {
            StreamSource res = info.resolve(href, space);
            if ( res != null ) {
                return res;
            }
        }
        // the map for this URI space
        Map<String, String> map = myPublicUris.get(space);
        if ( map == null ) {
            LOG.fine("Package ''{0}'', no URI in {1}", myName, space);
            return null;
        }
        // the file for this href in this package's map
        String rel = map.get(href);
        if ( rel == null ) {
            LOG.fine("Package ''{0}'', not in {1}: ''{2}''", myName, space, href);
            return null;
        }
        LOG.fine("Package ''{0}'', resolved ''{1}'' in {2} to ''{3}''", myName, href, space, rel);
        try {
            // resolve the file ref into a JAXP source
            return myResolver.resolveComponent(rel);
        }
        catch ( Storage.NotExistException ex ) {
            throw new PackageException("Resource does NOT exist in the package", ex);
        }
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
        LOG.fine("Package ''{0}'', resolve in {1}: ''{2}'' ({3})", myName, space, href, transitive);
        StreamSource src = resolveInThisPackage(href, space);
        if ( src != null ) {
            return src;
        }
        if ( transitive ) {
            for ( PkgDependency dep : myPkgDeps ) {
                Package depended = resolveDependency(dep);
                // if the dependency is not found, just ignore it
                // TODO: Create an option to treat it as error/warning/nothing...
                if ( depended != null ) {
                    src = depended.resolve(href, space, transitive);
                }
                if ( src != null ) {
                    return src;
                }
            }
        }
        return null;
    }

    /**
     * Return the latest available package from the repo compatible with the dependency.
     */
    private Package resolveDependency(PkgDependency dep)
            throws PackageException
    {
        String name = dep.getPkgName();
        DependencyVersion version = dep.getVersion();
        Packages pp = myRepo.getPackages(name);
        if ( pp == null ) {
            return null;
        }
        for ( Package pkg : pp.packages() ) {
            if ( version.isCompatible(pkg.getVersion()) ) {
                return pkg;
            }
        }
        return null;
    }

    public Storage.PackageResolver getResolver()
    {
        return myResolver;
    }

    /**
     * The package name.
     */
    public String getName()
    {
        return myName;
    }

    /**
     * The package abbrev.
     */
    public String getAbbrev()
    {
        return myAbbrev;
    }

    /**
     * The package version.
     */
    public String getVersion()
    {
        return myVersion;
    }

    /**
     * Return the info object with the given name, null if there is no such info.
     */
    public PackageInfo getInfo(String name)
    {
        return myInfos.get(name);
    }

    /**
     * Set the info object for the given name.
     *
     * @throws PackageException
     *      If there is already an info object with that name.
     */
    public void addInfo(String name, PackageInfo info)
            throws PackageException
    {
        if ( getInfo(name) != null ) {
            throw new PackageException("Info for '" + name + "' already set");
        }
        myInfos.put(name, info);
    }

    /**
     * Set the info object for the given name.
     */
    public void setInfo(String name, PackageInfo info)
    {
        myInfos.put(name, info);
    }

    /**
     * Return the dependencies on packages.
     */
    public Collection<PkgDependency> getPackageDeps()
    {
        return myPkgDeps;
    }

    /**
     * Add a dependency on a package.
     */
    public void addPackageDep(String pkg, String versions, String semver, String min, String max)
            throws PackageException
    {
        DependencyVersion ver = DependencyVersion.makeVersion(versions, semver, min, max);
        PkgDependency dep = new PkgDependency(pkg, ver);
        myPkgDeps.add(dep);
    }

    /**
     * Return the dependencies on processors.
     */
    public Collection<ProcessorDependency> getProcessorDeps()
    {
        return myProcDeps;
    }

    /**
     * Add a dependency on a processor.
     */
    public void addProcessorDep(String proc, String versions, String semver, String min, String max)
    {
        ProcessorDependency dep = new ProcessorDependency(proc);
        dep.setVersions(versions);
        dep.setSemver(semver);
        dep.setSemverMin(min);
        dep.setSemverMax(max);
        myProcDeps.add(dep);
    }

    private Repository myRepo;
    private Storage.PackageResolver myResolver;
    private String myName;
    private String myAbbrev;
    private String myVersion;
    private String myTitle;
    private String myHome;
    private List<PkgDependency> myPkgDeps = new ArrayList<PkgDependency>();
    private List<ProcessorDependency> myProcDeps = new ArrayList<ProcessorDependency>();
    private Map<String, PackageInfo> myInfos;
    private Map<URISpace, Map<String, String>> myPublicUris;
    private static final Logger LOG = Logger.getLogger(Package.class);
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
