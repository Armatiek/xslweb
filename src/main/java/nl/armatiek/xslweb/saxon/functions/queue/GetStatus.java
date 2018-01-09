package nl.armatiek.xslweb.saxon.functions.queue;

import java.io.File;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class GetStatus extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_QUEUE, "get-status");

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
    return new GetStatusResponseCall();
  }

  private static class GetStatusResponseCall extends ExtensionFunctionCall {
    
    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
      String ticket = ((StringValue) arguments[0].head()).getStringValue();
      File queueDir = Context.getInstance().getQueueDir();
      String status = "notfound";
      if (new File(queueDir, ticket + ".lck").isFile())
        status = "processing";
      else if (new File(queueDir, ticket + ".err").isFile())
        status = "error";
      else if (new File(queueDir, ticket + ".bin").isFile())
        status = "available";
      return StringValue.makeStringValue(status);
    }
    
  }
  
}