/****************************************************************************/
/*  File:       PkgModuleResolver.java                                      */
/*  Author:     F. Georges                                                  */
/*  Company:    H2O Consulting                                              */
/*  Date:       2009-10-29                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009, 2010 Florent Georges (see end of file.)         */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.saxon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.trans.XPathException;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Repository;
import org.expath.pkg.repo.URISpace;
import org.expath.pkg.repo.util.Logger;

/**
 * Implementation for EXPath Pkg of Saxon's {@link ModuleURIResolver} for XQuery.
 *
 * @author Florent Georges
 * @date   2009-10-29
 */
public class PkgModuleResolver
        implements ModuleURIResolver
{
    public PkgModuleResolver(Map<String, String> overrides, SaxonRepository repo, Repository parent)
            throws PackageException
    {
        myOverrides = overrides;
        myRepo = repo;
        myParent = parent;
    }

    @Override
    public StreamSource[] resolve(String module_uri, String base_uri, String[] locations)
            throws XPathException
    {
        LOG.fine("resolve: {0} with base: {1}", module_uri, base_uri);
        for ( String l : locations ) {
            LOG.fine("  location: {0}", l);
        }

        try {
            // try the override URIs
            String href = myOverrides.get(module_uri);
            if ( href != null ) {
                File f = new File(href);
                InputStream in;
                try {
                    in = new FileInputStream(f);
                }
                catch ( FileNotFoundException ex ) {
                    throw new PackageException("Error opening file", ex);
                }
                StreamSource[] source = new StreamSource[1];
                source[0] = new StreamSource(in);
                source[0].setSystemId(f.toURI().toString());
                return source;
            }
            // try a Saxon-specific stuff
            StreamSource s = myRepo.resolve(module_uri, URISpace.XQUERY);
            if ( s != null ) {
                return new StreamSource[]{ s };
            }
            // delegate to pkg-repo's repository
            s = myParent.resolve(module_uri, URISpace.XQUERY);
            if ( s != null ) {
                return new StreamSource[]{ s };
            }
        }
        catch ( PackageException ex ) {
            throw new XPathException("Error resolving the URI", ex);
        }
        return null;
    }

    /** The overrides (take precedence over the catalog resolver). */
    private Map<String, String> myOverrides;
    /** The Saxon repo used to resolve Saxon-specific stuff. */
    private SaxonRepository myRepo;
    /** The pkg-repo's repository, to delegate everything else to. */
    private Repository myParent;
    /** The logger. */
    private static final Logger LOG = Logger.getLogger(PkgModuleResolver.class);
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
