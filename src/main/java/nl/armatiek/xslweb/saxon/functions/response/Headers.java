package nl.armatiek.xslweb.saxon.functions.response;

import javax.servlet.http.HttpServletResponse;

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

public class Headers extends ExtensionFunctionDefinition {

  private static final long serialVersionUID = 1L;
  
  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_RESPONSE, "headers");

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
    return new ResponseHeadersCall();
  }
  
  private static class ResponseHeadersCall extends ExtensionFunctionCall {
        
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    public SequenceIterator<BooleanValue> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {      
      HttpServletResponse response = (HttpServletResponse) context.getController().getParameter("{" + Definitions.NAMESPACEURI_XSLWEB_RESPONSE + "}response");
      NodeInfo nodeInfo = (NodeInfo) arguments[0].next();
      NodeOverNodeInfo nodeOverNodeInfo = NodeOverNodeInfo.wrap(nodeInfo);
      Element headersElem = nodeOverNodeInfo.getOwnerDocument().getDocumentElement();        
      Element headerElem = XMLUtils.getFirstChildElement(headersElem);        
      while (headerElem != null) {                     
        String name = headerElem.getAttribute("name");
        if (StringUtils.isBlank(name)) {
          throw new XPathException("Element \"header\" must have an attribute \"name\"");
        }          
        String value = headerElem.getTextContent();          
        response.setHeader(name, value);          
        headerElem = XMLUtils.getNextSiblingElement(headerElem);
      }                                           
      return SingletonIterator.makeIterator(BooleanValue.get(true));
    } 
  }
}