/****************************************************************************/
/*  File:       ZipFacade.java                                              */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-02-21                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.zip;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import org.ccil.cowan.tagsoup.Parser;
import org.expath.zip.ZipTree.ZipNode;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The overall facade to the ZIP module.
 *
 * @author Florent Georges
 * @date   2011-02-21
 */
public class ZipFacade
{
    public ZipFacade(String base_uri)
    {
        myBaseUri = base_uri;
    }

    /**
     * Return an entry as a string.
     * 
     * Return null if the entry does not exist.
     */
    public String textEntry(String href, String path)
            throws ZipException
    {
        // TODO: ...
        String charset = "utf-8";
        try {
            ZipFile zip = new ZipFile(fileFromURI(href, myBaseUri));
            ZipEntry entry = zip.getEntry(path);
            if ( entry == null ) {
                return null;
            }
            InputStream in = zip.getInputStream(entry);
            Reader reader = new InputStreamReader(in, charset);
            StringBuilder buf = new StringBuilder();
            char[] ch = new char[4096];
            int i = -1;
            while ( (i = reader.read(ch)) > 0 ) {
                buf.append(ch, 0, i);
            }
            return buf.toString();
        }
        catch ( java.util.zip.ZipException ex ) {
            throw new ZipException("Wrong ZIP file", ex);
        }
        catch ( IOException ex ) {
            throw new ZipException("Error reading ZIP file", ex);
        }
    }

    /**
     * Return an entry as an array of bytes.
     * 
     * Return null if the entry does not exist.
     */
    public byte[] binaryEntry(String href, String path)
            throws ZipException
    {
        try {
            ZipFile zip = new ZipFile(fileFromURI(href, myBaseUri));
            ZipEntry entry = zip.getEntry(path);
            if ( entry == null ) {
                return null;
            }
            InputStream in = zip.getInputStream(entry);
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            byte[] b = new byte[4096];
            int i = -1;
            while ( (i = in.read(b)) > 0 ) {
                buf.write(b, 0, i);
            }
            return buf.toByteArray();
        }
        catch ( java.util.zip.ZipException ex ) {
            throw new ZipException("Wrong ZIP file", ex);
        }
        catch ( IOException ex ) {
            throw new ZipException("Error reading ZIP file", ex);
        }
    }

    /**
     * Return an entry as an XML document.
     * 
     * Return null if the entry does not exist.
     */
    public Source xmlEntry(String href, String path)
            throws ZipException
    {
        return mlEntry(href, path, false);
    }

    /**
     * Return an entry as an HTML document.
     * 
     * Return null if the entry does not exist.  The entry is actually returned
     * as an XML document, but HTML sanitization has been applied to the entry.
     */
    public Source htmlEntry(String href, String path)
            throws ZipException
    {
        return mlEntry(href, path, true);
    }

    private Source mlEntry(String href, String path, boolean html)
            throws ZipException
    {
        // TODO: ...
        String charset = "utf-8";
        String sys_id = "TODO-find-a-useful-systemId";
        File input = fileFromURI(href, myBaseUri);
        try {
            ZipFile zip = new ZipFile(input);
            ZipEntry entry = zip.getEntry(path);
            if ( entry == null ) {
                return null;
            }
            InputStream in = zip.getInputStream(entry);
            Reader reader = new InputStreamReader(in, charset);
            if ( html ) {
                Parser parser = new Parser();
                parser.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
                Source src = new SAXSource(parser, new InputSource(reader));
                src.setSystemId(sys_id);
                return src;
            }
            else {
                return new StreamSource(reader, sys_id);
            }
        }
        catch ( java.util.zip.ZipException ex ) {
            throw new ZipException("Wrong ZIP file: " + input.getAbsolutePath(), ex);
        }
        catch ( IOException ex ) {
            throw new ZipException("Error reading ZIP file: " + input.getAbsolutePath(), ex);
        }
        catch ( SAXException ex ) {
            throw new ZipException("Error parsing HTML entry: " + input.getAbsolutePath() + "!" + path, ex);
        }
    }

    /**
     * Output the structure of the ZIP file to the tree builder.
     */
    public void entries(String href, TreeBuilder builder)
            throws ZipException
    {
        try {
            ZipFile zip = new ZipFile(fileFromURI(href, myBaseUri));
            ZipTree tree = new ZipTree(zip);
            builder.startElement("file");
            builder.attribute("href", href);
            builder.startContent();
            for ( Map.Entry<String, ZipNode> child : tree.getRoot().getNodes().entrySet() ) {
                outputTree(child.getValue(), builder);
            }
            builder.endElement();
        }
        catch ( java.util.zip.ZipException ex ) {
            throw new ZipException("Wrong ZIP file", ex);
        }
        catch ( IOException ex ) {
            throw new ZipException("Error reading ZIP file", ex);
        }
    }

    private void outputTree(ZipNode node, TreeBuilder builder)
            throws ZipException
    {
        ZipEntry entry = node.getEntry();
        builder.startElement(node.isDirectory() ? "dir" : "entry");
        builder.attribute("name", node.getName());
        if ( entry != null ) {
            String comment = entry.getComment();
            if ( comment != null ) {
                builder.attribute("comment", comment);
            }
            if ( ! node.isDirectory() ) {
                long size = entry.getSize();
                if ( size != -1 ) {
                    builder.attribute("size", Long.toString(size));
                }
            }
            long time = entry.getTime();
            if ( time != -1 ) {
                Date d = new Date(time);
                DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                builder.attribute("time", fmt.format(d));
            }
        }
        builder.startContent();
        if ( node.getNodes() != null ) {
            for ( Map.Entry<String, ZipNode> child : node.getNodes().entrySet() ) {
                outputTree(child.getValue(), builder);
            }
        }
        builder.endElement();
    }

    /**
     * Create a new ZIP file.
     *
     * Take as parameter a zip:file element node.  If the element contains an
     * attribute {@code href}, it must be a URI with file: scheme, and the file
     * is written on this URI (and this function returns null).  If not, the
     * file is returned as a base64 item.
     */
    public byte[] zipFile(Element struct)
            throws ZipException
    {
        Output out = new Output(struct);
        ZipOutputStream zip = out.getZipStream();
        // walk the elements
        for ( Element entry : struct.entries() ) {
            zipFileHandleEntry(entry, zip, new Entry.Path());
        }
        closeStream(zip);
        return out.getBase64();
    }

    private void zipFileHandleEntry(Element entry, ZipOutputStream zip, Entry.Path path)
            throws ZipException
    {
        Entry e = Entry.makeEntry(path, entry); // make path.push()
        e.serialize(zip);
        if ( e.isDir() ) {
            // walk the elements if a directory
            for ( Element child : entry.entries() ) {
                zipFileHandleEntry(child, zip, path);
            }
        }
        path.pop();
    }

    // TODO: Allow to remove existing entries too.
    // TODO: Accept another param, the name of the output file.  Href could refer
    // to the source, and we could have a different target (so it is easy to have
    // an ODF pattern and just update a few files within it, but to another
    // destination, so the pattern file is never modified.)
    public byte[] doUpdateEntries(Element struct, String dest)
            throws ZipException
    {
        try {
            byte[] bytes = new byte[4096];
            Map<String, Entry> entries = new HashMap<String, Entry>();
            accumulateEntries(struct, entries, new Entry.Path());
            File zip_file = analyseHref(struct);
            ZipInputStream zip = openZipFileInput(zip_file);
            File out_file = dest == null ? null : fileFromURI(dest, myBaseUri);
            Output output = new Output(out_file);
            ZipOutputStream out = output.getZipStream();
            for ( ZipEntry e = zip.getNextEntry(); e != null; e = zip.getNextEntry() ) {
                Entry entry = entries.get(e.getName());
                if ( entry == null ) {
                    out.putNextEntry(new ZipEntry(e.getName()));
                    // copy from zip to out
                    int l = -1;
                    while ( (l = zip.read(bytes)) != -1 ) {
                        out.write(bytes, 0, l);
                    }
                    out.closeEntry();
                }
                else {
                    entry.serialize(out);
                    entries.remove(entry.getPath());
                }
            }
            zip.close();
            // remaining entries, to add (that were not in the ZIP initially)
            for ( Entry entry : entries.values() ) {
                entry.serialize(out);
            }
            out.close();
            return output.getBase64();
        }
        catch ( IOException ex ) {
            throw new ZipException("Error opening temporary file", ex);
        }
    }

    private void accumulateEntries(Element elem, Map<String, Entry> map, Entry.Path path)
            throws ZipException
    {
        // walk the elements
        for ( Element child : elem.entries() ) {
            Entry e = Entry.makeEntry(path, child);  // make a path.push()
            map.put(e.getPath(), e);
            // if entry is a dir, recurse
            if ( e.isDir() ) {
                accumulateEntries(child, map, path);
            }
            path.pop();
        }
    }

    private File analyseHref(Element elem)
            throws ZipException
    {
        for ( Attribute a : elem.attributes() ) {
            String local = a.getLocalName();
            if ( !"".equals(a.getNamespaceUri()) ) {
                // ignore namespace qualified attributes
            }
            else if ( "href".equals(local) ) {
                return fileFromURI(a.getValue(), elem.getBaseUri());
            }
            else {
                throw new ZipException("Unknown attribute zip:file/@" + local);
            }
        }
        throw new ZipException("required @href has not been set on zip:file");
    }

    private ZipInputStream openZipFileInput(File zip)
            throws ZipException
    {
        ZipInputStream in = null;
        try {
            in = new ZipInputStream(new FileInputStream(zip));
        }
        catch ( FileNotFoundException ex ) {
            throw new ZipException("Error opening ZIP file for reading", ex);
        }
        return in;
    }

    /**
     * Resolve {@code @href} against the base URI, if needed, and return a {@code File}.
     */
    private File fileFromURI(String uri, String base)
            throws ZipException
    {
        try {
            // parse the URI
            URI result = new URI(uri);
            // resolve against base URI if not absolute yet
            if ( ! result.isAbsolute() ) {
                if ( base == null ) {
                    throw new ZipException("FIXME: Impossible to get the base URI");
                }
                URI base_uri = new URI(base);
                result = base_uri.resolve(result);
            }
            // return it as a File object
            return new File(result);
        }
        catch ( URISyntaxException ex ) {
            throw new ZipException("Invalid URI: " + uri, ex);
        }
    }

    private ZipOutputStream openZipFileOutput(File zip)
            throws ZipException
    {
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(zip));
        }
        catch ( FileNotFoundException ex ) {
            throw new ZipException("Error opening ZIP file for writing", ex);
        }
        return out;
    }

    private void closeStream(OutputStream out)
            throws ZipException
    {
        try {
            out.close();
        }
        catch ( IOException ex ) {
            throw new ZipException("Error closing zip file", ex);
        }
    }

    /**
     * Represent an output ZIP.
     *
     * It is constructed from a zip:file element.  If it has a href attribute,
     * it must be a target file to write to.  If not, the output must be
     * returned as a base64 item.
     */
    private class Output
    {
        public Output(File f)
        {
            myFile = f;
        }

        public Output(Element zip_elem)
            throws ZipException
        {
            for ( Attribute a : zip_elem.attributes() ) {
                String local = a.getLocalName();
                if ( !"".equals(a.getNamespaceUri()) ) {
                    // ignore namespace qualified attributes
                }
                else if ( "href".equals(local) ) {
                    myFile = fileFromURI(a.getValue(), zip_elem.getBaseUri());
                }
                else {
                    throw new ZipException("Unknown attribute zip:file/@" + local);
                }
            }
        }

        public ZipOutputStream getZipStream()
                throws ZipException
        {
            if ( myZip == null ) {
                if ( myFile == null ) {
                    myBuffer = new ByteArrayOutputStream();
                    myZip = new ZipOutputStream(myBuffer);
                }
                else {
                    myZip = openZipFileOutput(myFile);
                }
            }
            return myZip;
        }

        public byte[] getBase64()
        {
            if ( myBuffer == null ) {
                return null;
            }
            else {
                return myBuffer.toByteArray();
            }
        }

        private File myFile = null;
        private ByteArrayOutputStream myBuffer = null;
        private ZipOutputStream myZip = null;
    }

    private String myBaseUri;
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
