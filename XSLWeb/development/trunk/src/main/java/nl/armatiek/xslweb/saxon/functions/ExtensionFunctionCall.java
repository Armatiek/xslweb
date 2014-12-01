package nl.armatiek.xslweb.saxon.functions;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Value;
import net.sf.saxon.xpath.XPathEvaluator;
import nl.armatiek.xslweb.configuration.Attribute;

/**
 * @author Maarten Kroon
 *
 */
public abstract class ExtensionFunctionCall extends net.sf.saxon.lib.ExtensionFunctionCall {

  private static final long serialVersionUID = 1L;

  protected SequenceIterator<?> convertJavaObjectSequenceIterator(Object value) throws XPathException {
    if (value == null) {
      return EmptyIterator.getInstance();
    }
    return SingletonIterator.makeIterator(convertJavaObjectToAtomicValue(value));        
  }
  
  protected AtomicValue convertJavaObjectToAtomicValue(Object value) throws XPathException {
    AtomicValue atomicValue;
    if (value == null) {
      return null;
    } else if (value instanceof String) {
      atomicValue = new StringValue((String) value);
    } else if (value instanceof Boolean) {
      atomicValue = BooleanValue.get(((Boolean) value).booleanValue());
    } else if (value instanceof Integer) {
      atomicValue = new Int64Value(((Integer) value).intValue());
    } else if (value instanceof Long) {
      atomicValue = new Int64Value(((Long) value).longValue());
    } else if (value instanceof Double) {
      atomicValue = new DoubleValue(((Double) value).doubleValue());
    } else if (value instanceof Float) {
      atomicValue = new FloatValue(((Float) value).floatValue());
    } else if (value instanceof Date) {
      Calendar calendar = new GregorianCalendar();
      calendar.setTime((Date) value);
      atomicValue = new DateTimeValue(calendar, true);
    } else {
      throw new XPathException("Class of attribute not supported (" + value.getClass().toString() + ")");
    }    
    return atomicValue;
  }

  protected String serialize(NodeInfo nodeInfo, Properties props) throws XPathException {
    Configuration config = nodeInfo.getConfiguration();
    StringWriter sw = new StringWriter();    
    Receiver serializer = config.getSerializerFactory().getReceiver(new StreamResult(sw), 
        config.makePipelineConfiguration(), props);
    nodeInfo.copy(serializer, NodeInfo.ALL_NAMESPACES, 0);
    return sw.toString();
  }
  
  protected String serialize(NodeInfo nodeInfo) throws XPathException {
    Properties props = new Properties();
    props.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    props.setProperty(OutputKeys.METHOD, "xml");
    props.setProperty(OutputKeys.INDENT, "no");
    return serialize(nodeInfo, props);
  }
  
  @SuppressWarnings("rawtypes")
  protected Collection<Attribute> sequenceToAttributeCollection(SequenceIterator iter) throws XPathException {
    ArrayList<Attribute> attrs = new ArrayList<Attribute>();
    Item item;
    while ((item = iter.next()) != null) {                
      Object value;
      String type;
      boolean isSerialized;
      if (item instanceof NodeInfo) {
        value = serialize((NodeInfo) item);
        type = "node()";
        isSerialized = true;
      } else {                             
        value = Value.convertToJava(item);
        ItemType itemType = Type.getItemType(item, null);
        type = itemType.toString();
        isSerialized = false;
      }
      attrs.add(new Attribute(value, type, isSerialized));
    }
    return attrs;
  }
  
  @SuppressWarnings("rawtypes")
  protected SequenceIterator<Item> attributeCollectionToSequence(Collection<Attribute> attrs, XPathContext context) throws Exception {    
    if (attrs == null) {
      return EmptyIterator.emptyIterator();
    }       
    ArrayList<Item> results = new ArrayList<Item>();
    XPathEvaluator evaluator = new XPathEvaluator(context.getConfiguration());
    for (Attribute attr : attrs) {
      Object value = attr.getValue();      
      if (value instanceof Node) {                              
        results.add(evaluator.setSource(new DOMSource((Node) value)));                                        
      } else {          
        results.add(convertJavaObjectToAtomicValue(value));          
      }        
    }
    return new ListIterator<Item>(results);
  }
  
}