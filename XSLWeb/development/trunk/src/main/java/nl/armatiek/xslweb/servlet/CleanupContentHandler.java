package nl.armatiek.xslweb.servlet;

import java.io.IOException;
import java.util.Stack;

import javax.xml.transform.sax.TransformerHandler;

import net.sf.saxon.trans.XPathException;
import nl.armatiek.xslweb.configuration.Definitions;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public class CleanupContentHandler implements ContentHandler, LexicalHandler {
  
  private TransformerHandler handler;
  private Stack<String> uriPrefixes = new Stack<String>(); 
  
  public CleanupContentHandler(TransformerHandler handler) throws IOException, XPathException {
    this.handler = handler;                          
  }
  
  @Override
  public void characters(char[] ch, int start, int len) throws SAXException {    
    handler.characters(ch, start, len);    
  }

  @Override
  public void endDocument() throws SAXException {    
    handler.endDocument();    
  }
  
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {        
    handler.endElement(uri, localName, qName);    
  }

  @Override
  public void endPrefixMapping(String prefix) throws SAXException {    
    if (!uriPrefixes.isEmpty()) {
      if (uriPrefixes.peek().equals(prefix)) {
        handler.endPrefixMapping(prefix);
        uriPrefixes.pop();
      }
    }        
  }

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {    
    handler.ignorableWhitespace(ch, start, length);    
  }

  @Override
  public void processingInstruction(String target, String data) throws SAXException {    
    handler.processingInstruction(target, data);    
  }

  @Override
  public void setDocumentLocator(Locator locator) {    
    handler.setDocumentLocator(locator);    
  }

  @Override
  public void skippedEntity(String name) throws SAXException {    
    handler.skippedEntity(name);    
  }

  @Override
  public void startDocument() throws SAXException {    
    handler.startDocument();    
  }
  
  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {    
    handler.startElement(uri, localName, qName, atts);    
  }
  
  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    if (!uri.startsWith(Definitions.NAMESPACEURI_XSLWEB)) {
      handler.startPrefixMapping(prefix, uri);
      uriPrefixes.push(prefix);
    }            
  }
  
  @Override
  public void comment(char[] ch, int start, int length) throws SAXException {
    handler.comment(ch, start, length);    
  }

  @Override
  public void endCDATA() throws SAXException {
    handler.endCDATA();    
  }

  @Override
  public void endDTD() throws SAXException {
    handler.endDTD();    
  }

  @Override
  public void endEntity(String name) throws SAXException {
    handler.endEntity(name);    
  }

  @Override
  public void startCDATA() throws SAXException {
    handler.startCDATA();    
  }

  @Override
  public void startDTD(String name, String publicId, String systemId) throws SAXException {
    handler.startDTD(name, publicId, systemId);    
  }

  @Override
  public void startEntity(String name) throws SAXException {
    handler.startEntity(name);    
  }
  
}