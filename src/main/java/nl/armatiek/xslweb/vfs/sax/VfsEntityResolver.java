package nl.armatiek.xslweb.vfs.sax;

import java.io.IOException;

import nl.armatiek.xslweb.vfs.VfsResolver;
import nl.armatiek.xslweb.vfs.VfsUtils;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class VfsEntityResolver implements EntityResolver {

	private VfsResolver vfsResolver;
	
	public VfsEntityResolver(VfsResolver vfsResolver) {
		this.vfsResolver = vfsResolver;
	}

	/**
	 * Allow the application to resolve external entities.
	 * 
	 * The Parser will call this method before opening any external entity
	 * except the top-level document entity (including the external DTD subset,
	 * external entities referenced within the DTD, and external entities
	 * referenced within the document element): the application may request that
	 * the parser resolve the entity itself, that it use an alternative URI, or
	 * that it use an entirely different input source.
	 * 
	 * Application writers can use this method to redirect external system
	 * identifiers to secure and/or local URIs, to look up public identifiers in
	 * a catalogue, or to read an entity from a database or other input source
	 * (including, for example, a dialog box).
	 * 
	 * If the system identifier is a URL, the SAX parser must resolve it fully
	 * before reporting it to the application.
	 * 
	 * @param publicId
	 *            The public identifier of the external entity being referenced,
	 *            or null if none was supplied.
	 * @param systemId
	 *            The system identifier of the external entity being referenced.
	 * @return An InputSource object describing the new input source, or null to
	 *         request that the parser open a regular URI connection to the
	 *         system identifier.
	 * @throws SAXException
	 *             Any SAX exception, possibly wrapping another exception.
	 * @throws IOException
	 *             A Java-specific IO exception, possibly the result of creating
	 *             a new InputStream or Reader for the InputSource.
	 */
	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {

		try {
			FileObject vfsSource = null;

			try {
				vfsSource = vfsResolver.resolveFile(systemId);
			} catch (FileSystemException e) {
				// systemId could not be resolved, return null
				//for debugging
				int  x = 3;
			}

			if (vfsSource != null && !vfsSource.exists()) {
				throw new IOException("Could not resolve file "
						+ VfsUtils.toString(vfsSource));
			} else if (vfsSource == null) {
				return null;
			} else {
				return new VfsInputSource(vfsSource);
			}

		} catch (FileSystemException e) {
			throw new SAXException(e);
		}
	}

	
}
