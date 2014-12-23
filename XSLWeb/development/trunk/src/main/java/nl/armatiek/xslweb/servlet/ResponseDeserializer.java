package nl.armatiek.xslweb.servlet;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import nl.armatiek.xslweb.configuration.Definitions;

public class ResponseDeserializer implements XMLStreamWriter {
      
  private XMLStreamWriter out;
  private HttpServletResponse resp;
  private String localName;  
  private String name;
  private String type;
  private int depth;

  protected ResponseDeserializer(XMLStreamWriter out, HttpServletResponse resp) {    
    this.out = out;
    this.resp = resp;
  }

  @Override
  public void writeStartElement(String localName) throws XMLStreamException {
    depth++;  
    if (depth > 0) {
      out.writeStartElement(localName);
    }
  }

  @Override
  public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
    if (!namespaceURI.equals(Definitions.NAMESPACEURI_XSLWEB_RESPONSE)) {
      depth++;
    } else {
      this.localName = localName;
    }
    if (depth > 0) {
      out.writeStartElement(namespaceURI, localName);
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
      out.writeStartElement(prefix, localName, namespaceURI);
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
      out.writeEmptyElement(namespaceURI, localName);    
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
      out.writeEmptyElement(prefix, localName, namespaceURI);
    }
  }

  @Override
  public void writeEmptyElement(String localName) throws XMLStreamException {
    depth++;  
    if (depth > 0) {
      out.writeEmptyElement(localName);
    }    
  }

  @Override
  public void writeEndElement() throws XMLStreamException {
    if (depth > 0) {
      depth--;
    }
    if (depth > 0) {
      out.writeEndElement();
    }     
  }

  @Override
  public void writeEndDocument() throws XMLStreamException {
    out.writeEndDocument();    
  }

  @Override
  public void close() throws XMLStreamException {
    out.close();    
  }

  @Override
  public void flush() throws XMLStreamException {
    out.flush();    
  }

  @Override
  public void writeAttribute(String localName, String value) throws XMLStreamException {
    if (depth > 0) {
      out.writeAttribute(localName, value);
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
      out.writeAttribute(prefix, namespaceURI, localName, value);
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
      out.writeAttribute(namespaceURI, localName, value);
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
      out.writeNamespace(prefix, namespaceURI);
    }    
  }

  @Override
  public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
    //if (depth > 0) {
      out.writeDefaultNamespace(namespaceURI);
    //}    
  }

  @Override
  public void writeComment(String data) throws XMLStreamException {
    if (depth > 0) {
      out.writeComment(data);
    }    
  }

  @Override
  public void writeProcessingInstruction(String target) throws XMLStreamException {
    if (depth > 0) {
      out.writeProcessingInstruction(target);
    }    
  }

  @Override
  public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
    if (depth > 0) {
      out.writeProcessingInstruction(target, data);
    }    
  }

  @Override
  public void writeCData(String data) throws XMLStreamException {
    if (depth > 0) {
      out.writeCData(data);
    }    
  }

  @Override
  public void writeDTD(String dtd) throws XMLStreamException {
    out.writeDTD(dtd);    
  }

  @Override
  public void writeEntityRef(String name) throws XMLStreamException {
    if (depth > 0) {
      out.writeEntityRef(name);
    }    
  }

  @Override
  public void writeStartDocument() throws XMLStreamException {
    out.writeStartDocument();    
  }

  @Override
  public void writeStartDocument(String version) throws XMLStreamException {
    out.writeStartDocument(version);    
  }

  @Override
  public void writeStartDocument(String encoding, String version) throws XMLStreamException {
    out.writeStartDocument(encoding, version);    
  }

  @Override
  public void writeCharacters(String text) throws XMLStreamException {
    if (depth > 0) {
      out.writeCharacters(text);
    }    
  }

  @Override
  public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
    if (depth > 0) {
      out.writeCharacters(text, start, len);
    }    
  }

  @Override
  public String getPrefix(String uri) throws XMLStreamException {
    return out.getPrefix(uri);
  }

  @Override
  public void setPrefix(String prefix, String uri) throws XMLStreamException {
    out.setPrefix(prefix, uri);    
  }

  @Override
  public void setDefaultNamespace(String uri) throws XMLStreamException {
    out.setDefaultNamespace(uri);    
  }

  @Override
  public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
    out.setNamespaceContext(context);    
  }

  @Override
  public NamespaceContext getNamespaceContext() {
    return out.getNamespaceContext();
  }

  @Override
  public Object getProperty(String name) throws IllegalArgumentException {
    return out.getProperty(name);
  }

}