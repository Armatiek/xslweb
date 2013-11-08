package nl.armatiek.xslweb.vfs.sax;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

public class DocTypeParser {
	
	
	public class DocType {
		String rootElement = null;
		String name = null;
		String publicId = null;
		String systemId = null;
		
		public String getRootElement() {
			return rootElement;
		}

		public String getName() {
			return name;
		}

		public String getPublicId() {
			return publicId;
		}

		public String getSystemId() {
			return systemId;
		}
	}
	
	public class ProcessingDoneException extends SAXException {
		
		private static final long serialVersionUID = -5511460500966785662L;

		public ProcessingDoneException() {
			super();
		}
	}
	

	public class DocTHandler extends DefaultHandler implements LexicalHandler {
		
		private DocType dt = new DocType();
	
		public DocType getDocType() {
			return this.dt;
		}
		
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			dt.rootElement = name;
			throw new ProcessingDoneException();
		}
	
		public void comment(char[] ch, int start, int length) throws SAXException {
		}
	
		public void endCDATA() throws SAXException {
		}
	
		public void endDTD() throws SAXException {
		}
	
		public void endEntity(String name) throws SAXException {
		}
	
		public void startCDATA() throws SAXException {
		}
	
		public void startDTD(String name, String publicId, String systemId)
				throws SAXException {
			dt.name = name;
			dt.publicId = publicId;
			dt.systemId = systemId;		
		}
	
		public void startEntity(String name) throws SAXException {	
		}
	
	}	
	
	public DocType getDocType(FileObject xmlFile) throws FileSystemException, SAXException, IOException, ParserConfigurationException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
        
        DocTHandler handler = new DocTHandler();
        
        try {
	        SAXParser parser = factory.newSAXParser();
	        parser.parse(new VfsInputSource(xmlFile), handler);
        }
        catch (ProcessingDoneException e) {
        	// expected
        }
        return handler.getDocType();

	}
	
	
	
	

}
