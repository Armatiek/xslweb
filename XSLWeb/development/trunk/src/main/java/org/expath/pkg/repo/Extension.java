/****************************************************************************/
/*  File:       Extension.java                                              */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-09-18                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo;

import java.util.Map;

/**
 * TODO: ...
 *
 * @author Florent Georges
 * @date   2010-09-18
 */
public abstract class Extension
{
    public Extension(String name)
    {
        myName = name;
    }

    public String getName()
    {
        return myName;
    }

    public void init(Repository repo, Map<String, Packages> packages)
            throws PackageException
    {
        for ( Packages pp : packages.values() ) {
            for ( Package pkg : pp.packages() ) {
                init(repo, pkg);
            }
        }
    }

    public abstract void init(Repository repo, Package pkg)
            throws PackageException;

    private String myName;
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
