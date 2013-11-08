package nl.armatiek.xslweb.pipeline;

import java.util.ArrayList;
import java.util.List;

import nl.armatiek.xslweb.configuration.Definitions;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class PipelineHandler implements ContentHandler {
  
  private ArrayList<PipelineStep> pipelineSteps;
    
  @Override
  public void characters(char[] ch, int start, int len) throws SAXException { }

  @Override
  public void endDocument() throws SAXException { }
  
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException { }

  @Override
  public void endPrefixMapping(String prefix) throws SAXException { }

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException { }

  @Override
  public void processingInstruction(String target, String data) throws SAXException { }

  @Override
  public void setDocumentLocator(Locator locator) { }

  @Override
  public void skippedEntity(String name) throws SAXException { }

  @Override
  public void startDocument() throws SAXException { }
  
  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {    
    if (StringUtils.equals(uri, Definitions.NAMESPACEURI_XSLWEB_PIPELINE)) {
      if (localName.equals("transformer")) {
        String xslPath = getAttribute(atts, "xsl-path", null);
        if (StringUtils.isBlank(xslPath)) {
          throw new SAXException("Transformer step must have an attribute \"xsl-path\"");
        }        
        pipelineSteps.add(new TransformerStep(xslPath));
      } else if (localName.equals("pipeline")) {
      } else {
        throw new SAXException(String.format("Transformer step \"%s\" not supported", localName));
      }
    };            
  }

  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException { }
  
  public List<PipelineStep> getPipelineSteps() {
    return this.pipelineSteps;
  }
  
  private String getAttribute(Attributes attr, String name, String defaultValue) {
    int index = -1;
    return ((index = attr.getIndex(name)) >= 0) ? attr.getValue(index) : defaultValue;
  }
  
  /*
  private int getIntAttribute(Attributes attr, String name, int defaultValue) {
    int index = -1;
    return ((index = attr.getIndex(name)) >= 0) ? Integer.parseInt(attr.getValue(index)) : defaultValue;
  }
  */
  
}