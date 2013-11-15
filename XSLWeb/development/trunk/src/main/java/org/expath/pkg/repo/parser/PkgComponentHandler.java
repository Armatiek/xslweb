/****************************************************************************/
/*  File:       PkgComponentHandler.java                                    */
/*  Author:     F. Georges                                                  */
/*  Company:    H2O Consulting                                              */
/*  Date:       2009-11-04                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo.parser;

import javax.xml.stream.XMLStreamReader;
import org.expath.pkg.repo.Package;
import org.expath.pkg.repo.PackageException;

/**
 * Handle an element when parsing a package descriptor.
 *
 * @author Florent Georges
 * @date   2009-11-04
 */
public interface PkgComponentHandler
{
    /**
     * Handle an element in the package descriptor.
     * 
     * This handler has to have been registered for the element it handles in
     * the {@link Repository}.
     */
    public void handleDescription(XMLStreamReader parser, Package pkg, XMLStreamHelper helper)
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
