package nl.armatiek.xslweb.utils;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import nl.armatiek.xslweb.error.XSLWebException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * <p>
 * Helper class containing several XML/DOM/JAXP related helper methods.  
 * </p>
 * 
 * @author Maarten Kroon
 */
public class XMLUtils {
  
  public static DocumentBuilder getDocumentBuilder(boolean validate, 
      boolean namespaceAware, boolean xincludeAware) throws XSLWebException {
    try {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance(); 
      if (xincludeAware) {
        docFactory.setFeature("http://apache.org/xml/features/xinclude", true);
        docFactory.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);
        docFactory.setFeature("http://apache.org/xml/features/xinclude/fixup-language", false);
        docFactory.setXIncludeAware(true);
      }
      docFactory.setNamespaceAware(namespaceAware);
      docFactory.setValidating(validate);
      docFactory.setExpandEntityReferences(true);
      if (validate) {
        docFactory.setFeature("http://apache.org/xml/features/validation/dynamic", true);
        docFactory.setFeature("http://apache.org/xml/features/validation/schema", true);
      }
      docFactory.setIgnoringElementContentWhitespace(true);
      return docFactory.newDocumentBuilder();
    } catch (Exception e) {
      throw new XSLWebException(e);
    }
  }
  
  public static DocumentBuilder getDocumentBuilder(boolean validate, boolean namespaceAware) throws XSLWebException {
    return getDocumentBuilder(validate, namespaceAware, true);
  }
  
  public static Document stringToDocument(String xml) throws Exception {
    DocumentBuilder builder = getDocumentBuilder(false, true);
    return builder.parse(new InputSource(new StringReader(xml)));
  }
  
  public static Document inputStreamToDocument(InputStream is) throws Exception {
    if (is == null) {
      return null;
    }
    DocumentBuilder builder = getDocumentBuilder(false, true);
    return builder.parse(is);
  }
  
  public static Document readerToDocument(Reader reader, String systemId) throws Exception {
    if (reader == null) {
      return null;
    }
    DocumentBuilder builder = getDocumentBuilder(false, true);
    InputSource is = new InputSource(reader);
    if (systemId != null) {
      is.setSystemId(systemId);
    }
    return builder.parse(is);
  }
  
  public static Document readerToDocument(Reader reader) throws Exception {
    return readerToDocument(reader, null);
  }
  
  public static Element createElemWithText(Document ownerDoc, String tagName, String text) {
    Element newElem = ownerDoc.createElement(tagName);
    newElem.appendChild(ownerDoc.createTextNode(text));
    return newElem;
  }
  
  public static Element createElemWithCDATA(Document ownerDoc, String tagName, String cdata) {
    Element newElem = ownerDoc.createElement(tagName);
    newElem.appendChild(ownerDoc.createCDATASection(cdata));
    return newElem;
  }
  
  public static String nodeToString(Node node) throws Exception {
    if (node == null) {
      return null;
    }
    TransformerFactory factory = TransformerFactory.newInstance();
    Transformer transformer = factory.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.INDENT, "no");
    DOMSource source = new DOMSource(node);
    StringWriter sw = new StringWriter();
    StreamResult result = new StreamResult(sw);
    transformer.transform(source, result);
    return sw.toString();
  }
  
  public static void nodeToWriter(Node node, Writer writer) throws Exception {
    TransformerFactory factory = TransformerFactory.newInstance();
    Transformer transformer = factory.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.INDENT, "no");
    DOMSource source = new DOMSource(node);
    StreamResult result = new StreamResult(writer);
    transformer.transform(source, result);
  }
  
  public static Document getEmptyDOM() throws XSLWebException {
    DocumentBuilder builder = getDocumentBuilder(false, true);
    return builder.newDocument();
  }
  
  public static Document getRootDOM(String elemName) throws XSLWebException {
    DocumentBuilder builder = getDocumentBuilder(false, true);
    Document doc = builder.newDocument();
    doc.appendChild(doc.createElement(elemName));
    return doc;
  }
  
  public static void emptyNode(Node node) {
    Node childNode;
    while ((childNode = node.getFirstChild()) != null) {
      childNode.getParentNode().removeChild(childNode);
    }
  }
  
  public static String getDateTimeString(Date dateTime) { 
    Calendar cal = Calendar.getInstance();
    if (dateTime != null) {
      cal.setTime(dateTime);
    } 
    return DatatypeConverter.printDateTime(cal);   
  }
  
  public static String getDateTimeString() {
    return getDateTimeString(new Date());           
  }
  
  public static String xmlEncode(String value) {
    if (value == null) {
      return "";
    }
    return StringEscapeUtils.escapeXml(value);
  }
  
  protected static void getTextFromNode(Node node, StringBuffer buffer, boolean addSpace) {
    switch (node.getNodeType()) {
      case Node.CDATA_SECTION_NODE: 
      case Node.TEXT_NODE:
        buffer.append(node.getNodeValue());
        if (addSpace)
          buffer.append(" ");
    }
    Node child = node.getFirstChild();
    while (child != null) {
      getTextFromNode(child, buffer, addSpace);
      child = child.getNextSibling();
    }
  }
  
  public static String getTextFromNode(Node node, String defaultValue) {
    if (node == null) {
      return defaultValue;
    }
    if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
      return ((Attr)node).getValue();
    } else if (node.getNodeType() == Node.TEXT_NODE) {
      return node.getNodeValue();
    } else {
      StringBuffer text = new StringBuffer();
      getTextFromNode(node, text, true);
      return text.toString().trim();
    }
  }

  public static String getTextFromNode(Node node) {
    return getTextFromNode(node, null);
  }
  
  
  public static String getLocalName(Node node) {
    if (node.getPrefix() == null)
      return node.getNodeName();
    else
      return node.getLocalName();
  }
  
  public static Element getChildElementByLocalName(Element parentElem, String localName) {
    Node child = parentElem.getFirstChild();
    while (child != null) {
      if ((child.getNodeType() == Node.ELEMENT_NODE) && getLocalName(child).equals(localName)) {
        return (Element) child;
      }
      child = child.getNextSibling();
    }
    return null;
  }
    
  public static Element getFirstChildElement(Element parentElem) {
    if (parentElem == null) {
      return null;
    }
    Node child = parentElem.getFirstChild();
    while (child != null) {
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        return (Element) child;
      }
      child = child.getNextSibling();
    }
    return null;
  }
 
  public static String getValueOfChildElementByLocalName(Element parentElem, String localName) {
    Element childElem = getChildElementByLocalName(parentElem, localName);
    return (childElem != null) ? getTextFromNode(childElem) : null;
  }
  
  public static Node getPreviousSiblingElement(Node node) {
    if (node == null)
      return null;
    Node prevSibling = node.getPreviousSibling();
    while ((prevSibling != null) && (prevSibling.getNodeType() != Node.ELEMENT_NODE))
      prevSibling = prevSibling.getPreviousSibling();
    if ((prevSibling != null) && (prevSibling.getNodeType() == Node.ELEMENT_NODE))
      return prevSibling;
    return null;
  }
  
  public static Element getNextSiblingElement(Node node) {
    if (node == null)
      return null;
    Node nextSibling = node.getNextSibling();
    while ((nextSibling != null) && (nextSibling.getNodeType() != Node.ELEMENT_NODE))
      nextSibling = nextSibling.getNextSibling();
    if ((nextSibling != null) && (nextSibling.getNodeType() == Node.ELEMENT_NODE))
      return (Element) nextSibling;
    return null;
  }
  
  public static boolean getBooleanValue(String value, boolean defaultValue) {
    if (value == null) {
      return defaultValue;
    }
    return (value.equals("true") || value.equals("1"));
  }
  
  public static boolean isPunctuation(char c) {
    return '-' == c
            || '.' == c
            || ':' == c
            || '\u00B7' == c
            || '\u0387' == c
            || '-' == c
            || '\u06DD' == c
            || '\u06DE' == c;
  }
  
  public static NamespaceContext getNamespaceContext(final String prefix, final String uri) {
    return new NamespaceContext() {
      @Override
      public String getNamespaceURI(String prefix) {      
        return uri;
      }

      @Override
      public String getPrefix(String uri) {
        return prefix;
      }

      @Override
      public Iterator<String> getPrefixes(String uri) {        
         ArrayList<String> prefixes = new ArrayList<String>();
         prefixes.add(prefix);
         return prefixes.iterator();
      }      
    };    
  }
  
  public static Object getObject(String type, String value) {
    String t = StringUtils.substringAfter(type, ":");
    if (t.equals("string")) {
      return value;
    } else if (t.equals("boolean")) {
      return new Boolean(DatatypeConverter.parseBoolean(value));
    } else if (t.equals("byte")) {
      return new Byte(DatatypeConverter.parseByte(value));
    } else if (t.equals("date")) {
      return DatatypeConverter.parseDate(value);
    } else if (t.equals("dateTime")) {
      return DatatypeConverter.parseDateTime(value);
    } else if (t.equals("decimal")) {
      return DatatypeConverter.parseDecimal(value);
    } else if (t.equals("float")) {
      return DatatypeConverter.parseFloat(value);
    } else if (t.equals("double")) {
      return DatatypeConverter.parseDouble(value);
    } else if (t.equals("int")) {
      return new Integer(DatatypeConverter.parseInt(value));
    } else if (t.equals("integer")) {
      return DatatypeConverter.parseInteger(value);
    } else if (t.equals("long")) {
      return new Long(DatatypeConverter.parseLong(value));
    } else if (t.equals("short")) {
      return new Short(DatatypeConverter.parseShort(value));
    } else if (t.equals("time")) {
      return DatatypeConverter.parseTime(value);
    } else if (t.equals("unsignedInt")) {
      return new Long(DatatypeConverter.parseUnsignedInt(value));
    } else if (t.equals("unsignedShort")) {
      return new Integer(DatatypeConverter.parseUnsignedShort(value));      
    } else {
      throw new XSLWebException(String.format("Datatype \"%s\" not supported", type));
    }    
  }
}