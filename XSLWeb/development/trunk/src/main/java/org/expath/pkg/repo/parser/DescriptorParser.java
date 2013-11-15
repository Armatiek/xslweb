/****************************************************************************/
/*  File:       DescriptorParser.java                                       */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-10-07                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo.parser;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.expath.pkg.repo.Package;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Storage;
import org.expath.pkg.repo.Repository;
import org.expath.pkg.repo.URISpace;

/**
 * Parser for package descriptors.
 *
 * @author Florent Georges
 * @date   2010-10-07
 */
public class DescriptorParser
{
    /**
     * Parse a package descriptor and build the corresponding {@link Package} object.
     */
    public Package parse(InputStream desc, String rsrc_name, Storage storage, Repository repo)
            throws PackageException
    {
        // parse the pkg descriptor
        XMLStreamReader parser = XS_HELPER.makeDescriptorParser(desc);
        // go to the package element
        XS_HELPER.ensureDocument(parser);
        XS_HELPER.ensureNextElement(parser, "package");
        // check the spec version
        String spec = XS_HELPER.getAttributeValue(parser, "spec");
        if ( ! "1.0".equals(spec) ) {
            throw new PackageException("Spec version is not 1.0: '" + spec + "'");
        }
        // get the package attributes
        String name    = XS_HELPER.getAttributeValue(parser, "name");
        String abbrev  = XS_HELPER.getAttributeValue(parser, "abbrev");
        String version = XS_HELPER.getAttributeValue(parser, "version");
        // TODO: Check the module "dir" exists? (in the storage object)
        XS_HELPER.ensureNextElement(parser, "title");
        String title = XS_HELPER.getElementValue(parser);
        Package pkg = null;
        try {
            // the home URI
            String home = null;
            parser.next();
            if ( XS_HELPER.isElement(parser, "home") ) {
                home = XS_HELPER.getElementValue(parser);
                parser.next();
            }
            // create the package object
            Storage.PackageResolver resolver = storage.makePackageResolver(rsrc_name, abbrev);
            pkg = new Package(repo, resolver, name, abbrev, version, title, home);
            // the dependencies
            while ( XS_HELPER.isElement(parser, "dependency") ) {
                handleDependency(parser, pkg);
                parser.next();
            }
            // the components
            while ( parser.getEventType() == XMLStreamConstants.START_ELEMENT ) {
                handleComponent(parser, pkg);
            }
        }
        catch ( XMLStreamException ex ) {
            throw new PackageException("Error parsing the package descriptor", ex);
        }
        // TODO: Here, we must be on the </package> tag.  Ensure it...
        return pkg;
    }

    /**
     * Handle a dependency element.
     */
    private void handleDependency(XMLStreamReader parser, Package pkg)
            throws PackageException
                 , XMLStreamException
    {
        String pkg_name  = parser.getAttributeValue(null, "package");
        String processor = parser.getAttributeValue(null, "processor");
        String versions  = parser.getAttributeValue(null, "versions");
        String semver    = parser.getAttributeValue(null, "semver");
        String min       = parser.getAttributeValue(null, "semver-min");
        String max       = parser.getAttributeValue(null, "semver-max");
        if ( pkg_name == null && processor == null ) {
            throw new PackageException("None of @package nor @processor are set on a dependency element");
        }
        if ( pkg_name != null && processor != null ) {
            throw new PackageException("@package and @processor are both set on a dependency element");
        }
        if ( pkg_name != null ) {
            pkg.addPackageDep(pkg_name, versions, semver, min, max);
        }
        else {
            pkg.addProcessorDep(processor, versions, semver, min, max);
        }
        parser.next();
    }

    /**
     * Handle any single parse event after pkg:dependency elements.
     */
    private void handleComponent(XMLStreamReader parser, Package pkg)
            throws PackageException
                 , XMLStreamException
    {
        if ( PKG_NS.equals(parser.getNamespaceURI()) ) {
            String local = parser.getLocalName();
            PkgComponentHandler handler = MY_HANDLERS.get(local);
            if ( handler == null ) {
                throw new PackageException("Unknown component type: " + local);
            }
            handler.handleDescription(parser, pkg, XS_HELPER);
        }
        else {
            // ignore elements not in the EXPath Pkg namespace
            XS_HELPER.ignoreElement(parser);
        }
        parser.next();
    }

    /** The static handlers map. */
    private static final Map<String, PkgComponentHandler> MY_HANDLERS;
    static {
        Map<String, PkgComponentHandler> handlers = new HashMap<String, PkgComponentHandler>();
        try {
            handlers.put("xslt",       new PkgHandlerUriFile(URISpace.XSLT,       "import-uri", "file"));
            handlers.put("xproc",      new PkgHandlerUriFile(URISpace.XPROC,      "import-uri", "file"));
            handlers.put("xsd",        new PkgHandlerUriFile(URISpace.XSD,        "import-uri", "namespace", "file"));
            handlers.put("rng",        new PkgHandlerUriFile(URISpace.RNG,        "import-uri", "file"));
            handlers.put("rnc",        new PkgHandlerUriFile(URISpace.RNC,        "import-uri", "file"));
            handlers.put("schematron", new PkgHandlerUriFile(URISpace.SCHEMATRON, "import-uri", "file"));
            handlers.put("nvdl",       new PkgHandlerUriFile(URISpace.NVDL,       "import-uri", "file"));
            handlers.put("xquery",     new PkgHandlerUriFile(URISpace.XQUERY,     "import-uri", "namespace", "file"));
            handlers.put("dtd",        new PkgHandlerDtd());
        }
        catch ( PackageException ex ) {
            throw new RuntimeException("Exception occured during static members initialization", ex);
        }
        MY_HANDLERS = handlers;
    }
    /** The namespace of the package descriptor elements. */
    public static final String PKG_NS = "http://expath.org/ns/pkg";
    /** The XML Streaming API helper object. */
    public static final XMLStreamHelper XS_HELPER = new XMLStreamHelper(PKG_NS);
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
