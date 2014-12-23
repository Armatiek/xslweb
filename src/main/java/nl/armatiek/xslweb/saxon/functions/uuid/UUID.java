package nl.armatiek.xslweb.saxon.functions.uuid;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class UUID extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_UUID, "uuid");

  public StructuredQName getFunctionQName() {
    return qName;
  }

  public int getMinimumNumberOfArguments() {
    return 0;
  }

  public int getMaximumNumberOfArguments() {
    return 0;
  }

  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { };
  }

  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.SINGLE_STRING;
  }

  public ExtensionFunctionCall makeCallExpression() {
    return new UUIDCall();
  }
  
  private static class UUIDCall extends ExtensionFunctionCall {

    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
      return StringValue.makeStringValue(java.util.UUID.randomUUID().toString());            
    }
  }
}