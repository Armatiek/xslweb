/****************************************************************************/
/*  File:       BatchUserInteraction.java                                   */
/*  Author:     F. Georges                                                  */
/*  Company:    H2O Consulting                                              */
/*  Date:       2010-01-05                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo.tui;

import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.UserInteractionStrategy;

/**
 * Interaction strategy for batch "interaction".  Always return default values.
 *
 * @author Florent Georges
 * @date   2010-01-05
 */
public class BatchUserInteraction
        implements UserInteractionStrategy
{
    public BatchUserInteraction()
    {
    }

    @Override
    public void messageInfo(String msg)
            throws PackageException
    {
        System.err.println("info: " + msg);
    }

    @Override
    public void messageError(String msg)
            throws PackageException
    {
        System.err.println("ERROR: " + msg);
    }

    @Override
    public void logInfo(String msg)
            throws PackageException
    {
    }

    @Override
    public boolean ask(String prompt, boolean dflt)
            throws PackageException
    {
        return dflt;
    }

    @Override
    public String ask(String prompt, String dflt)
            throws PackageException
    {
        return dflt;
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
