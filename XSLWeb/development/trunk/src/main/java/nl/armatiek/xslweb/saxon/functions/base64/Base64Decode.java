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
public class Base64Decode extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_BASE64, "decode");

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
    return SequenceType.SINGLE_STRING;
  }

  public ExtensionFunctionCall makeCallExpression() {
    return new Base64DecodeCall();
  }
  
  private static class Base64DecodeCall extends ExtensionFunctionCall {

    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {            
      try {
        String str = ((StringValue) arguments[0].head()).getStringValue();        
        return StringValue.makeStringValue(new String(Base64.decodeBase64(str), "UTF-8"));        
      } catch (Exception e) {
        throw new XPathException("Could not base64 decode string", e);
      }
    }
  }
}