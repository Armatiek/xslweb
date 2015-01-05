package nl.armatiek.xslweb.pipeline;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
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
  private boolean cache;
  private String cacheKey;
  private int cacheTimeToLive = 60;
  private int cacheTimeToIdle = 60;
  private String cacheScope;
  private SerializingContentHandler serializingHandler;
  private OutputStream os;
  private StringBuilder chars = new StringBuilder();
  private Processor processor;
  private Configuration conf;
  private Parameter parameter;
  
  public PipelineHandler(Processor processor, Configuration conf) {
    this.processor = processor;
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
          pipelineSteps.add(new ResponseStep(response, "response", false));
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
          boolean log = getAttribute(atts, "log", "false").equals("true");
          pipelineSteps.add(new TransformerStep(xslPath, name, log));
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
              processor,
              getAttribute(atts, "uri", null),
              name,
              getAttribute(atts, "type", "xs:string"));                
          step.addParameter(this.parameter);          
        } else if (localName.equals("pipeline")) {
          cache = getAttribute(atts, "cache", "false").equals("true");
          if (cache) {
            cacheKey = getAttribute(atts, "cache-key", null);          
            cacheTimeToLive = Integer.parseInt(getAttribute(atts, "cache-time-to-live", "60"));
            cacheTimeToIdle = Integer.parseInt(getAttribute(atts, "cache-time-to-idle", "60"));
            cacheScope = getAttribute(atts, "cache-scope", "webapp");
          }
        } else if (localName.equals("json-serializer")) {                    
          String name = getAttribute(atts, "name", "json-serializer-" + Integer.toString(pipelineSteps.size()+1));
          boolean log = getAttribute(atts, "log", "false").equals("true");          
          JSONSerializerStep step = new JSONSerializerStep(name, log);
          pipelineSteps.add(step);
          
          /*
          SystemTransformerStep step = new SystemTransformerStep("system/json/xml-to-json.xsl", name, log);
          pipelineSteps.add(step);          
          String debug = getAttribute(atts, "debug", null);
          String useRabbitfish = getAttribute(atts, "use-rabbitfish", null);
          String useBadgerfish = getAttribute(atts, "use-badgerfish", null);
          String useNamespaces = getAttribute(atts, "use-namespaces", null);
          String useRayfish = getAttribute(atts, "use-rayfish", null);
          String jsonP = getAttribute(atts, "jsonp", null);
          String skipRoot = getAttribute(atts, "skip-root", null);
          if (debug != null)
            step.addParameter(new Parameter(processor, null, "debug", "xs:boolean", debug)); 
          if (useRabbitfish != null)
            step.addParameter(new Parameter(processor, null, "use-rabbitfish", "xs:boolean", useRabbitfish));
          if (useBadgerfish != null)
            step.addParameter(new Parameter(processor, null, "use-badgerfish", "xs:boolean", debug));
          if (useNamespaces != null)
            step.addParameter(new Parameter(processor, null, "use-namespaces", "xs:boolean", debug));
          if (useRayfish != null)
            step.addParameter(new Parameter(processor, null, "use-rayfish", "xs:boolean", debug));
          if (jsonP != null)
            step.addParameter(new Parameter(processor, null, "jsonp", "xs:string", debug));
          if (skipRoot != null)
            step.addParameter(new Parameter(processor, null, "skip-root", "xs:boolean", debug));
          */                             
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
  
  public boolean getCache() {
    return cache;
  }
  
  public String getCacheKey() {
    return cacheKey;
  }
  
  public int getCacheTimeToLive() {
    return cacheTimeToLive;
  }
  
  public int getCacheTimeToIdle() {
    return cacheTimeToIdle;
  }
  
  public String getCacheScope() {
    return cacheScope;
  }
  
  private String getAttribute(Attributes attr, String name, String defaultValue) {
    int index = -1;
    return ((index = attr.getIndex(name)) >= 0) ? attr.getValue(index) : defaultValue;
  }
  
}