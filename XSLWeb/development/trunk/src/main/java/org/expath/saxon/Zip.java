/****************************************************************************/
/*  File:       Zip.java                                                    */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-08-03                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.saxon;

import javax.xml.transform.Source;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Base64BinaryValue;
import org.expath.zip.Element;
import org.expath.zip.ZipException;
import org.expath.zip.ZipFacade;
import org.expath.zip.saxon.SaxonElement;
import org.expath.zip.saxon.SaxonTreeBuilder;

/**
 * EXPath ZIP module implementation for Saxon.
 *
 * @author Florent Georges
 * @date   2009-08-03
 */
public class Zip
{
    public static NodeInfo xmlEntry(XPathContext ctxt, String href, String path)
            throws ZipException
                 , XPathException
    {
        // FIXME: How to access the base URI from here?  I think this is not
        // possible, so I pass null for now...
        ZipFacade zip = new ZipFacade(null);
        Source src = zip.xmlEntry(href, path);
        return ctxt.getConfiguration().buildDocument(src);
    }

    public static NodeInfo htmlEntry(XPathContext ctxt, String href, String path)
            throws ZipException
                 , XPathException
    {
        // FIXME: How to access the base URI from here?  I think this is not
        // possible, so I pass null for now...
        ZipFacade zip = new ZipFacade(null);
        Source src = zip.htmlEntry(href, path);
        return ctxt.getConfiguration().buildDocument(src);
    }

    public static String textEntry(String href, String path)
            throws ZipException
    {
        // FIXME: How to access the base URI from here?  I think this is not
        // possible, so I pass null for now...
        ZipFacade zip = new ZipFacade(null);
        return zip.textEntry(href, path);
    }

    public static Base64BinaryValue binaryEntry(String href, String path)
            throws ZipException
    {
        // FIXME: How to access the base URI from here?  I think this is not
        // possible, so I pass null for now...
        ZipFacade zip = new ZipFacade(null);
        byte[] bytes = zip.binaryEntry(href, path);
        return new Base64BinaryValue(bytes);
    }

    public static NodeInfo entries(XPathContext ctxt, String href)
            throws ZipException
                 , XPathException
    {
        SaxonTreeBuilder builder = new SaxonTreeBuilder(ctxt);
        // FIXME: How to access the base URI from here?  I think this is not
        // possible, so I pass null for now...
        ZipFacade zip = new ZipFacade(null);
        zip.entries(href, builder);
        return builder.getRoot();
    }

    public static Base64BinaryValue zipFile(NodeInfo struct)
            throws ZipException
    {
        ZipFacade zip = new ZipFacade(null);
        Element elem = new SaxonElement(struct);
        byte[] bytes = zip.zipFile(elem);
        if ( bytes == null ) {
            return null;
        }
        else {
            return new Base64BinaryValue(bytes);
        }
    }

    public static void updateEntries(XPathContext ctxt, NodeInfo struct, String dest)
            throws ZipException
    {
        // FIXME: How to access the base URI from here?  I think this is not
        // possible, so I pass null for now...
        ZipFacade zip = new ZipFacade(null);
        Element elem = new SaxonElement(struct);
        zip.doUpdateEntries(elem, dest);
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
