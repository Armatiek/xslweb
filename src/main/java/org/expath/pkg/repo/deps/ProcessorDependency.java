/****************************************************************************/
/*  File:       ProcessorDependency.java                                    */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-01-27                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo.deps;

/**
 * A dependence to a processor.
 * 
 * @author Florent Georges
 * @date   2011-27-27
 */
public class ProcessorDependency
{
    public ProcessorDependency(String proc)
    {
        myProc = proc;
    }

    public String getProcessor()
    {
        return myProc;
    }

    public String getVersions()
    {
        return myVersions;
    }

    public void setVersions(String s)
    {
        myVersions = s;
    }

    public String getSemver()
    {
        return mySemver;
    }

    public void setSemver(String s)
    {
        mySemver = s;
    }

    public String getSemverMin()
    {
        return myMin;
    }

    public void setSemverMin(String s)
    {
        myMin = s;
    }

    public String getSemverMax()
    {
        return myMax;
    }

    public void setSemverMax(String s)
    {
        myMax = s;
    }

    private String myProc;
    private String myVersions;
    private String mySemver;
    private String myMin;
    private String myMax;
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
