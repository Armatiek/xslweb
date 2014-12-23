package nl.armatiek.xslweb.servlet;

import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import nl.armatiek.xslweb.configuration.Definitions;

public class ResponseDeserializer implements XMLStreamWriter {
      
  private XMLStreamWriter mainWriter;
  private XMLStreamWriter currentWriter;
  private StringWriter tempXml;
  private HttpServletResponse resp;
  private String localName;
  private StringBuilder text = new StringBuilder();
  private String name;
  private String type;
  private int depth;

  protected ResponseDeserializer(XMLStreamWriter out, HttpServletResponse resp) {    
    this.mainWriter = out;
    this.currentWriter = this.mainWriter;
    this.resp = resp;
  }

  @Override
  public void writeStartElement(String localName) throws XMLStreamException {
    depth++;  
    if (depth > 0) {
      currentWriter.writeStartElement(localName);
    }
  }

  @Override
  public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
    if (!namespaceURI.equals(Definitions.NAMESPACEURI_XSLWEB_RESPONSE)) {
      depth++;
    } else {
      if (localName.equals("headers") || localName.equals("session") || localName.equals("cookies")) {         
      }            
      this.localName = localName;
    }
    if (depth > 0) {
      currentWriter.writeStartElement(namespaceURI, localName);
    }
  }

  @Override
  public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
    if (!namespaceURI.equals(Definitions.NAMESPACEURI_XSLWEB_RESPONSE)) {
      depth++;
    } else {
      this.localName = localName;
    }
    if (depth > 0) {
      currentWriter.writeStartElement(prefix, localName, namespaceURI);
    }
  }

  @Override
  public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {    
    if (!namespaceURI.equals(Definitions.NAMESPACEURI_XSLWEB_RESPONSE)) {
      depth++;
    } else {
      this.localName = localName;
    }
    if (depth > 0) {
      currentWriter.writeEmptyElement(namespaceURI, localName);    
    }
  }

  @Override
  public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
    if (!namespaceURI.equals(Definitions.NAMESPACEURI_XSLWEB_RESPONSE)) {
      depth++;
    } else {
      this.localName = localName;
    }
    if (depth > 0) {
      currentWriter.writeEmptyElement(prefix, localName, namespaceURI);
    }
  }

  @Override
  public void writeEmptyElement(String localName) throws XMLStreamException {
    depth++;  
    if (depth > 0) {
      currentWriter.writeEmptyElement(localName);
    }    
  }

  @Override
  public void writeEndElement() throws XMLStreamException {            
    if (depth > 0) {
      depth--;
    }
    if (depth > 0) {
      currentWriter.writeEndElement();
    } else {
      if (localName.equals("header")) {
        this.resp.setHeader(this.name, this.text.toString());
      } else if (localName.equals("item")) {
      }
      this.text.setLength(0);
    }
  }

  @Override
  public void writeEndDocument() throws XMLStreamException {
    currentWriter.writeEndDocument();    
  }

  @Override
  public void close() throws XMLStreamException {
    currentWriter.close();    
  }

  @Override
  public void flush() throws XMLStreamException {
    currentWriter.flush();    
  }

  @Override
  public void writeAttribute(String localName, String value) throws XMLStreamException {
    if (depth > 0) {
      currentWriter.writeAttribute(localName, value);
    } else if (this.localName.equals("response") && localName.equals("status")) {
      resp.setStatus(Integer.parseInt(value)); 
    } else if (this.localName.equals("header") && localName.equals("name")) {
      this.name = value;
    } else if (this.localName.equals("attribute") && localName.equals("name")) {
      this.name = value;  
    } else if (this.localName.equals("item") && localName.equals("type")) {
      this.type = value;  
    }  
  }

  @Override
  public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
    if (depth > 0) {
      currentWriter.writeAttribute(prefix, namespaceURI, localName, value);
    } else if (this.localName.equals("response") && localName.equals("status")) {
      resp.setStatus(Integer.parseInt(value)); 
    } else if (this.localName.equals("header") && localName.equals("name")) {
      this.name = value;
    } else if (this.localName.equals("attribute") && localName.equals("name")) {
      this.name = value;  
    } else if (this.localName.equals("item") && localName.equals("type")) {
      this.type = value;  
    }
  }

  @Override
  public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
    if (depth > 0) {
      currentWriter.writeAttribute(namespaceURI, localName, value);
    } else if (this.localName.equals("response") && localName.equals("status")) {
      resp.setStatus(Integer.parseInt(value)); 
    } else if (this.localName.equals("header") && localName.equals("name")) {
      this.name = value;
    } else if (this.localName.equals("attribute") && localName.equals("name")) {
      this.name = value;  
    } else if (this.localName.equals("item") && localName.equals("type")) {
      this.type = value;  
    }
  }

  @Override
  public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
    if (!namespaceURI.startsWith(Definitions.NAMESPACEURI_XSLWEB)) {
      currentWriter.writeNamespace(prefix, namespaceURI);
    }    
  }

  @Override
  public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
    //if (depth > 0) {
      currentWriter.writeDefaultNamespace(namespaceURI);
    //}    
  }

  @Override
  public void writeComment(String data) throws XMLStreamException {
    if (depth > 0) {
      currentWriter.writeComment(data);
    }    
  }

  @Override
  public void writeProcessingInstruction(String target) throws XMLStreamException {
    if (depth > 0) {
      currentWriter.writeProcessingInstruction(target);
    }    
  }

  @Override
  public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
    if (depth > 0) {
      currentWriter.writeProcessingInstruction(target, data);
    }    
  }

  @Override
  public void writeCData(String data) throws XMLStreamException {
    if (depth > 0) {
      currentWriter.writeCData(data);
    } else {
      this.text.append(data);
    }
  }

  @Override
  public void writeDTD(String dtd) throws XMLStreamException {
    currentWriter.writeDTD(dtd);    
  }

  @Override
  public void writeEntityRef(String name) throws XMLStreamException {
    if (depth > 0) {
      currentWriter.writeEntityRef(name);
    } else {
      this.text.append('&');
      this.text.append(name);
      this.text.append(';');
    }
  }

  @Override
  public void writeStartDocument() throws XMLStreamException {
    currentWriter.writeStartDocument();    
  }

  @Override
  public void writeStartDocument(String version) throws XMLStreamException {
    currentWriter.writeStartDocument(version);    
  }

  @Override
  public void writeStartDocument(String encoding, String version) throws XMLStreamException {
    currentWriter.writeStartDocument(encoding, version);    
  }

  @Override
  public void writeCharacters(String text) throws XMLStreamException {
    if (depth > 0) {
      currentWriter.writeCharacters(text);
    }    
  }

  @Override
  public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
    if (depth > 0) {
      currentWriter.writeCharacters(text, start, len); 
    } else {
      this.text.append(text, start, len);
    }
  }

  @Override
  public String getPrefix(String uri) throws XMLStreamException {
    return currentWriter.getPrefix(uri);
  }

  @Override
  public void setPrefix(String prefix, String uri) throws XMLStreamException {
    currentWriter.setPrefix(prefix, uri);    
  }

  @Override
  public void setDefaultNamespace(String uri) throws XMLStreamException {
    currentWriter.setDefaultNamespace(uri);    
  }

  @Override
  public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
    currentWriter.setNamespaceContext(context);    
  }

  @Override
  public NamespaceContext getNamespaceContext() {
    return currentWriter.getNamespaceContext();
  }

  @Override
  public Object getProperty(String name) throws IllegalArgumentException {
    return currentWriter.getProperty(name);
  }
    
  private XMLStreamWriter getTempWriter() throws XMLStreamException {    
    tempXml = new StringWriter();    
    XMLOutputFactory output = XMLOutputFactory.newInstance();    
    return output.createXMLStreamWriter(tempXml);    
  }

}