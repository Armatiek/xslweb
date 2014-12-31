package nl.armatiek.xslweb.saxon.functions.response;

import javax.servlet.http.Cookie;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;
import nl.armatiek.xslweb.saxon.utils.NodeInfoUtils;

public class AddCookie extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_RESPONSE, "add-cookie");

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
    return SequenceType.OPTIONAL_BOOLEAN;
  }
  
  @Override
  public boolean hasSideEffects() {    
    return true;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new ResponseAddCookieCall();
  }
  
  private static class ResponseAddCookieCall extends ExtensionFunctionCall {
        
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {                                  
      NodeInfo cookieElem =  unwrapNodeInfo((NodeInfo) arguments[0].head());                                          
      String comment = NodeInfoUtils.getValueOfChildElementByLocalName(cookieElem, "comment", context);
      String domain = NodeInfoUtils.getValueOfChildElementByLocalName(cookieElem, "domain", context);
      String maxAge = NodeInfoUtils.getValueOfChildElementByLocalName(cookieElem, "max-age", context);
      String name = NodeInfoUtils.getValueOfChildElementByLocalName(cookieElem, "name", context);
      String path = NodeInfoUtils.getValueOfChildElementByLocalName(cookieElem, "path", context);
      String isSecure = NodeInfoUtils.getValueOfChildElementByLocalName(cookieElem, "is-secure", context);
      String value = NodeInfoUtils.getValueOfChildElementByLocalName(cookieElem, "value", context);
      String version = NodeInfoUtils.getValueOfChildElementByLocalName(cookieElem, "version", context);
      Cookie cookie = new Cookie(name, value);
      if (comment != null) cookie.setComment(comment);
      if (domain != null) cookie.setDomain(domain);
      if (maxAge != null) cookie.setMaxAge(Integer.parseInt(maxAge));
      if (path != null) cookie.setPath(path);
      if (isSecure != null) cookie.setSecure(Boolean.parseBoolean(isSecure));
      if (version != null) cookie.setVersion(Integer.parseInt(version));                
      getResponse(context).addCookie(cookie);                                                      
      return EmptySequence.getInstance();              
    }
    
  }

}