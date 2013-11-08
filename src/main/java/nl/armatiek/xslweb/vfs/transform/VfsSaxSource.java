package nl.armatiek.xslweb.vfs.transform;

import javax.xml.transform.sax.SAXSource;

import nl.armatiek.xslweb.vfs.sax.VfsInputSource;

import org.xml.sax.XMLReader;

/**
 * Utility class to create a SAX source with a valid VFS system id
 * 
 * @author kleij -at- users.sourceforge.net
 */
public class VfsSaxSource extends SAXSource {

	/**
	 * Constructs a SAXSource based on a VFS Input Source
	 * 
	 * @param inputSource
	 */
	public VfsSaxSource(VfsInputSource inputSource) {
		super(inputSource);
	}

	/**
	 * Constructs a SAXSource based on a VFS Input Source and a custom XMLReader
	 * 
	 * @param xmlReader
	 * @param inputSource
	 */
	public VfsSaxSource(XMLReader xmlReader, VfsInputSource inputSource) {
		super(xmlReader, inputSource);
	}

}
