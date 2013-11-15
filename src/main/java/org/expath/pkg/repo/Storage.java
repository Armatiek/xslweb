/****************************************************************************/
/*  File:       Storage.java                                                */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-10-07                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import javax.xml.transform.stream.StreamSource;

/**
 * Abstract the physical storage of a repository.
 *
 * For instance, two standard implementations are on disk (the official layout
 * defined in the spec) and within the classpath.  The former resolves resource
 * names as filenames on the disk, the later resolves them as resource names
 * within the classpath.
 *
 * @author Florent Georges
 * @date   2010-10-07
 */
public abstract class Storage
{
    public static abstract class PackageResolver
    {
        /**
         * Return the package ID within the repository (i.e. its subdirectory name).
         */
        public abstract String getResourceName();

        /**
         * Resolve a resource within the package "root dir".
         */
        public abstract StreamSource resolveResource(String path)
                throws PackageException
                     , NotExistException;
        /**
         * Resolve a resource within the package "module dir".
         */
        public abstract StreamSource resolveComponent(String path)
                throws PackageException
                     , NotExistException;
        /**
         * Delete a package resources from the repository storage.
         *
         * Throw an exception when this is not supported on a particular
         * storage type.
         */
        public abstract void removePackage()
                throws PackageException;
    }

    public static class NotExistException
            extends Exception
    {
        public NotExistException(String msg)
        {
            super(msg);
        }

        public NotExistException(String msg, Throwable ex)
        {
            super(msg, ex);
        }
    }

    /**
     * Return whether this storage is read-only.
     */
    public abstract boolean isReadOnly();

    /**
     * Return a resolver for a specific package.
     *
     * {@code rsrc_name} is the name of the resource representing the package.
     * For instance, on the filesystem that is the name of the root directory,
     * and in the classpath that is the name of the root package (in the Java
     * sense).
     *
     * {@code abbrev} is the abbrev of the package (as in the package descriptor,
     * and must match the module dir within the package).
     */
    public abstract PackageResolver makePackageResolver(String rsrc_name, String abbrev)
            throws PackageException;

    public abstract InputStream resolveRsrc(String path)
            throws PackageException;

    /**
     * Return the list of installed packages.
     *
     * The returned list is the list of the package directories within the
     * repository.
     *
     * TODO: Cache the list.
     */
    public Set<String> listPackageDirectories()
            throws PackageException
    {
        Set<String> result = new HashSet<String>();
        try {
            InputStream stream = resolveRsrc(".expath-pkg/packages.txt");
            if ( stream == null ) {
                // return an empty set if the list does not exist
                // that can be the case for instance when the repo is still empty
                return result;
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ( (line = in.readLine()) != null ) {
                int pos = line.indexOf(' ');
                String dir = line.substring(0, pos);
                result.add(dir);
            }
            return result;
        }
        catch ( IOException ex ) {
            throw new PackageException("Error reading the package list", ex);
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
