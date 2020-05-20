package nl.armatiek.xslweb.saxon.utils;

import java.math.BigDecimal;
import java.util.Date;

import javax.xml.XMLConstants;
import javax.xml.transform.ErrorListener;

import org.apache.commons.lang3.StringUtils;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.serialize.MessageWarner;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XsltController;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.saxon.errrorlistener.ErrorListenerMessageListener;
import nl.armatiek.xslweb.saxon.errrorlistener.MessageListenerProxy;

public class SaxonUtils {
  
  public static SequenceType getSequenceType(String lexicalType) {
    ItemType itemType;
    lexicalType = lexicalType.trim().toLowerCase();
    if (lexicalType.equals("empty-sequence()")) {
      return SequenceType.EMPTY_SEQUENCE;
    } else if (lexicalType.startsWith("item()")) {
      itemType = AnyItemType.getInstance();
    } else if (lexicalType.matches("^(node|element|document-node|attribute|comment|processing-instruction)\\(")) {
      itemType = AnyNodeTest.getInstance();
    } else if (lexicalType.startsWith("xs:") || lexicalType.startsWith("xsd:")) {
      itemType = Type.getBuiltInItemType(XMLConstants.W3C_XML_SCHEMA_NS_URI, StringUtils.substringAfter(lexicalType, ":"));
    } else {
      itemType = AnyItemType.getInstance(); // TODO: raise exception?
    }
    
    int cardinality = StaticProperty.EXACTLY_ONE; 
    if (lexicalType.endsWith("*")) {
      cardinality = StaticProperty.ALLOWS_ZERO_OR_MORE;
    } else if (lexicalType.endsWith("+")) {
      cardinality = StaticProperty.ALLOWS_ONE_OR_MORE;
    } else if (lexicalType.endsWith("?")) {
      cardinality = StaticProperty.ALLOWS_ZERO_OR_ONE;
    }
 
    return SequenceType.makeSequenceType(itemType, cardinality);
  }
  
  public static Class convertToJava(SequenceType sequenceType) throws XPathException {
    ItemType itemType = sequenceType.getPrimaryType();
    int cardinality = sequenceType.getCardinality();
    boolean isArray = (cardinality == StaticProperty.ALLOWS_ZERO_OR_MORE) || (cardinality == StaticProperty.ALLOWS_ONE_OR_MORE);
    if (itemType instanceof AnyItemType) {
      return (isArray) ? NodeInfo[].class : NodeInfo.class;
    } else if (itemType instanceof AnyNodeTest) {
      return (isArray) ? Object[].class : Object.class;
    } else {
      switch (itemType.getPrimitiveType()) {
      case StandardNames.XS_STRING:
      case StandardNames.XS_UNTYPED_ATOMIC:
      case StandardNames.XS_ANY_URI:
      case StandardNames.XS_DURATION:
        return (isArray) ? String[].class : String.class;
      case StandardNames.XS_BOOLEAN:
        return (isArray) ? Boolean[].class : Boolean.class;
      case StandardNames.XS_DECIMAL:
        return (isArray) ? BigDecimal[].class : BigDecimal.class;
      case StandardNames.XS_INTEGER:
        return (isArray) ? Long[].class : Long.class;
      case StandardNames.XS_DOUBLE:
        return (isArray) ? Double[].class : Double.class;
      case StandardNames.XS_FLOAT:
        return (isArray) ? Float[].class : Float.class;
      case StandardNames.XS_DATE_TIME:
      case StandardNames.XS_DATE:
        return (isArray) ? Date[].class : Date.class;
      case StandardNames.XS_TIME:
        return (isArray) ? String[].class : String.class;
      case StandardNames.XS_BASE64_BINARY:
      case StandardNames.XS_HEX_BINARY:
        return (isArray) ? byte[][].class : byte[].class;
      default:
        return (isArray) ? Object[].class : Object.class;
      }
    }
  }
  
  protected Object concertToJava(Item item) throws XPathException {
    return SequenceTool.convertToJava(item);
  }
  
  public static void setMessageEmitter(XsltController controller, Configuration config, ErrorListener errorListener) {
    if (config.getBooleanProperty(Feature.ALLOW_MULTITHREADING)) {
      controller.setMessageFactory(() -> new MessageListenerProxy(new ErrorListenerMessageListener(errorListener), config.makePipelineConfiguration()));  
    } else {
      controller.setMessageEmitter(new MessageWarner());
    }
  }
  
}