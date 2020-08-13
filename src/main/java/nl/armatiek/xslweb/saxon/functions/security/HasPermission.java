package nl.armatiek.xslweb.saxon.functions.security;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
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
public class HasPermission extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_SECURITY, "has-permisson");

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
    return new SequenceType[] { SequenceType.SINGLE_STRING };
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
    return new HasPermissionCall();
  }

  private static class HasPermissionCall extends ExtensionFunctionCall {
    
    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {                            
      Subject subject = SecurityUtils.getSubject();
      String name = ((StringValue) arguments[0].head()).getStringValue();
      return BooleanValue.get(subject != null && StringUtils.isNoneBlank(name) && subject.isPermitted(name)); 
    }
    
  }

}