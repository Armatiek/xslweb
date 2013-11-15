/****************************************************************************/
/*  File:       Main.java                                                   */
/*  Author:     F. Georges                                                  */
/*  Company:    H2O Consulting                                              */
/*  Date:       2009-11-03                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009, 2010 Florent Georges (see end of file.)         */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo.tui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import javax.xml.transform.Source;
import org.expath.pkg.repo.Package;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Packages;
import org.expath.pkg.repo.Repository;
import org.expath.pkg.repo.URISpace;
import org.expath.pkg.repo.UserInteractionStrategy;

/**
 * Main class for the Text User Interface repository administration.
 *
 * <p>The repository is set either by (in order of precedence):</p>
 *
 * <ul>
 *   <li>the option {@code --repo=...}</li>
 *   <li>the Java property {@code expath.repo}</li>
 *   <li>the environment variable {@code $EXPATH_REPO}</li>
 * </ul>
 *
 * TODO: Add a {@code remove} command...
 * 
 * @author Florent Georges
 * @date   2009-11-03
 */
public class Main
{
    public static void main(String[] args)
    {
        Main m = new Main();
        m.run(args);
    }

    private void run(String[] args)
    {
        int consumed = parseOptions(args);
        if ( consumed == args.length ) {
            System.err.println("Missing command");
            System.err.println();
            usage(true);
        }
        String cmd = args[consumed];
        if ( "help".equals(args[consumed]) ) {
            doHelp(args, consumed + 1);
        }
        else if ( "version".equals(args[consumed]) ) {
            doVersion(args, consumed + 1);
        }
        else if ( "list".equals(args[consumed]) ) {
            doList(args, consumed + 1);
        }
        else if ( "install".equals(args[consumed]) ) {
            doInstall(args, consumed + 1);
        }
        else if ( "remove".equals(args[consumed]) ) {
            doRemove(args, consumed + 1);
        }
        else if ( "create".equals(args[consumed]) ) {
            doCreate(args, consumed + 1);
        }
        else if ( "lookup".equals(args[consumed]) ) {
            doLookup(args, consumed + 1);
        }
        else {
            System.err.println("Unknown command: " + cmd);
            System.err.println();
            usage(true);
        }
    }

    private int parseOptions(String[] args)
    {
        int i = 0;
        for ( ; i < args.length; ++i ) {
            String a = args[i];
            if ( a.equals("--") ) {
                return i + 1;
            }
            else if ( ! a.startsWith("-") ) {
                return i;
            }
            else if ( a.equals("--repo") || a.equals("-r") ) {
                ++i;
                myRepoDir = args[i];
            }
            else if ( a.equals("--verbose") || a.equals("-v") ) {
                myVerbose = true;
            }
            else {
                System.err.println("Unknown option: " + a);
                System.err.println();
                usage(true);
            }
        }
        return i;
    }

    private void setRepo()
    {
        try {
            myRepo = Repository.makeDefaultRepo(myRepoDir);
        }
        catch ( PackageException ex ) {
            System.err.println("Error instantiating the repo: " + ex.getMessage());
            if ( myVerbose ) {
                ex.printStackTrace(System.err);
            }
            System.exit(1);
        }
    }

    private Repository requireRepo()
    {
        if ( myRepo == null ) {
            setRepo();
            if ( myRepo == null ) {
                System.err.println("The repository is not set");
                System.exit(1);
            }
        }
        return myRepo;
    }

    private void usage(boolean error)
    {
        System.err.println("Usage:");
        System.err.println("  xrepo [--repo <repo>|--verbose] help|list|install|remove|create|lookup ...");
        System.err.println();
        System.err.println("Commands:");
        System.err.println("  help");
        System.err.println("  version");
        System.err.println("  list");
        System.err.println("  install [-b|-f] <pkg>");
        System.err.println("      -b batch operations (no interaction)");
        System.err.println("      -f override a package if already installed (force)");
        System.err.println("  remove [-b] <pkg> <version?>");
        System.err.println("      -b batch operations (no interaction)");
        System.err.println("  create <repo>");
        System.err.println("  lookup <space> <uri>");
        System.exit(error ? 1 : 0);
    }

    private void doHelp(String[] args, int consumed)
    {
        checkParams("Help", args, consumed, new String[]{ });
        usage(false);
    }

    private void doVersion(String[] args, int consumed)
    {
        checkParams("Version", args, consumed, new String[]{ });
        Properties props = new Properties();
        InputStream rsrc = Main.class.getResourceAsStream("/org/expath/pkg/repo/tui/version.properties");
        if ( rsrc == null ) {
            System.err.println("Version properties file does not exist - internal error");
            System.exit(1);
        }
        try {
            props.load(rsrc);
            rsrc.close();
        }
        catch ( IOException ex ) {
            System.err.println("Error reading the version properties: " + ex.getMessage());
            if ( myVerbose ) {
                ex.printStackTrace();
            }
            System.exit(1);
        }
        String version = props.getProperty("org.expath.pkg.repo.version");
        String revision = props.getProperty("org.expath.pkg.repo.revision");
        System.err.println("EXPath Packaging System standard on-disk repository layout manager.");
        System.err.println("Version: " + version + " (r" + revision + ")");
    }

    private void doList(String[] args, int consumed)
    {
        checkParams("List", args, consumed, new String[]{ });
        Repository repo = requireRepo();
        for ( Packages pp : repo.listPackages() ) {
            System.out.println(pp.name());
            for ( Package p : pp.packages() ) {
                System.out.println("  " + p.getVersion() + ", in " + p.getResolver().getResourceName());
            }
        }
    }

    private void doInstall(String[] args, int consumed)
    {
        //checkParams("Install", args, consumed, new String[]{"package file name"});
        try {
            boolean force = false;
            UserInteractionStrategy interact = null;
            while ( args[consumed].startsWith("-") ) {
                if ( args[consumed].equals("-b") ) {
                    ++consumed;
                    interact = new BatchUserInteraction();
                }
                else if ( args[consumed].equals("-f") ) {
                    ++consumed;
                    force = true;
                }
                else {
                    break;
                }
            }
            if ( interact == null ) {
                interact = new TextUserInteraction(myVerbose);
            }
            Repository repo = requireRepo();
            URI uri = getURI(args[consumed]);
            if ( uri == null ) {
                File f = new File(args[consumed]);
                repo.installPackage(f, force, interact);
            }
            else {
                repo.installPackage(uri, force, interact);
            }
        }
        catch ( PackageException ex ) {
            System.err.println("Error installing the package: " + ex.getMessage());
            if ( myVerbose ) {
                ex.printStackTrace();
            }
        }
    }

    private void doRemove(String[] args, int consumed)
    {
        //checkParams("Remove", args, consumed, new String[]{"package file name"});
        try {
            boolean force = false;
            UserInteractionStrategy interact = null;
            while ( args[consumed].startsWith("-") ) {
                if ( args[consumed].equals("-b") ) {
                    ++consumed;
                    interact = new BatchUserInteraction();
                }
                else {
                    break;
                }
            }
            if ( interact == null ) {
                interact = new TextUserInteraction(myVerbose);
            }
            if ( consumed >= args.length ) {
                throw new PackageException("Package name is required");
            }
            Repository repo = requireRepo();
            String pkg = args[consumed++];
            if ( consumed < args.length ) {
                String version = args[consumed++];
                if ( consumed < args.length ) {
                    throw new PackageException("Extra options given to command remove");
                }
                repo.removePackage(pkg, version, force, interact);
            }
            else {
                repo.removePackage(pkg, force, interact);
            }
        }
        catch ( PackageException ex ) {
            System.err.println("Error removing the package: " + ex.getMessage());
            if ( myVerbose ) {
                ex.printStackTrace();
            }
        }
    }

    private void doCreate(String[] args, int consumed)
    {
        checkParams("Create", args, consumed, new String[]{"repository directory name"});
        File repo_dir = new File(args[consumed]);
        try {
            Repository.createRepository(repo_dir);
        }
        catch ( PackageException ex ) {
            System.err.println("Error creating the repository: " + ex.getMessage());
            if ( myVerbose ) {
                ex.printStackTrace();
            }
        }
    }

    private void doLookup(String[] args, int consumed)
    {
        checkParams("Lookup", args, consumed, new String[]{"URI space", "URI"});
        try {
            // TODO: Catch IllegalArgumentException in case of unknown space name...
            URISpace   space    = URISpace.valueOf(args[consumed].toUpperCase());
            String     uri      = args[consumed + 1];
            Repository repo     = requireRepo();
            Source     resolved = repo.resolve(uri, space);
            if ( resolved == null ) {
                System.out.println("not found");
            }
            else {
                System.out.println(resolved.getSystemId());
            }
        }
        catch ( PackageException ex ) {
            System.err.println("Error configuring the repository: " + ex.getMessage());
            if ( myVerbose ) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Check the parameters of a command are ok.
     * 
     * @param args The actual arguments passed to the application.
     * @param params The formal params to check the args agasint.
     */
    private void checkParams(String cmd, String[] args, int consumed, String[] params)
    {
        if ( args.length > consumed + params.length ) {
            System.err.println(cmd + ": too much parameters");
            System.err.println();
            usage(true);
        }
        for ( int i = 0; i < params.length; ++i ) {
            if ( consumed + i == args.length ) {
                System.err.println(cmd + ": missing " + params[i]);
                System.err.println();
                usage(true);
            }
        }
    }

    /**
     * Return the URI with the lexical form passed as {@code s}, if possible.
     * 
     * If {@code s} is not a valid absolute URI (not a URI or a relative one),
     * then return null.
     */
    private URI getURI(String s)
    {
        URI uri;
        try {
            uri = new URI(s);
        }
        catch ( URISyntaxException ex ) {
            return null;
        }
        if ( uri.isAbsolute() ) {
            return uri;
        }
        else {
            return null;
        }
    }

    private String     myRepoDir = null;
    private boolean    myVerbose = false;
    private Repository myRepo    = null;
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
