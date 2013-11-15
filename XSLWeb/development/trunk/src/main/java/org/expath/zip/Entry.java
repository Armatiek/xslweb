package org.expath.zip;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ...
 *
 * @author Florent Georges
 * @date   2009-12-21
 */
public abstract class Entry
{
    public Entry(String path)
    {
        myPath = path;
    }

    public abstract boolean isDir();
    protected abstract void doSerialize(ZipOutputStream out, ZipEntry entry)
            throws ZipException, IOException;

    public static class Path
    {
        public void push(String step) {
            myStack.push(step);
        }
        public void pop() {
            myStack.pop();
        }
        public String getPath(boolean dir) {
            StringBuilder buf = new StringBuilder();
            for ( int i = 0, s = myStack.size(); i < s; ++i ) {
                buf.append(myStack.get(i));
                if ( i < s - 1 || dir ) {
                    buf.append('/');
                }
            }
            return buf.toString();
        }
        private Stack<String> myStack = new Stack<String>();
    }

    protected static Entry makeEntry(Path path, URI src_base, File entry)
            throws ZipException
    {
        String name = entry.getName();
        path.push(name);
        if ( ! entry.exists() ) {
            throw new ZipException("File does not exist: " + entry);
        }
        else if ( entry.isDirectory() ) {
            return new DirEntry(src_base.resolve(name + "/"), path);
        }
        else {
            return new FileEntry(null, null, src_base.resolve(name), path, null);
        }
    }

    public static Entry makeEntry(Path path, Element entry)
            throws ZipException
    {
        String node_name = entry.getLocalName();
        String name = null;
        Serialization serial = entry.makeSerialization();
        String src = null;
        Boolean compress = null;
        for ( Attribute a : entry.attributes() ) {
            String local = a.getLocalName();
            String value = a.getValue();
            if ( !"".equals(a.getNamespaceUri()) ) {
                // ignore namespace qualified attributes
            }
            else if ( "name".equals(local) ) {
                name = value;
            }
            else if ( "compress".equals(local) ) {
                String s1 = value;
                String s2 = s1.trim();
                if ( "true".equals(s2) ) {
                    compress = true;
                }
                else if ( "false".equals(s2) ) {
                    compress = false;
                }
                else {
                    throw new ZipException("Invalid compress value:" + s1);
                }
            }
            else if ( "src".equals(local) ) {
                src = value;
            }
            else {
                serial.setOutputParam(local, value);
            }
        }
        // TODO: @name can be null if there is a @src (name is initialized with
        // src basename).
        if ( name == null ) {
            if ( src == null ) {
                throw new ZipException("required @name has not been set on zip:" + node_name);
            }
            name = new File(src).getName();
        }
        path.push(name);
        if ( node_name.equals("entry") ) {
            if ( serial.getMethod() == null && src == null ) {
                throw new ZipException("required @method has not been set on zip:" + node_name);
            }
            URI src_uri = resolveSrc(src, entry);
            return new FileEntry(serial, compress, src_uri, path, entry);
        }
        else if ( ! node_name.equals("dir") ) {
            throw new ZipException("Entry is neither zip:file or zip:dir: " + entry.formatName());
        }
        else if ( serial.getMethod() != null ) {
            throw new ZipException("Unknown attribute zip:dir/@method");
        }
        else if ( compress != null ) {
            throw new ZipException("Unknown attribute zip:dir/@compress");
        }
        else if ( src == null ) {
            return new DirEntry(null, path);
        }
        else {
            URI src_uri = resolveSrc(src.endsWith("/") ? src : src + "/", entry);
            return new DirEntry(src_uri, path);
        }
    }

    private static URI resolveSrc(String src, Element entry)
            throws ZipException
    {
        if ( src == null ) {
            return null;
        }
        try {
            URI base = new URI(entry.getBaseUri());
            return base.resolve(new URI(src));
        }
        catch ( URISyntaxException ex ) {
            String msg = "zip:entry/@src is not a valid URI: " + src;
            throw new ZipException(msg, ex);
        }
    }

    public String getPath()
    {
        return myPath;
    }

    public void serialize(ZipOutputStream out)
            throws ZipException
    {
        try {
            ZipEntry e = new ZipEntry(myPath);
            doSerialize(out, e);
            out.closeEntry();
        }
        catch ( IOException ex ) {
            throw new ZipException("Error writing entry: " + myPath, ex);
        }
    }

    private String myPath;
}
