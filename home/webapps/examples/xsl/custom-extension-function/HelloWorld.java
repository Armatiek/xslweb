package nl.armatiek.xslweb.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import com.google.common.io.BaseEncoding;

public class HelloWorld extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", "http://www.armatiek.com/xslweb/functions/custom", "hello-world");

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
    return new HelloWorldCall2();
  }
  
  private static class HelloWorldCall2 extends ExtensionFunctionCall {

    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {            
      try {
        String str = ((StringValue) arguments[0].head()).getStringValue();
        return new StringValue("Hello World: " + BaseEncoding.base64().encode(str.getBytes("UTF-8")));        
      } catch (Exception e) {
        throw new XPathException("Could not base64 encode string", e);
      }            
    }

  }
  
}