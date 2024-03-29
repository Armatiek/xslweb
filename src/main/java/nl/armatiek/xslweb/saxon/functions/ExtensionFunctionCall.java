/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.armatiek.xslweb.saxon.functions;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Node;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrMore;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.BigIntegerValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.ExternalObject;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Attribute;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.saxon.utils.SerializableNodeInfo;
import nl.armatiek.xslweb.utils.Closeable;

/**
 * @author Maarten Kroon
 *
 */
public abstract class ExtensionFunctionCall extends net.sf.saxon.lib.ExtensionFunctionCall {

  // http://en.wikipedia.org/wiki/XQuery_API_for_Java
  protected AtomicValue convertJavaObjectToAtomicValue(Object value) throws XPathException {
    try {
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
      } else if (value instanceof Date) { // includes java.sql.Date, java.sql.Time and java.sql.Timestamp
        Calendar calendar = new GregorianCalendar();
        calendar.setTime((Date) value);
        atomicValue = new DateTimeValue(calendar, true);
      } else if (value instanceof Byte) {
        atomicValue = new Int64Value(((Byte) value).byteValue());
      } else if (value instanceof Short) {
        atomicValue = new Int64Value(((Short) value).shortValue());
      } else if (value instanceof BigDecimal) {
        atomicValue = new BigDecimalValue((BigDecimal) value);
      } else if (value instanceof BigInteger) {
        atomicValue = new BigIntegerValue((BigInteger) value);      
      } else if (value instanceof byte[]) {      
        atomicValue = new Base64BinaryValue((byte[]) value);               
      } else if (value instanceof java.sql.Clob) {
        atomicValue = new StringValue(IOUtils.toString(((java.sql.Clob) value).getCharacterStream()));            
      } else if (value instanceof java.sql.Blob) {
        atomicValue = new Base64BinaryValue(IOUtils.toByteArray(((java.sql.Blob) value).getBinaryStream()));
      } else if (value instanceof java.sql.SQLXML) {
        atomicValue = new StringValue(IOUtils.toString(((java.sql.SQLXML) value).getCharacterStream())); // TODO: node() instead of atomic?
      } else if (value instanceof InputStream) {
        atomicValue = new StringValue(IOUtils.toString((InputStream) value, "UTF-8")); // TODO: get encoding from somewhere
      } else if (value instanceof Reader) {
        atomicValue = new StringValue(IOUtils.toString((Reader) value));      
      } else {
        throw new XPathException("Java class not supported converting Java object to AtomicValue (" + value.getClass().toString() + ")");
      }    
      return atomicValue;
    } catch (XPathException xpe) {
      throw xpe;
    } catch (Exception e) {
      throw new XPathException("Error converting Java object to AtomicValue", e);
    }    
  }
  
  protected NodeInfo unwrapNodeInfo(NodeInfo nodeInfo) {
    if (nodeInfo != null && nodeInfo.getNodeKind() == Type.DOCUMENT) {
      nodeInfo = nodeInfo.iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT).next();
    }
    return nodeInfo;
  }
  
  protected void serialize(NodeInfo nodeInfo, OutputStream os, Properties outputProperties, Processor proc) throws XPathException {
    try {
      Serializer ser = proc.newSerializer(os);
      if (outputProperties != null) {
        ser.setOutputProperties(outputProperties);
      }
      ser.serializeNode(new XdmNode(nodeInfo));  
    } catch (Exception e) {
      throw new XPathException(e);
    }
  }
  
  protected void serialize(NodeInfo nodeInfo, Writer w, Properties outputProperties, Processor proc) throws XPathException {
    try {
      Serializer ser = proc.newSerializer(w);
      if (outputProperties != null) {
        ser.setOutputProperties(outputProperties);
      }
      ser.serializeNode(new XdmNode(nodeInfo));  
    } catch (Exception e) {
      throw new XPathException(e);
    }
  }
  
  protected void serialize(Sequence seq, Writer w, Properties outputProperties, Processor proc) throws XPathException {
    try {
      SequenceIterator iter = seq.iterate(); 
      Item item;
      while ((item = iter.next()) != null) {
        if (item instanceof NodeInfo) {
          serialize((NodeInfo) item, w, outputProperties, proc);
        } else {
          w.append(item.getStringValue());
        }
      }
    } catch (Exception e) {
      throw new XPathException(e);
    }
  }
  
  protected void serialize(Sequence seq, OutputStream os, Properties outputProperties, Processor proc) throws XPathException {
    String encoding = "UTF-8";
    if (outputProperties != null) {
      encoding = outputProperties.getProperty("encoding", encoding);
    }
    try {
      SequenceIterator iter = seq.iterate(); 
      Item item;
      while ((item = iter.next()) != null) {
        if (item instanceof NodeInfo) {
          serialize((NodeInfo) item, os, outputProperties, proc);
        } else {
          new OutputStreamWriter(os, encoding).append(item.getStringValue());
        }
      }
    } catch (Exception e) {
      throw new XPathException(e);
    }
  }

  protected String serialize(NodeInfo nodeInfo, Properties props, Processor proc) throws XPathException {
    StringWriter sw = new StringWriter();
    serialize(nodeInfo, sw, props, proc);
    return sw.toString();
  }
  
  protected String serialize(NodeInfo nodeInfo, Processor proc) throws XPathException {
    Properties props = new Properties();
    props.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    props.setProperty(OutputKeys.METHOD, "xml");
    props.setProperty(OutputKeys.INDENT, "no");
    return serialize(nodeInfo, props, proc);
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
    
  protected ArrayList<Attribute> sequenceToAttributeCollection(Sequence seq, XPathContext context, 
      boolean mustSupportSerialization) throws XPathException {
    ArrayList<Attribute> attrs = new ArrayList<Attribute>();
    Item item;
    SequenceIterator iter = seq.iterate();
    while ((item = iter.next()) != null) {                
      Object value;
      String type;
      if (item instanceof NodeInfo) {
        if (mustSupportSerialization) {
          WebApp webApp = getWebApp(context);
          value = new SerializableNodeInfo((NodeInfo) item, webApp);  
        } else {
          value = (NodeInfo) item;
        }
        type = "node()";
      } else if (mustSupportSerialization && (item instanceof HashTrieMap || item instanceof Function || item instanceof ExternalObject)) {
        throw new XPathException("Cannot cache maps, functions or external objects");
      } else {                             
        value = SequenceTool.convertToJava(item);
        ItemType itemType = Type.getItemType(item, null);
        type = itemType.toString();
      }
      attrs.add(new Attribute(value, type));
    }
    return attrs;
  }
  
  protected ZeroOrMore<Item> attributeCollectionToSequence(ArrayList<Attribute> attrs, XPathContext context) throws Exception {                    
    ArrayList<Item> results = new ArrayList<Item>();
    if (attrs != null) {
      for (Attribute attr : attrs) {
        Object value = attr.getValue();      
        if (value instanceof NodeInfo) {             
          results.add((NodeInfo) value);        
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
  
  protected HttpSession getSession(XPathContext context, boolean create) {    
    return getRequest(context).getSession(create);
  }
  
  protected WebApp getWebApp(XPathContext context) {
    return (WebApp) ((ObjectValue<?>)context.getController().getParameter(
        new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_WEBAPP, "webapp"))).getObject();
  }
  
  @SuppressWarnings("unchecked")
  protected void addCloseable(Closeable closeable, XPathContext context) {
    HttpServletRequest request = getRequest(context);    
    List<Closeable> closeables = (List<Closeable>) request.getAttribute("xslweb-closeables");
    if (closeables == null) {
      closeables = new ArrayList<Closeable>();
      request.setAttribute("xslweb-closeables", closeables);
    }
    closeables.add(closeable);    
  }
  
}