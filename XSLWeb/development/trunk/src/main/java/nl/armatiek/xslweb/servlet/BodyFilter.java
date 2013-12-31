package nl.armatiek.xslweb.servlet;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

public class BodyFilter extends XMLFilterImpl {
  
  @Override
  public void characters(char[] ch, int start, int len) throws SAXException {    
    super.characters(ch, start, len);
  }

  @Override
  public void endDocument() throws SAXException { }
  
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {        
    super.endElement(uri, localName, qName);
  }

  @Override
  public void endPrefixMapping(String prefix) throws SAXException {    
    super.endPrefixMapping(prefix);
  }

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {    
    super.ignorableWhitespace(ch, start, length);
  }

  @Override
  public void processingInstruction(String target, String data) throws SAXException { }

  @Override
  public void setDocumentLocator(Locator locator) { 
    super.setDocumentLocator(locator);
  }

  @Override
  public void skippedEntity(String name) throws SAXException {    
    super.skippedEntity(name);
  }

  @Override
  public void startDocument() throws SAXException { }
  
  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {    
    super.startElement(uri, localName, qName, atts);
  }

  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {    
    super.startPrefixMapping(prefix, uri);    
  }

}
