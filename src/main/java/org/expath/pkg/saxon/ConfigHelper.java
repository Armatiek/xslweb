/****************************************************************************/
/*  File:       ConfigHelper.java                                           */
/*  Author:     F. Georges                                                  */
/*  Company:    H2O Consulting                                              */
/*  Date:       2009-07-28                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009, 2010 Florent Georges (see end of file.)         */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.saxon;

import javax.xml.transform.URIResolver;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.lib.ModuleURIResolver;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.URISpace;

/**
 * ...
 *
 * @author Florent Georges
 * @date   2009-07-28
 */
public class ConfigHelper
{
    public ConfigHelper(SaxonRepository repo)
    {
        myRepo = repo;
    }

    public void config(Configuration config)
            throws PackageException
    {
        // resolver for XQuery
        ModuleURIResolver xquery_resolver = myRepo.getModuleURIResolver();
        config.setModuleURIResolver(xquery_resolver);
        // resolver for XSLT
        URIResolver xslt_resolver = myRepo.getURIResolver(URISpace.XSLT);
        config.setURIResolver(xslt_resolver);
        // the extension functions
        myRepo.registerExtensionFunctions(config);
    }

    public void config(Controller control)
            throws PackageException
    {
        control.setURIResolver(myRepo.getURIResolver(URISpace.XSLT));
    }

    /** The repo. */
    private SaxonRepository myRepo;
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
