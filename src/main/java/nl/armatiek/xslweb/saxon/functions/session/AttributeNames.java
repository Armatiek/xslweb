package nl.armatiek.xslweb.saxon.functions.session;

import java.util.ArrayList;
import java.util.Enumeration;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrMore;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

/**
 * @author Maarten Kroon
 *
 */
public class AttributeNames extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_SESSION, "attribute-names");

  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 0;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 0;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.makeSequenceType(BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_MORE);
  }
  
  @Override
  public boolean hasSideEffects() {    
    return false;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new AttributeNamesCall();
  }

  private static class AttributeNamesCall extends ExtensionFunctionCall {
    
    @Override
    public ZeroOrMore<StringValue> call(XPathContext context, Sequence[] arguments) throws XPathException {      
      Enumeration<String> names = getSession(context).getAttributeNames();                  
      ArrayList<StringValue> nameList = new ArrayList<StringValue>();
      if (names != null) {
        while (names.hasMoreElements()) {
          nameList.add(new StringValue(names.nextElement()));
        }
      }
      return new ZeroOrMore<StringValue>(nameList.toArray(new StringValue[nameList.size()]));
    }
  }

}