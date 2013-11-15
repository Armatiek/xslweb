/****************************************************************************/
/*  File:       PkgHandlerDtd.java                                          */
/*  Author:     F. Georges                                                  */
/*  Company:    H2O Consulting                                              */
/*  Date:       2012-05-09                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo.parser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.expath.pkg.repo.Package;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.URISpace;

/**
 * Parse a standard DTD component in the package descriptor.
 *
 * @author Florent Georges
 * @date   2012-05-09
 */
class PkgHandlerDtd
        implements PkgComponentHandler
{
    @Override
    public void handleDescription(XMLStreamReader parser, Package pkg, XMLStreamHelper helper)
            throws PackageException
    {
        String pubid = null;
        if ( helper.isNextElement(parser, "public-id") ) {
            pubid = helper.getElementValue(parser);
            helper.ensureNextElement(parser, "system-id");
        }
        else {
            helper.ensureElement(parser, "system-id");
        }
        String sysid = helper.getElementValue(parser);
        helper.ensureNextElement(parser, "file");
        String file = helper.getElementValue(parser);
        // TODO: Wat to do with the Public ID?
        pkg.addPublicUri(URISpace.DTD, sysid, file);
        try {
            // position to the component's end tag
            parser.next();
        }
        catch ( XMLStreamException ex ) {
            throw new PackageException("Error parsing the package descriptor", ex);
        }
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
