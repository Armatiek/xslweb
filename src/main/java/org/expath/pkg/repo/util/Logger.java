/****************************************************************************/
/*  File:       Logger.java                                                 */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-02-13                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo.util;

import java.util.logging.Level;

/**
 * Wrapper around the Java Logging facility.
 *
 * @author Florent Georges
 * @date   2012-02-13
 */
public class Logger
{
    private Logger(Class c)
    {
        myLogger = java.util.logging.Logger.getLogger(c.getName());
    }

    public static Logger getLogger(Class c)
    {
        return new Logger(c);
    }

    public void finer(String msg, Object... args)
    {
        myLogger.log(Level.FINER, msg, args);
    }

    public void fine(String msg, Object... args)
    {
        myLogger.log(Level.FINE, msg, args);
    }

    public void info(String msg, Object... args)
    {
        myLogger.log(Level.INFO, msg, args);
    }

    public void severe(String msg, Object... args)
    {
        myLogger.log(Level.SEVERE, msg, args);
    }

    @SuppressWarnings("NonConstantLogger")
    private java.util.logging.Logger myLogger;
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
