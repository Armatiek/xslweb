package nl.armatiek.xslweb.saxon.errrorlistener;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javanet.staxutils.IndentingXMLStreamWriter;
import nl.armatiek.xslweb.configuration.Definitions;

public class ValidatorErrorHandler implements ErrorHandler {
  
  private static final Logger logger = LoggerFactory.getLogger(ValidatorErrorHandler.class);
  
  private XMLStreamWriter writer;
  private StringWriter sw;
  private String validationResults;
  private String name;
  
  public ValidatorErrorHandler(String name) {
    this.name = name;
  }
  
  protected void writeResult(SAXParseException e, String severity) throws SAXException {
    try {
      if (writer == null) {
        sw = new StringWriter(); 
        XMLOutputFactory output = XMLOutputFactory.newInstance();
        writer = new IndentingXMLStreamWriter(output.createXMLStreamWriter(sw)); 
        writer.setDefaultNamespace(Definitions.NAMESPACEURI_XSLWEB_VALIDATION);
        writer.writeStartDocument();
        writer.writeStartElement(Definitions.NAMESPACEURI_XSLWEB_VALIDATION, "validation-result");
        writer.writeNamespace("", Definitions.NAMESPACEURI_XSLWEB_VALIDATION);
      }
      writer.writeStartElement(Definitions.NAMESPACEURI_XSLWEB_VALIDATION, severity);
      writer.writeAttribute("line", Integer.toString(e.getLineNumber()));
      writer.writeAttribute("col", Integer.toString(e.getColumnNumber()));
      String systemId = e.getSystemId();
      String publicId = e.getPublicId();
      if (systemId != null) {
        writer.writeAttribute("system-id", systemId);
      }
      if (publicId != null) {
        writer.writeAttribute("public-id", publicId);
      }
      writer.writeCharacters(ExceptionUtils.getMessage(e));
      writer.writeEndElement();
    } catch (Exception ex) {
      throw new SAXException(ex);
    }
  }
  
  public void fatalError(SAXParseException e) throws SAXException { 
    logger.error("Fatal error validating XML file \"" + name + "\"", e);
    writeResult(e, "fatal");
  }

  public void error(SAXParseException e) throws SAXException {
    logger.error("Error validating XML file \"" + name + "\"", e);
    writeResult(e, "error");
  }

  public void warning(SAXParseException e) throws SAXException {
    logger.error("Warning validating XML file \"" + name + "\"", e);
    writeResult(e, "warn");
  }
  
  public Source getValidationResults() throws Exception {
    if (writer == null) {
      return null;
    }
    if (validationResults == null) {
      writer.writeEndElement();
      writer.writeEndDocument();
      validationResults = sw.toString();
    }
    return new StreamSource(new StringReader(validationResults));
  }
  
}