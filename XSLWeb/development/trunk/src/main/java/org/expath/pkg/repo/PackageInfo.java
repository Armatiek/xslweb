/****************************************************************************/
/*  File:       PackageInfo.java                                            */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-09-19                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo;

import javax.xml.transform.stream.StreamSource;

/**
 * TODO: ...
 *
 * @author Florent Georges
 * @date   2010-09-19
 */
public abstract class PackageInfo
{
    public PackageInfo(String name, Package pkg)
    {
        myName = name;
        myPkg = pkg;
    }

    public String getName()
    {
        return myName;
    }

    public Package getPackage()
    {
        return myPkg;
    }

    public abstract StreamSource resolve(String href, URISpace space)
            throws PackageException;

    private String myName;
    private Package myPkg;
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
