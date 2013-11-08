package nl.armatiek.xslweb.vfs;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;

public class VfsUtils {

	/**
	 * Utility method to print the name of a file object.
	 * Needed since the VFS FileName.toString prints passwords.
	 * @param file
	 * @return
	 */
	public static String toString(FileObject file) {
		String result = "";
		if (file!=null) {
			result = toString(file.getName());
		}
		return result;
	}
	
	/**
	 * Utility method to print a file name
	 * @param filename
	 * @return
	 */
	public static String toString(FileName filename) {
		String result = "";
		if (filename!=null) {
			String nameString = filename.getFriendlyURI();
			//strip the password hiding (:*****)
			result = nameString.replaceAll(":\\*+", "");
		}
		return result;
	}
	
	/**
	 * Create a valid VFS system id for resolvers used in XML processing 
	 * @param file
	 * @return a system id that can be interpreted by the VFS
	 */
	public static String getSystemId(FileObject file) {
		if (file==null) {
			return null;
		}
		return getSystemId(file.getName());
	}
	
	/**
	 * Create a valid VFS system id for resolvers used in XML processing
	 * @param name
	 * @return a system id that can be interpreted by the VFS
	 */
	public static String getSystemId(FileName name) {
		if (name==null) {
			return null;
		}
		return name.getURI();
	}
}
