package nl.armatiek.xslweb.saxon.functions.base64;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;

import org.apache.commons.codec.binary.Base64;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class Base64Encode extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_BASE64, "encode");

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
    return SequenceType.SINGLE_STRING;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new Base64EncodeCall();
  }
  
  private static class Base64EncodeCall extends ExtensionFunctionCall {
           
    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
      try {
        String str = ((StringValue) arguments[0].head()).getStringValue();            
        return StringValue.makeStringValue(Base64.encodeBase64String(str.getBytes("UTF-8")));
      } catch (Exception e) {
        throw new XPathException("Could not base64 encode string", e);
      } 
    }
  }
  
}