package nl.armatiek.xslweb.saxon.functions.function;

import org.apache.commons.lang3.ArrayUtils;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.QualifiedNameValue;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class Call extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_FUNCTION, "call");

  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 2;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 2;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { SequenceType.SINGLE_QNAME, SequenceType.ANY_SEQUENCE };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.ANY_SEQUENCE;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new CallFunctionCall();
  }
  
  private static class CallFunctionCall extends ExtensionFunctionCall {
    
    @Override
    public Sequence<?> call(XPathContext context, Sequence[] arguments) throws XPathException {            
      String functionName = ((QualifiedNameValue) arguments[0].head()).getClarkName();        
      ExtensionFunctionDefinition funcDef = getWebApp(context).getExtensionFunctionDefinition(functionName);
      if (funcDef == null) {
        throw new XPathException("No function with name \"" + functionName + "\" is registered");
      }
      // return funcDef.makeCallExpression().call(context, ArrayUtils.subarray(arguments, 1, arguments.length + 1));
      return funcDef.makeCallExpression().call(context, new Sequence[] { arguments[1] });
    }
  }
}