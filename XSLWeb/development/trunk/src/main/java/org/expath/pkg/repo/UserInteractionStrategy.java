/****************************************************************************/
/*  File:       UserInteractionStrategy.java                                */
/*  Author:     F. Georges                                                  */
/*  Company:    H2O Consulting                                              */
/*  Date:       2009-06-17                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo;

/**
 * Define the way the application can interact with the user.
 *
 * Each concrete application will create a concrete implementation.  For
 * instance, the text command line application will implement a text menu-based
 * strategy, while the graphical GUI will use dialog boxes and such.
 *
 * @author Florent Georges
 * @date   2009-06-17
 */
public interface UserInteractionStrategy
{
    /**
     * A message to display to the user, "info" level.
     *
     * @param msg
     *      The message to display.
     */
    public void messageInfo(String msg)
            throws PackageException;

    /**
     * A message to display to the user, "error" level.
     *
     * @param msg
     *      The message to display.
     */
    public void messageError(String msg)
            throws PackageException;

    /**
     * A log message, "info" level.
     *
     * @param msg
     *      The log message.
     */
    public void logInfo(String msg)
            throws PackageException;

    /**
     * Ask something to the user (the response must be a {@code boolean}.)
     *
     * @param prompt
     *      The prompt to display to the user (to ask the question.)
     *
     * @param dflt
     *      The default value to propose.
     *
     * @return
     *      The value the user has given.
     */
    public boolean ask(String prompt, boolean dflt)
            throws PackageException;

    /**
     * Ask something to the user (the response must be a {@link String}.)
     *
     * @param prompt
     *      The prompt to display to the user (to ask the question.)
     *
     * @param dflt
     *      The default value to propose.
     *
     * @return
     *      The value the user has given.
     */
    public String ask(String prompt, String dflt)
            throws PackageException;
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
