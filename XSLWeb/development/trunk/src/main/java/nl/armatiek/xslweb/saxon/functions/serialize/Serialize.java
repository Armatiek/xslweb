package nl.armatiek.xslweb.saxon.functions.serialize;

import java.io.StringWriter;
import java.util.Properties;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class Serialize extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_SERIALIZE, "serialize");

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
    return 2;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { SequenceType.ANY_SEQUENCE, SequenceType.OPTIONAL_NODE };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.SINGLE_STRING;
  }
  
  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new SerializeCall();
  }

  private static class SerializeCall extends ExtensionFunctionCall {
    
    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {                            
      Properties props = null;
      if (arguments.length == 2) {
        props = getOutputProperties((NodeInfo) arguments[1].head());
      }
      StringWriter sw = new StringWriter();
      serialize(arguments[1], sw, props);
      return StringValue.makeStringValue(sw.toString());              
    }
  }
  
}