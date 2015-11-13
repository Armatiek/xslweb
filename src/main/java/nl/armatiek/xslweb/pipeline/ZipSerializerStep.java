package nl.armatiek.xslweb.pipeline;

import java.io.OutputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.Attributes;

import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.SAXDestination;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.serializer.ZipSerializer;

public class ZipSerializerStep extends SerializerStep {
  
  public ZipSerializerStep(Attributes atts) {
    super(atts);            
  }
  
  @Override
  public Destination getDestination(WebApp webApp, HttpServletResponse resp, 
      OutputStream os, Properties outputProperties) throws XMLStreamException {      
    return new SAXDestination(new ZipSerializer(webApp, resp, os));            
  }
  
}