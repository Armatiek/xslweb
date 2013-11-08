package nl.armatiek.xslweb.vfs.sax;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class CollectingErrorHandler implements ErrorHandler {
	
	private List errors = new ArrayList();
	private List fatalErrors = new ArrayList();
	private List warnings = new ArrayList();

	public void error(SAXParseException arg0) throws SAXException {
		this.errors.add(arg0);
	}

	public void fatalError(SAXParseException arg0) throws SAXException {
		this.fatalErrors.add(arg0);
	}

	public void warning(SAXParseException arg0) throws SAXException {
		this.warnings.add(arg0);
	}

	public List getErrors() {
		return this.errors;
	}
	
	public List getFatalErrors() {
		return this.fatalErrors;
	}
	
	public List getWarnings() {
		return this.warnings;
	}
	
	public boolean hasErrors() {
		return this.errors.size()>0;
	}
	
	public boolean hasFatalErrors() {
		return this.fatalErrors.size()>0;
	}

	public boolean hasWarnings() {
		return this.warnings.size()>0;
	}
}
