package nl.armatiek.xslweb.saxon.functions.response;

import javax.servlet.http.HttpServletResponse;

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
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.utils.NodeInfoUtils;

import org.apache.commons.lang3.StringUtils;

public class Headers extends ExtensionFunctionDefinition {

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
        
    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {      
      HttpServletResponse response = (HttpServletResponse) ((ObjectValue<?>)context.getController().getParameter(
          new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_RESPONSE, "response"))).getObject();
      NodeInfo headersElem = (NodeInfo) arguments[0].head();                   
      NodeInfo headerElem = NodeInfoUtils.getFirstChildElement(headersElem);        
      while (headerElem != null) {                     
        String name = headerElem.getAttributeValue("", "name");
        if (StringUtils.isBlank(name)) {
          throw new XPathException("Element \"header\" must have an attribute \"name\"");
        }          
        String value = headerElem.getStringValue();          
        response.setHeader(name, value);          
        headerElem = NodeInfoUtils.getNextSiblingElement(headerElem);
      }                                           
      return BooleanValue.TRUE;
    } 
  }
}