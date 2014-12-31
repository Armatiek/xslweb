package nl.armatiek.xslweb.saxon.functions.response;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Attribute;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.error.XSLWebException;
import nl.armatiek.xslweb.utils.XMLUtils;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

public class Session extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_RESPONSE, "session");

  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 1;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 1;
  }

  @Override
  public SequenceType[] getArgumentTypes() {    
    return new SequenceType[] { SequenceType.SINGLE_NODE };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {    
    return SequenceType.SINGLE_BOOLEAN;
  }
  
  @Override
  public boolean hasSideEffects() {    
    return true;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new ResponseSessionCall();
  }
  
  
  
  /* TODO
   * NodeInfo nodeInfo = (NodeInfo) arguments[0].head();        
        if (StringUtils.equals(nodeInfo.getAttributeValue("", "invalidate"), "true")) {
          session.invalidate();
        }                       
        AxisIterator intervalIter = nodeInfo.iterateAxis(AxisInfo.CHILD, new LocalNameTest(context.getNamePool(), Type.ELEMENT, "max-inactive-interval"));
        NodeInfo interval = intervalIter.next();
        if (interval != null) {
          session.setMaxInactiveInterval(Integer.parseInt(interval.getStringValue()));
        }
        
        AxisIterator attributesIter = nodeInfo.iterateAxis(AxisInfo.CHILD, new LocalNameTest(context.getNamePool(), Type.ELEMENT, "attributes"));
        NodeInfo attributes = attributesIter.next();
        if (attributes != null) {
          AxisIterator attributeIter = attributes.iterateAxis(AxisInfo.CHILD, new LocalNameTest(context.getNamePool(), Type.ELEMENT, "attribute"));          
          NodeInfo attribute;
          while ((attribute = attributeIter.next()) != null) {
            String name = attribute.getAttributeValue("", "name");            
            Collection<Attribute> attrs = sequenceToAttributeCollection( SequenceTool.toLazySequence(attribute.iterateAxis(AxisInfo.CHILD)));
            session.setAttribute(name, attrs);            
          }         
        }
   * 
   * 
   */
  
  private static class ResponseSessionCall extends ExtensionFunctionCall {
    
    private Object getObject(String type, String value) {   
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
        
    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {                            
      try {
        HttpServletRequest request = (HttpServletRequest) ((ObjectValue<?>)context.getController().getParameter(
            new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_REQUEST, "request"))).getObject();                
        HttpSession session = request.getSession();
        
        NodeInfo nodeInfo = (NodeInfo) arguments[0].head();
        
        Element sessionElem = (Element) NodeOverNodeInfo.wrap(nodeInfo);
                
        String interval = XMLUtils.getValueOfChildElementByLocalName(sessionElem, "max-inactive-interval");
        if (interval != null) {
          session.setMaxInactiveInterval(Integer.parseInt(interval));
        }
        if (StringUtils.equals(sessionElem.getAttribute("invalidate"), "true")) {
          session.invalidate();
        }      
        Element attrsElem = XMLUtils.getChildElementByLocalName(sessionElem, "attributes");                
        Element attrElem = XMLUtils.getFirstChildElement(attrsElem);
        while (attrElem != null) {
          String name = attrElem.getAttribute("name");
          if (StringUtils.isBlank(name)) {
            throw new XPathException("Session element \"attribute\" must have an attribute \"name\"");
          }
          List<Attribute> attrs = new ArrayList<Attribute>();
          Element itemElem = XMLUtils.getFirstChildElement(attrElem);
          while (itemElem != null) {
            String type = itemElem.getAttribute("type");
            if (StringUtils.isBlank(type)) {
              throw new XPathException("Session element \"item\" must have an attribute \"type\"");
            }            
            if (type.equals("node()") || type.equals("element()")) {
              attrs.add(new Attribute(XMLUtils.nodeToString(XMLUtils.getFirstChildElement(itemElem)), type, true));                                        
            } else {
              attrs.add(new Attribute(getObject(type, itemElem.getTextContent()), type, false));
            }             
            itemElem = XMLUtils.getNextSiblingElement(itemElem);
          }          
          session.setAttribute(name, attrs);          
          attrElem = XMLUtils.getNextSiblingElement(attrElem);
        }                                           
        return BooleanValue.get(true);              
      } catch (Exception e) {
        throw new XPathException(e);
      }
    }
    
  }

}