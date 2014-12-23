package nl.armatiek.xslweb.servlet;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import nl.armatiek.xslweb.configuration.Definitions;

import javanet.staxutils.helpers.StreamWriterDelegate;

public class CleanupXMLStreamWriter extends StreamWriterDelegate {

  protected CleanupXMLStreamWriter(XMLStreamWriter out) {
    super(out);    
  }
  
  @Override
  public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
    if (!namespaceURI.startsWith(Definitions.NAMESPACEURI_XSLWEB)) {
      out.writeNamespace(prefix, namespaceURI);
    }    
  }
  
}