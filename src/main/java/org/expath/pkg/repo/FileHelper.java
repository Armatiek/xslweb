/****************************************************************************/
/*  File:       FileHelper.java                                             */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-04-14                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Centralize some file-related utilities.
 *
 * @author Florent Georges
 * @date   2011-04-14
 */
public class FileHelper
{
    /**
     * Create a directory if it does not exist yet.
     * 
     * Throw an error if the directory already exists and is not actually a
     * directory, or if there is an error creating it.
     */
    public static void ensureDir(File dir)
            throws PackageException
    {
        // if it does not exist, create it
        if ( ! dir.exists() ) {
            boolean res = dir.mkdir();
            if ( ! res ) {
                throw new PackageException("Error creating the directory: " + dir);
            }
        }
        // but if it exists and is not a dir, that's an error
        else if ( ! dir.isDirectory() ) {
            throw new PackageException("The directory is not a directory: " + dir);
        }
    }

    /**
     * Create a new directory within {@code parent}.
     * 
     * The new directory is empty, and its name is based on {@code prefix},
     * and on the current date after the prefix.
     */
    public static File makeTempDir(String prefix, File parent)
            throws PackageException
    {
        Date now = new Date();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        String today = fmt.format(now);
        File tmp;
        try {
            tmp = File.createTempFile(prefix + "-" + today + "-", ".d", parent);
        }
        catch ( IOException ex ) {
            throw new PackageException("Error creating a temporary file", ex);
        }
        if ( ! tmp.delete() ) {
            throw new PackageException("Error removing the temporary file: " + tmp);
        }
        if ( ! tmp.mkdir() ) {
            throw new PackageException("Error creating the temporary dir: " + tmp);
        }
        return tmp;
    }

    // TODO: Check atomicity in case of failure (for instance in the middle of the copy)
    public static void renameTmpDir(File from, File to)
            throws PackageException
    {
        // DEBUG: Added some debug "logs" to see what happen on Windows.  To be
        // removed or correctly re-written.
        // System.err.println("renaming " + from + " to " + to);
        boolean failed = ! from.renameTo(to);
        // System.err.println("failed? : " + failed + ", OS: " + System.getProperty("os.name"));
        // if renaming failed and we're not on Windows, that's an error
        // if renaming failed and we're on Windows, we try several things, as
        // this can be related to stpid Windows locking handling...
        if ( failed && ! System.getProperty("os.name").startsWith("Windows") ) {
            throw new PackageException("Renaming '" + from + "' to '" + to + "' failed");
        }
        // if the renaming fails, max. 5 times...
        for ( int i = 0; failed && i < 5; ++ i ) {
            // System.err.println("renaming loop #" + i);
            // call the garbage collector
            // TODO: Document why (Windows, locks, etc.)
            System.gc();
            // and try again
            failed = ! from.renameTo(to);
        }
        // if that was not enough and renaming still failed after 5 attempts
        if ( failed ) {
            // System.err.println("trying to copy");
            // then try to copy the whole thing
            copyDir(from, to);
            boolean res = deleteDir(from);
            if ( ! res ) {
                System.err.println("Error deleting dir: " + from);
            }
        }
        // // if that failed too, then that's an error
        // if ( failed ) {
        //     String msg = "Error moving the temp dir to the repo: ";
        //     throw new PackageException(msg + from + " -> " + to);
        // }
    }

    private static void copyDir(File from, File to)
            throws PackageException
    {
        File[] files = from.listFiles();
        if ( files == null ) {  // null if security restricted
            throw new PackageException("Failed to list contents of " + from);
        }
        if ( to.exists() ) {
            throw new PackageException("Destination '" + to + "' exists");
        }
        if ( ! to.mkdirs() ) {
            throw new PackageException("Destination '" + to + "' directory cannot be created");
        }
        if ( ! to.canWrite() ) {
            throw new PackageException("Destination '" + to + "' cannot be written to");
        }
        for ( File file : files ) {
            File copied = new File(to, file.getName());
            // System.err.println("handling subcopy of " + file + " to " + copied);
            if ( file.isDirectory() ) {
                copyDir(file, copied);
            }
            else {
                try {
                    byte[] buffer = new byte[4096];
                    InputStream in = new FileInputStream(file);
                    OutputStream out = new FileOutputStream(copied);
                    int count;
                    while ( (count = in.read(buffer)) > 0 ) {
                        out.write(buffer, 0, count);
                    }
                }
                catch ( IOException ex ) {
                    throw new PackageException("Error copying '" + file + "' to '" + copied + "'", ex);
                }
            }
        }
    }

    private static boolean deleteDir(File f)
    {
        if ( f.isDirectory() ) {
            boolean res = true;
            for ( File child : f.listFiles() ) {
                res = deleteDir(child) && res;
            }
            res = f.delete() && res;
            return res;
        }
        else {
            return f.delete();
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
