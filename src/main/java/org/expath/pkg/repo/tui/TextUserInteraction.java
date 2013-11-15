/****************************************************************************/
/*  File:       TextUserInteraction.java                                    */
/*  Author:     F. Georges                                                  */
/*  Company:    H2O Consulting                                              */
/*  Date:       2009-11-08                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009, 2010 Florent Georges (see end of file.)         */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo.tui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.UserInteractionStrategy;

/**
 * Interaction strategy for the Text User Interface.
 *
 * TODO: See what has been done for pkg-saxon...
 *
 * @author Florent Georges
 * @date   2009-11-08
 */
public class TextUserInteraction
        implements UserInteractionStrategy
{
    public TextUserInteraction(boolean verbose)
    {
        myVerbose = verbose;
        myInput   = new BufferedReader(new InputStreamReader(System.in));
    }

    // TODO: ...
    @Override
    public void messageInfo(String msg)
            throws PackageException
    {
        System.err.println("info: " + msg);
    }

    // TODO: ...
    @Override
    public void messageError(String msg)
            throws PackageException
    {
        System.err.println("ERROR: " + msg);
    }

    // TODO: ...
    @Override
    public void logInfo(String msg)
            throws PackageException
    {
        if ( myVerbose ) {
            System.err.println("log: " + msg);
        }
    }

    // TODO: Do not ask for true/false, but for [yYnN].
    @Override
    public boolean ask(String prompt, boolean dflt)
            throws PackageException
    {
        System.out.print(prompt + " [" + dflt + "]: ");
        String val;
        try {
            val = myInput.readLine();
        }
        catch ( IOException ex ) {
            throw new PackageException("Error reading stdin", ex);
        }
        return val == null || val.isEmpty() ? dflt : Boolean.getBoolean(val);
    }

    @Override
    public String ask(String prompt, String dflt)
            throws PackageException
    {
        System.out.print(prompt + " [" + dflt + "]: ");
        String val;
        try {
            val = myInput.readLine();
        }
        catch ( IOException ex ) {
            throw new PackageException("Error reading stdin", ex);
        }
        return val == null || val.isEmpty() ? dflt : val;
    }

    private boolean myVerbose;
    private BufferedReader myInput;
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
