package nl.armatiek.xslweb.saxon.functions.security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class HasRole extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_SECURITY, "has-role");

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
    return new SequenceType[] { SequenceType.makeSequenceType(BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ONE_OR_MORE) };
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
    return new HasRoleCall();
  }

  private static class HasRoleCall extends SecurityExtensionFunctionCall {
    
    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
      if (!isSecurityContextAvailable(context)) {
        return BooleanValue.get(false);
      }
      boolean hasAnyRole = false;
      Subject subject = SecurityUtils.getSubject();
      if (subject != null) {
        SequenceIterator iter = arguments[0].iterate();
        StringValue value;
        while ((value = (StringValue) iter.next()) != null) {
          if (subject.hasRole(value.getStringValue().trim())) {
            hasAnyRole = true;
            break;
          }
        }
      }
      return BooleanValue.get(hasAnyRole);
    }
    
  }

}