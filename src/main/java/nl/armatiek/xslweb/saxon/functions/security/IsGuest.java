package nl.armatiek.xslweb.saxon.functions.security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class IsGuest extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_SECURITY, "is-guest");

  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 0;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 0;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { };
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
    return new IsGuestCall();
  }

  private static class IsGuestCall extends SecurityExtensionFunctionCall {
    
    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {                            
      if (!isSecurityContextAvailable(context)) {
        return BooleanValue.get(false);
      }
      Subject subject = SecurityUtils.getSubject();
      return BooleanValue.get(subject == null || subject.getPrincipal() == null);              
    }
    
  }

}