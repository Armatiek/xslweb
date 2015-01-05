package nl.armatiek.xslweb.pipeline;

import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLOutputFactory;

public class JSONSerializerStep extends SerializerStep {
  
  public JSONSerializerStep(String name, boolean log) {
    super(name, log);        
  }
  
  public XMLStreamWriter getWriter(OutputStream os, String encoding) throws XMLStreamException {
    JsonXMLConfig config = new JsonXMLConfigBuilder().        
        prettyPrint(true).
        build();    
    XMLOutputFactory factory = new JsonXMLOutputFactory(config);
    return factory.createXMLStreamWriter(os, encoding);
  }
  
}