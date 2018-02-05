package nl.armatiek.xslweb.saxon.functions.util;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.functions.ParseXml;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Definitions;

/**
 * XPath extension function class for
 * 
 * @author Maarten Kroon
 */
public class Parse extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_UTIL, "parse");

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
    return SequenceType.makeSequenceType(NodeKindTest.DOCUMENT, StaticProperty.EXACTLY_ONE);    
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new ParseCall();
  }
  
  private static class ParseCall extends ExtensionFunctionCall {
    
    @Override
    public ZeroOrOne<NodeInfo> call(XPathContext context, Sequence[] arguments) throws XPathException {
      ParseXml parseXml = new ParseXml();
      parseXml.setRetainedStaticContext(new RetainedStaticContext(context.getConfiguration()));
      return new ZeroOrOne<NodeInfo>(parseXml.call(context, arguments));
    }
  }
}