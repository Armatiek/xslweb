/****************************************************************************/
/*  File:       Repository.java                                             */
/*  Author:     F. Georges                                                  */
/*  Company:    H2O Consulting                                              */
/*  Date:       2009-10-19                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import javax.xml.transform.stream.StreamSource;
import org.expath.pkg.repo.Storage.PackageResolver;
import org.expath.pkg.repo.parser.DescriptorParser;
import org.expath.pkg.repo.util.Logger;

/**
 * Represent a standard EXPath package repository structure on the disk.
 *
 * TODO: The resolution part has been (more or less completely) adapted to the
 * new structure, using directly the package descriptors.  We still need to
 * adapt the install and removing processes, and other admin services here...!
 * 
 * TODO: Because we have a repository descriptor with the list of installed
 * packages (by the spec: .expath-pkg/packages.txt and .expath-pkg/packages.xml),
 * we don't have to parse all the package descriptors in the repository at the
 * instantiation of the object.  We can instead create "fake" packages with
 * only the information from the repository descriptor, and parse the whole
 * package descriptor only when the user ask for that package.
 * 
 * That way, the creation of a Repository object just needs to parse the
 * repository descriptor, and parses the package descriptors only as needed.
 *
 * @author Florent Georges
 * @date   2009-10-19
 */
public class Repository
        implements Universe
{
    public Repository(Storage storage)
            throws PackageException
    {
        LOG.info("Create a new repository with storage: {0}", storage);
        myStorage = storage;
        // dynamically register extensions from the classpath
        ServiceLoader<Extension> loader = ServiceLoader.load(Extension.class);
        for ( Extension e : loader ) {
            registerExtension(e);
        }
        // TODO: Enable lazy initialization...
        parsePublicUris();
    }

    /**
     * Shortcut for {@code makeDefaultRepo(null)}.
     */
    public static Repository makeDefaultRepo()
            throws PackageException
    {
        return makeDefaultRepo(null);
    }

    /**
     * Return a repository instantiated from default location.
     * 
     * If the parameter is not null, it uses it.  If not, then looks at the
     * system property {@code expath.repo}, and if it is not set at the
     * environment variable {@code EXPATH_REPO}.  This string is interpreted
     * as a directory path, which must point to a repository.
     * 
     * It throws an exception if the the directory does not exist (or is not a
     * directory), or if there is any error creating the repository object from
     * it.
     */
    public static Repository makeDefaultRepo(String dir)
            throws PackageException
    {
        if ( dir == null ) {
            dir = System.getProperty("expath.repo");
        }
        if ( dir == null ) {
            dir = System.getenv("EXPATH_REPO");
        }
        if ( dir != null ) {
            File f = new File(dir);
            if ( ! f.exists() ) {
                throw new PackageException("Repo directory does not exist: " + dir);
            }
            if ( ! f.isDirectory() ) {
                throw new PackageException("Repo is not a directory: " + dir);
            }
            try {
                Storage storage = new FileSystemStorage(f);
                return new Repository(storage);
            }
            catch ( PackageException ex ) {
                throw new PackageException("Error setting the repo (" + dir + ")", ex);
            }
        }
        return null;
    }

    /**
     * ...
     */
    final public void registerExtension(Extension ext)
            throws PackageException
    {
        if ( ! myExtensions.containsKey(ext.getName()) ) {
            myExtensions.put(ext.getName(), ext);
            ext.init(this, myPackages);
        }
    }

    public Collection<Packages> listPackages()
    {
        return myPackages.values();
    }

    public Packages getPackages(String name)
    {
        return myPackages.get(name);
    }

    /**
     * ...
     *
     * TODO: Must be delegated to the storage!
     */
    public static Repository createRepository(File dir)
            throws PackageException
    {
        if ( dir.exists() ) {
            // must be a dir and empty, or that's an error
            if ( ! dir.isDirectory() || dir.list() == null ) {
                throw new PackageException("File exists and is not a directory (" + dir + ")");
            }
            if ( dir.list().length > 0 ) {
                throw new PackageException("Directory exists and is not empty (" + dir + ")");
            }
            // TODO: Add a force option to delete the dir if it exists and is
            // not empty?
        }
        else {
            if ( ! dir.mkdir() ) {
                throw new PackageException("Error creating the directory (" + dir + ")");
            }
        }
        // here, we know 'dir' is a directory and is empty...
        File priv_dir = new File(dir, ".expath-pkg/");
        if ( ! priv_dir.mkdir() ) {
            throw new PackageException("Error creating the private directory (" + priv_dir + ")");
        }
        return new Repository(new FileSystemStorage(dir));
    }

    /**
     * Install a XAR package into this repository.
     *
     * If force is false, this is an error if the same package has already
     * been installed in the repository.  If it is true, it is first deleted
     * if existing.
     *
     * Return the directory where the package has been installed, within the
     * repository.
     */
    public Package installPackage(File pkg, boolean force, UserInteractionStrategy interact)
            throws PackageException
    {
        if ( myStorage.isReadOnly() ) {
            throw new PackageException("The storage is read-only, package install not supported");
        }
        return doInstall(pkg, force, interact);
    }

    public Package installPackage(URI pkg, boolean force, UserInteractionStrategy interact)
            throws PackageException
    {
        if ( myStorage.isReadOnly() ) {
            throw new PackageException("The storage is read-only, package install not supported");
        }
        // TODO: Must be moved within the storage class (because we are writing on disk)...
        File downloaded;
        try {
            URLConnection connection = pkg.toURL().openConnection();
            InputStream instream = connection.getInputStream();
            BufferedInputStream in = new BufferedInputStream(instream);
            // just to get the name, to have a meaningful name for the tmp file
            // TODO: use the header Content-Disposition to see if there is a
            // filename param...
            String name = new File(pkg.getPath()).getName();
            downloaded = File.createTempFile(name + "-", "-expath-tmp.xar");
            FileOutputStream tmp = new FileOutputStream(downloaded);
            BufferedOutputStream out = new BufferedOutputStream(tmp);
            int i;
            byte[] buf = new byte[4096];
            while ( (i = in.read(buf)) != -1 ) {
                out.write(buf, 0, i);
            }
            out.close();
            in.close();
        }
        catch ( MalformedURLException ex ) {
            throw new PackageException("Error downloading the package", ex);
        }
        catch ( IOException ex ) {
            throw new PackageException("Error downloading the package", ex);
        }
        return doInstall(downloaded, force, interact);
    }

    /**
     * TODO: Rely on the FileSystemStorage!  Change that!
     */
    private Package doInstall(File xar_file, boolean force, UserInteractionStrategy interact)
            throws PackageException
    {
        if ( ! ( myStorage instanceof FileSystemStorage ) ) {
            throw new PackageException("Install not supported for the storage " + myStorage.getClass());
        }
        FileSystemStorage filesystem = (FileSystemStorage) myStorage;
        Package pkg = filesystem.install(xar_file, force, interact, this, myPackages);
        for ( Extension ext : myExtensions.values() ) {
            ext.init(this, pkg);
        }
        return pkg;
    }

    /**
     * TODO: ...
     */
    public void removePackage(String pkg, boolean force, UserInteractionStrategy interact)
            throws PackageException
    {
        if ( ! interact.ask("Remove package " + pkg + "?", true) ) {
            return;
        }
        // delete the package content
        Packages pp = myPackages.get(pkg);
        if ( pp == null ) {
            throw new PackageException("The package does not exist: " + pkg);
        }
        if ( pp.packages().size() != 1 ) {
            throw new PackageException("The package has several versions installed: " + pkg);
        }
        pp.latest().removeContent();
        // remove the package from the list
        myPackages.remove(pkg);
    }

    /**
     * TODO: ...
     */
    public void removePackage(String pkg, String version, boolean force, UserInteractionStrategy interact)
            throws PackageException
    {
        if ( ! interact.ask("Remove package " + pkg + ", version " + version + "?", true) ) {
            return;
        }
        // delete the package content
        Packages pp = myPackages.get(pkg);
        if ( pp == null ) {
            throw new PackageException("The package does not exist: " + pkg);
        }
        Package p = pp.version(version);
        if ( p == null ) {
            throw new PackageException("The version " + version + " does not exist for the package: " + pkg);
        }
        p.removeContent();
        pp.remove(p);
        // remove the package from the list if it was the only version
        if ( pp.latest() == null ) {
            myPackages.remove(pkg);
        }
    }

    /**
     * Resolve a URI in this repository, in the specified space, return a File.
     *
     * For each package, use only the latest version.
     *
     * TODO: What about the packages with a versionning scheme which does NOT
     * follow SemVer? (because basically those are not ordered)
     *
     * TODO: And when we want to resolve into a specific version?  For instance
     * when we are evaluating within the context of a specific package, and we
     * want to resolve only in its declared dependencies?  Or at least to use
     * the versionning of its dependencies to guide within which package we
     * should search (instead of taking always the latest systematically).  Same
     * comments for SaxonRepository.
     */
    @Override
    public StreamSource resolve(String href, URISpace space)
            throws PackageException
    {
        LOG.fine("Repository, resolve in {0}: ''{1}''", space, href);
        for ( Packages pp : myPackages.values() ) {
            Package p = pp.latest();
            StreamSource src = p.resolve(href, space);
            if ( src != null ) {
                return src;
            }
        }
        return null;
    }

    @Override
    public StreamSource resolve(String href, URISpace space, boolean transitive)
            throws PackageException
    {
        // transitive or not is meaningless, as anyway the universe is the whole
        // respository (and dependencies are defined within the repo)
        return resolve(href, space);
    }

    /**
     * ...
     */
    private synchronized void parsePublicUris()
            throws PackageException
    {
        // the list of package dirs
        Set<String> packages = myStorage.listPackageDirectories();
        // the parser
        DescriptorParser parser = new DescriptorParser();
        // loop over the packages
        for ( String p : packages ) {
            PackageResolver res = myStorage.makePackageResolver(p, null);
            StreamSource desc;
            try {
                desc = res.resolveResource("expath-pkg.xml");
            }
            catch ( Storage.NotExistException ex ) {
                throw new PackageException("Package descriptor does NOT exist in: " + p, ex);
            }
            Package pkg = parser.parse(desc.getInputStream(), p, myStorage, this);
            addPackage(pkg);
            for ( Extension ext : myExtensions.values() ) {
                ext.init(this, pkg);
            }
        }
    }

    /**
     * Package-level to be used in tests (to "manually" build a repo).
     */
    void addPackage(Package pkg)
    {
        String name = pkg.getName();
        Packages pp = myPackages.get(name);
        if ( pp == null ) {
            pp = new Packages(name);
            myPackages.put(name, pp);
        }
        pp.add(pkg);
    }

    /**
     * Package-level, only to be used in tests (to "manually" build a repo).
     */
    Repository()
    {
        // nothing, packages will be added "by hand" in tests
    }

    /**
     * ...
     */
    private Storage myStorage;
    /**
     * ...
     */
    private Map<String, Packages> myPackages = new HashMap<String, Packages>();
    /**
     * ...
     */
    private Map<String, Extension> myExtensions = new HashMap<String, Extension>();
    /**
     * The logger.
     */
    private static final Logger LOG = Logger.getLogger(Repository.class);
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
