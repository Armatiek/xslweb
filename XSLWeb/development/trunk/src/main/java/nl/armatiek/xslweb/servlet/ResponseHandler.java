package nl.armatiek.xslweb.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;

import nl.armatiek.xslweb.configuration.Definitions;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.megginson.sax.XMLWriter;


public class ResponseHandler implements ContentHandler {
  
  private HttpServletResponse response;  
  private Properties outputProperties;
  private XMLWriter writer;
  private boolean inBody;
  
  public ResponseHandler(HttpServletResponse response, OutputStream os, Properties outputProperties) throws IOException {
    this.response = response;    
    this.outputProperties = outputProperties;
    this.writer = new XMLWriter(new OutputStreamWriter(os));
    this.inBody = false;
  }
  
  @Override
  public void characters(char[] ch, int start, int len) throws SAXException {
    if (inBody) {
      writer.characters(ch, start, len);      
    }
  }

  @Override
  public void endDocument() throws SAXException {
    if (inBody) {
      writer.endDocument();
    }
  }
  
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {    
    if (inBody && StringUtils.equals(uri, Definitions.NAMESPACEURI_XSLWEB_RESPONSE) && localName.equals("body")) {
      this.inBody = false;
      writer.endDocument();
    } else {    
      if (inBody) {
        writer.endElement(uri, localName, qName);
      }
    }
  }

  @Override
  public void endPrefixMapping(String prefix) throws SAXException {    
    writer.endPrefixMapping(prefix);
  }

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    if (inBody) {
      writer.ignorableWhitespace(ch, start, length);
    }
  }

  @Override
  public void processingInstruction(String target, String data) throws SAXException {
    if (inBody) {
      writer.processingInstruction(target, data);
    }
  }

  @Override
  public void setDocumentLocator(Locator locator) {
    if (inBody) {
      writer.setDocumentLocator(locator);
    }
  }

  @Override
  public void skippedEntity(String name) throws SAXException {
    if (inBody) {
      writer.skippedEntity(name);
    }
  }

  @Override
  public void startDocument() throws SAXException {
    if (inBody) {
      writer.startDocument();
    }
  }
  
  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {    
    if (!inBody && StringUtils.equals(uri, Definitions.NAMESPACEURI_XSLWEB_RESPONSE)) {
      if (localName.equals("response")) {
        int status = getIntAttribute(atts, "status", HttpServletResponse.SC_OK);
        String message = getAttribute(atts, "message", null);       
        if (StringUtils.isNotBlank(message)) {
          // response.sendError(status, message); TODO
        } else {        
          response.setStatus(status);
        }               
      } else if (localName.equals("header")) {
        String name = getAttribute(atts, "name", null);
        if (name == null) {
          throw new SAXException("Element \"header\" must have an attribute \"name\"");
        }
        String value = getAttribute(atts, "value", null);
        if (value == null) {
          throw new SAXException("Element \"header\" must have an attribute \"value\"");
        }
        response.setHeader(name, value);        
      } else if (localName.equals("body")) {
        this.inBody = true;
        String charset = getAttribute(atts, "charset", null);                
        if (StringUtils.isNotBlank(charset)) {
          response.setCharacterEncoding(charset);
        } else {
          response.setCharacterEncoding(outputProperties.getProperty(OutputKeys.ENCODING, "UTF-8"));                              
        }
        
        String contentType = getAttribute(atts, "content-type", null);
        if (StringUtils.isNotBlank(contentType)) {
          response.setContentType(contentType);
        } else {
          String mediaType = outputProperties.getProperty(OutputKeys.MEDIA_TYPE, null);
          if (StringUtils.isNotBlank(mediaType)) {
            response.setContentType(mediaType);
          } else {          
            String method = outputProperties.getProperty(OutputKeys.METHOD, "xml");
            if (method.equals("xhtml")) {
              response.setContentType("application/xhtml+xml");
            } else if (method.equals("html")) {
              response.setContentType("text/html");
            } else if (method.equals("text")) {
              response.setContentType("plain/text");
            } else {
              response.setContentType("application/xml");
            }            
          }          
        }
        writer.startDocument();        
      }
    } else if (inBody) {                     
      writer.startElement(uri, localName, qName, atts);
    }
  }

  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {    
    writer.startPrefixMapping(prefix, uri);    
  }
  
  private String getAttribute(Attributes attr, String name, String defaultValue) {
    int index = -1;
    return ((index = attr.getIndex(name)) >= 0) ? attr.getValue(index) : defaultValue;
  }
  
  private int getIntAttribute(Attributes attr, String name, int defaultValue) {
    int index = -1;
    return ((index = attr.getIndex(name)) >= 0) ? Integer.parseInt(attr.getValue(index)) : defaultValue;
  }
  
}