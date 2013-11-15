package org.expath.zip;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.expath.zip.Entry.Path;

/**
 * TODO: ...
 *
 * @author Florent Georges
 * @date   2009-12-21
 */
public class DirEntry
        extends Entry
{
    public DirEntry(URI src, Path path)
    {
        super(path.getPath(true));
        mySrc  = src;
        myPath = path;
    }

    public boolean isDir()
    {
        return true;
    }

    @Override
    protected void doSerialize(ZipOutputStream out, ZipEntry entry)
            throws ZipException, IOException
    {
        // create a dir entry, to take empty dirs into account
        out.putNextEntry(entry);
        if ( mySrc != null ) {
            // TODO: Ensure the resolution is done against the right Base URI!
            File d = new File(mySrc);
            if ( ! d.exists() ) {
                throw new ZipException("Directory does not exist: " + d);
            }
            if ( ! d.isDirectory() ) {
                throw new ZipException("Not a directory: " + d);
            }
            for ( File f : d.listFiles() ) {
                Entry e = makeEntry(myPath, mySrc, f);
                e.serialize(out);
                myPath.pop();
            }
        }
    }

    private URI mySrc;
    private Path myPath;
}
