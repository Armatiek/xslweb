package nl.armatiek.xslweb.saxon.functions.response;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.utils.XMLUtils;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

public class Session extends ExtensionFunctionDefinition {

  private static final long serialVersionUID = 1L;
  
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
  public ExtensionFunctionCall makeCallExpression() {    
    return new ResponseSessionCall();
  }
  
  private static class ResponseSessionCall extends ExtensionFunctionCall {
        
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    public SequenceIterator<BooleanValue> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {                            
      HttpServletRequest request = (HttpServletRequest) context.getController().getParameter("{" + Definitions.NAMESPACEURI_XSLWEB_REQUEST + "}request");
      HttpSession session = request.getSession();
      
      NodeInfo nodeInfo = (NodeInfo) arguments[0].next();
      NodeOverNodeInfo nodeOverNodeInfo = NodeOverNodeInfo.wrap(nodeInfo);
      Element sessionElem = nodeOverNodeInfo.getOwnerDocument().getDocumentElement();
      
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
        String value = attrElem.getTextContent();          
        session.setAttribute(name, value);          
        attrElem = XMLUtils.getNextSiblingElement(attrElem);
      }                                           
      return SingletonIterator.makeIterator(BooleanValue.get(true));              
    }
    
  }

}