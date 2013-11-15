/****************************************************************************/
/*  File:       DescriptorExtension.java                                    */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-09-18                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo;

import java.io.InputStream;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import org.expath.pkg.repo.parser.DescriptorParser;

/**
 * An extension that is based on a dedicated descriptor (e.g. saxon.xml or exist.xml).
 *
 * @author Florent Georges
 * @date   2010-09-18
 */
public abstract class DescriptorExtension
        extends Extension
{
    public DescriptorExtension(String extension_name, String descriptor_name)
    {
        super(extension_name);
        myDescriptorName = descriptor_name;
    }

    @Override
    public void init(Repository repo, Package pkg)
            throws PackageException
    {
        StreamSource desc;
        try {
            desc = pkg.getResolver().resolveResource(myDescriptorName);
        }
        catch ( Storage.NotExistException ex ) {
            // nothing
            return;
        }
        // parse the pkg descriptor
        InputStream desc_in = desc.getInputStream();
        XMLStreamReader parser = DescriptorParser.XS_HELPER.makeDescriptorParser(desc_in);
        // go to the module element
        DescriptorParser.XS_HELPER.ensureDocument(parser);
        // actually parse the descriptor (in the derived class)
        parseDescriptor(parser, pkg);
        // TODO: ensure end document, terminate parsing, etc...
    }

    protected abstract void parseDescriptor(XMLStreamReader parser, Package pkg)
            throws PackageException;

    protected String myDescriptorName;
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
