package org.expath.zip;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.expath.zip.Entry.Path;

/**
 * ...
 *
 * @author Florent Georges
 * @date   2009-12-21
 */
public class FileEntry
        extends Entry
{
    public FileEntry(Serialization serial, Boolean compress, URI src, Path path, Element entry)
    {
        super(path.getPath(false));
        boolean c = compress == null ? true : compress;
        myCompress = c ? ZipEntry.DEFLATED : ZipEntry.STORED;
        mySerial = serial;
        mySrc = src;
        myElem = entry;
    }

    public boolean isDir()
    {
        return false;
    }

    @Override
    protected void doSerialize(ZipOutputStream out, ZipEntry entry)
            throws ZipException, IOException
    {
        // the compression method
        entry.setMethod(myCompress);
        // for stored (uncompressed) entries, we need to set the size and CRC,
        // so wee need to buffer the output
        OutputStream first_out = out;
        ByteArrayOutputStream byte_out = null;
        if ( myCompress == ZipEntry.STORED ) {
            byte_out = new ByteArrayOutputStream();
            first_out = byte_out;
        }
        else {
            out.putNextEntry(entry);
        }
        // if no @src
        if ( mySrc == null ) {
            myElem.serialize(first_out, mySerial);
        }
        // if @src
        else {
            // TODO: For now, support only local files.  Should be extended to
            // support other schemes as well (at least HTTP and HTTPS.)
            File f = new File(mySrc);
            InputStream in = new FileInputStream(f);
            byte[] buf = new byte[4096];
            int l = -1;
            while ( (l = in.read(buf)) > 0 ) {
                first_out.write(buf, 0, l);
            }
            in.close();
        }
        // if we buffered the output, let's actually write it
        if ( byte_out != null ) {
            byte[] bytes = byte_out.toByteArray();
            // set the size
            entry.setSize(bytes.length);
            entry.setCompressedSize(bytes.length);
            // compute the CRC 32
            CRC32 crc = new CRC32();
            crc.update(bytes);
            entry.setCrc(crc.getValue());
            // actually write to the final destination
            out.putNextEntry(entry);
            out.write(bytes);
        }
    }

    private int myCompress;
    private Serialization mySerial;
    private URI mySrc;
    private Element myElem;
}
