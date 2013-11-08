package nl.armatiek.xslweb.vfs.transform;

import javax.xml.transform.dom.DOMSource;

import nl.armatiek.xslweb.vfs.VfsUtils;

import org.apache.commons.vfs2.FileName;
import org.w3c.dom.Node;

/**
 * Utility class to instantiate a DOMSource with a valid VFS system id
 * 
 * @author kleij - at - users.sourceforge.net
 * 
 */
public class VfsDomSource extends DOMSource {

	/**
	 * Constructs a DOM source from the given node and sets the sytemId based on
	 * the given VFS file object
	 * 
	 * @param node
	 * @param file
	 *            the file to base the system id on
	 */
	public VfsDomSource(Node node, FileName file) {
		super(node, VfsUtils.getSystemId(file));
	}
}
