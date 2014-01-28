package nl.armatiek.xslweb.servlet;

import java.io.IOException;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.StreamWriterToReceiver;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.trans.XPathException;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import com.sun.xml.ws.util.xml.ContentHandlerToXMLStreamWriter;

public class DebugContentHandler implements ContentHandler, LexicalHandler {
  
  private TransformerHandler handler;    
  private ContentHandler writer;
  private StreamWriterToReceiver xsw;
  private StringBuilder cdata;
  
  public DebugContentHandler(TransformerHandler handler, LogWriter logWriter, Configuration conf, 
      Properties outputProperties) throws IOException, XPathException {
    this.handler = handler;             
    SerializerFactory sf = new SerializerFactory(conf);    
    this.xsw = sf.getXMLStreamWriter(new StreamResult(logWriter), outputProperties);    
    this.writer = new ContentHandlerToXMLStreamWriter(xsw);          
  }
  
  @Override
  public void characters(char[] ch, int start, int len) throws SAXException {    
    handler.characters(ch, start, len);
    if (cdata != null) {
      cdata.append(ch, start, len);
    } else {
      writer.characters(ch, start, len);
    }
  }

  @Override
  public void endDocument() throws SAXException {    
    handler.endDocument();
    writer.endDocument(); 
    try {
      xsw.close();     
    } catch (XMLStreamException e) {
      throw new SAXException("Could not close StreamWriterToReceiver", e);
    }
  }
  
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {        
    handler.endElement(uri, localName, qName);
    writer.endElement(uri, localName, qName);
  }

  @Override
  public void endPrefixMapping(String prefix) throws SAXException {    
    handler.endPrefixMapping(prefix);
    writer.endPrefixMapping(prefix);
  }

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {    
    handler.ignorableWhitespace(ch, start, length);
    writer.ignorableWhitespace(ch, start, length);
  }

  @Override
  public void processingInstruction(String target, String data) throws SAXException {    
    handler.processingInstruction(target, data);
    writer.processingInstruction(target, data);
  }

  @Override
  public void setDocumentLocator(Locator locator) {    
    handler.setDocumentLocator(locator);
    writer.setDocumentLocator(locator);
  }

  @Override
  public void skippedEntity(String name) throws SAXException {    
    handler.skippedEntity(name);
    writer.skippedEntity(name);
  }

  @Override
  public void startDocument() throws SAXException {    
    handler.startDocument();
    writer.startDocument();
  }
  
  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {    
    handler.startElement(uri, localName, qName, atts);
    writer.startElement(uri, localName, qName, atts);
  }

  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {    
    handler.startPrefixMapping(prefix, uri);
    writer.startPrefixMapping(prefix, uri);    
  }

  @Override
  public void comment(char[] ch, int start, int length) throws SAXException {
    handler.comment(ch, start, length);
    try {
      xsw.writeComment(new String(ch, start, length));
    } catch (XMLStreamException e) {
      throw new SAXException(e);
    }
  }

  @Override
  public void endCDATA() throws SAXException {
    handler.endCDATA();
    try {
      xsw.writeCData(cdata.toString());
    } catch (XMLStreamException e) {
      throw new SAXException(e);
    } finally {
      cdata = null;
    }
  }

  @Override
  public void endDTD() throws SAXException {
    handler.endDTD();
    // writer noop
  }

  @Override
  public void endEntity(String name) throws SAXException {
    handler.endEntity(name);
    // writer noop
  }

  @Override
  public void startCDATA() throws SAXException {
    handler.startCDATA();
    cdata = new StringBuilder();
  }

  @Override
  public void startDTD(String name, String publicId, String systemId) throws SAXException {
    handler.startDTD(name, publicId, systemId);
    try {
      if (publicId == null) {
        xsw.writeDTD(String.format("<!DOCTYPE %s SYSTEM \"%s\">", name, systemId));
      } else {
        xsw.writeDTD(String.format("<!DOCTYPE %s PUBLIC \"%s\" \"%s\">", name, publicId, systemId));
      }
    } catch (XMLStreamException e) {
      throw new SAXException(e);
    }
  }

  @Override
  public void startEntity(String name) throws SAXException {
    handler.startEntity(name);
    try {
      xsw.writeEntityRef(name);
    } catch (XMLStreamException e) {
      throw new SAXException(e);
    }
  }
  
}