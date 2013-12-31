package nl.armatiek.xslweb.pipeline;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

import net.sf.saxon.Configuration;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.Parameter;
import nl.armatiek.xslweb.utils.SerializingContentHandler;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class PipelineHandler implements ContentHandler {
  
  private ArrayList<PipelineStep> pipelineSteps = new ArrayList<PipelineStep>();
  private SerializingContentHandler serializingHandler;
  private OutputStream os;
  private StringBuilder chars = new StringBuilder();
  private Configuration conf;
  private Parameter parameter;
  
  public PipelineHandler(Configuration conf) {
    this.conf = conf;
  }
    
  @Override
  public void characters(char[] ch, int start, int len) throws SAXException { 
    chars.append(ch, start, len);
    if (serializingHandler != null) {
      serializingHandler.characters(ch, start, len);
    }
  }

  @Override
  public void endDocument() throws SAXException {
    if (serializingHandler != null) {
      serializingHandler.endDocument();
    }    
  }
  
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException { 
    try {
      if (serializingHandler != null) {
        serializingHandler.endElement(uri, localName, qName);
      }    
      if (StringUtils.equals(uri, Definitions.NAMESPACEURI_XSLWEB_PIPELINE)) {
        if (localName.equals("value")) { 
          parameter.addValue(chars.toString());
        }
      } else if (StringUtils.equals(uri, Definitions.NAMESPACEURI_XSLWEB_RESPONSE)) {
        if (localName.equals("response")) {
          serializingHandler.close();
          String response = IOUtils.toString(((ByteArrayOutputStream)os).toByteArray(), "UTF-8");
          pipelineSteps.add(new ResponseStep(response, "response"));
          serializingHandler = null;                   
        }
      }
      chars.setLength(0);
    } catch (Exception e) {
      throw new SAXException(e);
    }
  }

  @Override
  public void endPrefixMapping(String prefix) throws SAXException { 
    if (serializingHandler != null) {
      serializingHandler.endPrefixMapping(prefix);
    }
  }

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException { 
    if (serializingHandler != null) {
      serializingHandler.ignorableWhitespace(ch, start, length);
    }
  }

  @Override
  public void processingInstruction(String target, String data) throws SAXException { 
    if (serializingHandler != null) {
      serializingHandler.processingInstruction(target, data);
    }
  }

  @Override
  public void setDocumentLocator(Locator locator) { 
    if (serializingHandler != null) {
      serializingHandler.setDocumentLocator(locator);
    }
  }

  @Override
  public void skippedEntity(String name) throws SAXException { 
    if (serializingHandler != null) {
      serializingHandler.skippedEntity(name);
    }
  }

  @Override
  public void startDocument() throws SAXException { 
    if (serializingHandler != null) {
      serializingHandler.startDocument();
    }
  }
  
  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {    
    if (serializingHandler != null) {
      serializingHandler.startElement(uri, localName, qName, atts);
    }    
    try {
      if (StringUtils.equals(uri, Definitions.NAMESPACEURI_XSLWEB_PIPELINE)) {
        if (localName.equals("transformer")) {         
          String xslPath = getAttribute(atts, "xsl-path", null);
          if (StringUtils.isBlank(xslPath)) {
            throw new SAXException("Transformer step must have an attribute \"xsl-path\"");
          }
          String name = getAttribute(atts, "name", "transformer-" + Integer.toString(pipelineSteps.size()+1));        
          pipelineSteps.add(new TransformerStep(xslPath, name));
        } else if (localName.equals("parameter")) {
          if (pipelineSteps.isEmpty()) {
            throw new SAXException("Element \"parameter\" not expected at this location in pipeline definition");
          }
          TransformerStep step = (TransformerStep) pipelineSteps.get(pipelineSteps.size()-1);
          String name = getAttribute(atts, "name", null);
          if (StringUtils.isBlank(name)) {
            throw new SAXException("Element \"parameter\" must have an attribute \"name\"");
          }                 
          this.parameter = new Parameter(
              getAttribute(atts, "uri", null),
              name,
              getAttribute(atts, "type", "xs:string"));                
          step.addParameter(this.parameter);          
        } else if (localName.equals("pipeline")) {
        } else if (localName.equals("value")) {
        } else {
          throw new SAXException(String.format("Pipeline element \"%s\" not supported", localName));
        }
      } else if (StringUtils.equals(uri, Definitions.NAMESPACEURI_XSLWEB_RESPONSE)) {
        if (localName.equals("response")) {
          Properties props = new Properties();
          props.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
          props.setProperty(OutputKeys.METHOD, "xml");
          props.setProperty(OutputKeys.INDENT, "no");     
          os = new ByteArrayOutputStream();
          serializingHandler = new SerializingContentHandler(os, conf, props);
          serializingHandler.startElement(uri, localName, qName, atts);
        }
      }
    } catch (Exception e) {
      throw new SAXException(e);
    }
  }

  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException { 
    if (serializingHandler != null) {
      serializingHandler.startPrefixMapping(prefix, uri);
    }
  }
  
  public List<PipelineStep> getPipelineSteps() {
    return this.pipelineSteps;
  }
  
  private String getAttribute(Attributes attr, String name, String defaultValue) {
    int index = -1;
    return ((index = attr.getIndex(name)) >= 0) ? attr.getValue(index) : defaultValue;
  }
  
}