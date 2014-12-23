package nl.armatiek.xslweb.saxon.functions.session;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Attribute;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;
import nl.armatiek.xslweb.saxon.functions.common.GetAttributeCall;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class GetAttribute extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_SESSION, "get-attribute");

  public StructuredQName getFunctionQName() {
    return qName;
  }

  public int getMinimumNumberOfArguments() {
    return 1;
  }

  public int getMaximumNumberOfArguments() {
    return 1;
  }

  public SequenceType[] getArgumentTypes() {    
    return new SequenceType[] { SequenceType.SINGLE_STRING };
  }

  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.ANY_SEQUENCE;
  }

  public ExtensionFunctionCall makeCallExpression() {
    return new GetAttributeCall() {      
      @SuppressWarnings("unchecked")
      @Override
      protected Collection<Attribute> getAttributes(String name, XPathContext context) {
        HttpServletRequest request = (HttpServletRequest) ((ObjectValue<?>)context.getController().getParameter(
            new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_REQUEST, "request"))).getObject();
        HttpSession session = request.getSession();                        
        return (Collection<Attribute>) session.getAttribute(name);
      }
    };    
  }
  
}