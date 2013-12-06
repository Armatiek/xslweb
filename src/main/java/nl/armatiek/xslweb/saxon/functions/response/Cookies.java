package nl.armatiek.xslweb.saxon.functions.response;

import javax.servlet.http.Cookie;
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

import org.w3c.dom.Element;

public class Cookies extends ExtensionFunctionDefinition {

  private static final long serialVersionUID = 1L;
  
  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_RESPONSE, "cookies");

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
    return new ResponseAddCookieCall();
  }
  
  private static class ResponseAddCookieCall extends ExtensionFunctionCall {
        
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    public SequenceIterator<BooleanValue> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {                            
      HttpServletResponse response = (HttpServletResponse) context.getController().getParameter("{" + Definitions.NAMESPACEURI_XSLWEB_RESPONSE + "}response");
      NodeInfo nodeInfo = (NodeInfo) arguments[0].next();
      NodeOverNodeInfo nodeOverNodeInfo = NodeOverNodeInfo.wrap(nodeInfo);
      Element cookiesElem = nodeOverNodeInfo.getOwnerDocument().getDocumentElement();        
      Element cookieElem = XMLUtils.getFirstChildElement(cookiesElem);        
      while (cookieElem != null) {
        String comment = XMLUtils.getValueOfChildElementByLocalName(cookieElem, "comment");
        String domain = XMLUtils.getValueOfChildElementByLocalName(cookieElem, "domain");
        String maxAge = XMLUtils.getValueOfChildElementByLocalName(cookieElem, "max-age");
        String name = XMLUtils.getValueOfChildElementByLocalName(cookieElem, "name");
        String path = XMLUtils.getValueOfChildElementByLocalName(cookieElem, "path");
        String isSecure = XMLUtils.getValueOfChildElementByLocalName(cookieElem, "is-secure");
        String value = XMLUtils.getValueOfChildElementByLocalName(cookieElem, "value");
        String version = XMLUtils.getValueOfChildElementByLocalName(cookieElem, "version");
        Cookie cookie = new Cookie(name, value);
        if (comment != null) cookie.setComment(comment);
        if (domain != null) cookie.setDomain(domain);
        if (maxAge != null) cookie.setMaxAge(Integer.parseInt(maxAge));
        if (path != null) cookie.setPath(path);
        if (isSecure != null) cookie.setSecure(Boolean.parseBoolean(isSecure));
        if (version != null) cookie.setVersion(Integer.parseInt(version));                
        response.addCookie(cookie);
        cookieElem = XMLUtils.getNextSiblingElement(cookieElem);
      }                                           
      return SingletonIterator.makeIterator(BooleanValue.get(true));              
    }
    
  }

}