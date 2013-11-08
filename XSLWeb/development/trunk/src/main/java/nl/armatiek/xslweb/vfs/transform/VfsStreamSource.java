package nl.armatiek.xslweb.vfs.transform;

import java.io.InputStream;
import java.io.Reader;

import javax.xml.transform.stream.StreamSource;

import nl.armatiek.xslweb.vfs.VfsUtils;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

public class VfsStreamSource extends StreamSource {

	/**
	 * Creates a Stream based source using the given FileObject. The systemId
	 * will be set to the URI of the source.
	 * 
	 * @param source
	 * @throws FileSystemException
	 */
	public VfsStreamSource(FileObject source) throws FileSystemException {
		this(source.getContent().getInputStream(), source.getName());
	}

	/**
	 * Creates a stream based source. The systemId is user specified. Use this
	 * when you want to set a different context for the URI resolving.
	 * 
	 * @param inputStream
	 * @param base
	 *            the name the system id is based on
	 */
	public VfsStreamSource(InputStream inputStream, FileName base) {
		super(inputStream, VfsUtils.getSystemId(base));
	}

	/**
	 * Creates a reader based source. The systemId is user specified. Use this
	 * when you want to set a different context for the URI resolving.
	 * 
	 * @param reader
	 * @param base
	 *            the name the system id is based on
	 */
	public VfsStreamSource(Reader reader, FileName base) {
		super(reader, VfsUtils.getSystemId(base));
	}

}
