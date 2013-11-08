package nl.armatiek.xslweb.vfs.sax;

import java.io.InputStream;
import java.io.Reader;

import nl.armatiek.xslweb.vfs.VfsUtils;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.xml.sax.InputSource;

public class VfsInputSource extends InputSource {

	public VfsInputSource(FileObject file) throws FileSystemException {
		this(file.getContent().getInputStream(), file.getName());
	}

	public VfsInputSource(InputStream inputStream, FileName base) {
		super(inputStream);
		setSystemId(VfsUtils.getSystemId(base));
	}

	public VfsInputSource(Reader reader, FileName base) {
		super(reader);
		setSystemId(VfsUtils.getSystemId(base));
	}

}
