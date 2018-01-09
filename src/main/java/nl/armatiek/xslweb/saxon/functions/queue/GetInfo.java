package nl.armatiek.xslweb.saxon.functions.queue;

import java.io.File;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrOne;
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
public class GetInfo extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_QUEUE, "get-info");

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
    return SequenceType.OPTIONAL_NODE;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new GetInfoCall();
  }

  private static class GetInfoCall extends ExtensionFunctionCall {
    
    @Override
    public ZeroOrOne<NodeInfo> call(XPathContext context, Sequence[] arguments) throws XPathException {
      String ticket = ((StringValue) arguments[0].head()).getStringValue();
      File queueDir = Context.getInstance().getQueueDir();
      File infoFile = new File(queueDir, ticket + ".xml");
      if (!infoFile.isFile() || new File(queueDir, ticket + ".lck").isFile())
        return ZeroOrOne.empty();
      return new ZeroOrOne<NodeInfo>(context.getConfiguration().buildDocumentTree(new StreamSource(infoFile)).getRootNode());
    }
    
  }
  
}