package nl.armatiek.xslweb.saxon.functions;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrMore;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Attribute;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;

import org.w3c.dom.Node;

/**
 * @author Maarten Kroon
 *
 */
public abstract class ExtensionFunctionCall extends net.sf.saxon.lib.ExtensionFunctionCall {

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
  
  protected NodeInfo unwrapNodeInfo(NodeInfo nodeInfo) {
    if (nodeInfo != null && nodeInfo.getNodeKind() == Type.DOCUMENT) {
      nodeInfo = nodeInfo.iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT).next();
    }
    return nodeInfo;
  }
  
  protected void serialize(NodeInfo nodeInfo, Result result, Properties outputProperties) throws XPathException {
    try {
      TransformerFactory factory = new TransformerFactoryImpl();
      Transformer transformer = factory.newTransformer();
      if (outputProperties != null) {
        transformer.setOutputProperties(outputProperties);
      }
      transformer.transform(nodeInfo, result);
    } catch (Exception e) {
      throw new XPathException(e);
    }
  }
  
  protected void serialize(Sequence seq, Writer w, Properties outputProperties) throws XPathException {
    try {
      SequenceIterator iter = seq.iterate(); 
      Item item;
      while ((item = iter.next()) != null) {
        if (item instanceof NodeInfo) {
          serialize((NodeInfo) item, new StreamResult(w), outputProperties);
        } else {
          w.append(item.getStringValue());
        }
      }
    } catch (Exception e) {
      throw new XPathException(e);
    }
  }
  
  protected void serialize(Sequence seq, OutputStream os, Properties outputProperties) throws XPathException {
    String encoding = "UTF-8";
    if (outputProperties != null) {
      encoding = outputProperties.getProperty("encoding", encoding);
    }
    try {
      SequenceIterator iter = seq.iterate(); 
      Item item;
      while ((item = iter.next()) != null) {
        if (item instanceof NodeInfo) {
          serialize((NodeInfo) item, new StreamResult(os), outputProperties);
        } else {
          new OutputStreamWriter(os, encoding).append(item.getStringValue());
        }
      }
    } catch (Exception e) {
      throw new XPathException(e);
    }
  }

  protected String serialize(NodeInfo nodeInfo, Properties props) throws XPathException {
    StringWriter sw = new StringWriter();
    serialize(nodeInfo, new StreamResult(sw), props);
    return sw.toString();
  }
  
  protected String serialize(NodeInfo nodeInfo) throws XPathException {
    Properties props = new Properties();
    props.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    props.setProperty(OutputKeys.METHOD, "xml");
    props.setProperty(OutputKeys.INDENT, "no");
    return serialize(nodeInfo, props);
  }
  
  protected Properties getOutputProperties(NodeInfo paramsElem) {
    Properties props = new Properties();
    paramsElem = unwrapNodeInfo(paramsElem);
    AxisIterator iter = paramsElem.iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT);
    NodeInfo paramElem;
    while ((paramElem = iter.next()) != null) {
      props.put(paramElem.getLocalPart(), paramElem.getAttributeValue("", "value"));
    }  
    return props;
  }
    
  protected Collection<Attribute> sequenceToAttributeCollection(Sequence seq) throws XPathException {
    ArrayList<Attribute> attrs = new ArrayList<Attribute>();
    Item item;
    SequenceIterator iter = seq.iterate();
    while ((item = iter.next()) != null) {                
      Object value;
      String type;
      boolean isSerialized;
      if (item instanceof NodeInfo) {
        value = serialize((NodeInfo) item);
        type = "node()";
        isSerialized = true;
      } else {                             
        value = SequenceTool.convertToJava(item);
        ItemType itemType = Type.getItemType(item, null);
        type = itemType.toString();
        isSerialized = false;
      }
      attrs.add(new Attribute(value, type, isSerialized));
    }
    return attrs;
  }
  
  protected ZeroOrMore<Item> attributeCollectionToSequence(Collection<Attribute> attrs, XPathContext context) throws Exception {                    
    ArrayList<Item> results = new ArrayList<Item>();
    if (attrs != null) {
      for (Attribute attr : attrs) {
        Object value = attr.getValue();      
        if (value instanceof Node) {             
          results.add(source2NodeInfo(new DOMSource((Node) value), context.getConfiguration()));                                        
        } else {          
          results.add(convertJavaObjectToAtomicValue(value));          
        }        
      }
    }
    return new ZeroOrMore<Item>(results);
  }
  
  protected NodeInfo source2NodeInfo(Source source, Configuration configuration) {        
    Node node = ((DOMSource)source).getNode();
    String baseURI = source.getSystemId();
    DocumentWrapper documentWrapper = new DocumentWrapper(node.getOwnerDocument(), baseURI, configuration);
    return documentWrapper.wrap(node);            
  }
  
  protected HttpServletRequest getRequest(XPathContext context) {
    return (HttpServletRequest) ((ObjectValue<?>) context.getController().getParameter(
        new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_REQUEST, "request"))).getObject();
  }
  
  protected HttpServletResponse getResponse(XPathContext context) {
    return (HttpServletResponse) ((ObjectValue<?>) context.getController().getParameter(
        new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_RESPONSE, "response"))).getObject();
  }
  
  protected HttpSession getSession(XPathContext context) {    
    return getRequest(context).getSession();
  }
  
  protected WebApp getWebApp(XPathContext context) {
    return (WebApp) ((ObjectValue<?>)context.getController().getParameter(
        new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_WEBAPP, "webapp"))).getObject();
  }
  
}