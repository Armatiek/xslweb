/****************************************************************************/
/*  File:       PkgHandlerUriFile.java                                      */
/*  Author:     F. Georges                                                  */
/*  Company:    H2O Consulting                                              */
/*  Date:       2010-05-13                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo.parser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.expath.pkg.repo.Package;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.URISpace;

/**
 * Parse a component composed of two parts: a public URI and a file ref.
 *
 * The public URI must be in a first element child, the file ref in the second,
 * and there cannot be any other content.  The name of those elements are
 * configured in the ctor.  They are pushed in the map passed in the ctor.
 *
 * @author Florent Georges
 * @date   2010-05-13
 */
class PkgHandlerUriFile
        implements PkgComponentHandler
{
    PkgHandlerUriFile(URISpace space, String uri_elem, String file_elem)
            throws PackageException
    {
        if ( space == null ) {
            throw new PackageException("space is null");
        }
        if ( uri_elem == null ) {
            throw new PackageException("uri_elem is null");
        }
        if ( file_elem == null ) {
            throw new PackageException("file_elem is null");
        }
        mySpace = space;
        myUriElem = uri_elem;
        myFileElem = file_elem;
    }

    PkgHandlerUriFile(URISpace space, String uri_elem, String alternate_elem, String file_elem)
            throws PackageException
    {
        this(space, uri_elem, file_elem);
        if ( alternate_elem == null ) {
            throw new PackageException("alternate_elem is null");
        }
        myAlternateElem = alternate_elem;
    }

    @Override
    public void handleDescription(XMLStreamReader parser, Package pkg, XMLStreamHelper helper)
            throws PackageException
    {
        if ( myAlternateElem == null ) {
            helper.ensureNextElement(parser, myUriElem);
        }
        else {
            ensureNextElements(parser, myUriElem, myAlternateElem, helper);
        }
        String href = helper.getElementValue(parser);
        helper.ensureNextElement(parser, myFileElem);
        String file = helper.getElementValue(parser);
        pkg.addPublicUri(mySpace, href, file);
        try {
            // position to the component's end tag
            parser.next();
        }
        catch ( XMLStreamException ex ) {
            throw new PackageException("Error parsing the package descriptor", ex);
        }
    }

    private void ensureNextElements(XMLStreamReader parser, String elem1, String elem2, XMLStreamHelper helper)
            throws PackageException
    {
        if ( helper.isNextElement(parser, elem1) ) {
            return;
        }
        if ( helper.isElement(parser, elem2) ) {
            return;
        }
        throw new PackageException("The element is neither a pkg:" + elem1 + " nor a pkg:" + elem2 + ": " + parser.getName());
    }

    private URISpace mySpace;
    private String myUriElem;
    private String myAlternateElem;
    private String myFileElem;
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
