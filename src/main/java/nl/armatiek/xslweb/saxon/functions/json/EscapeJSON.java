package nl.armatiek.xslweb.saxon.functions.json;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrOne;
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
public class EscapeJSON extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_JSON, "escape-json");

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
    return new SequenceType[] { SequenceType.OPTIONAL_STRING };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.OPTIONAL_STRING;
  }
  
  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new EscapeJSONCall();
  }

  private static class EscapeJSONCall extends ExtensionFunctionCall {
    
    @Override
    public ZeroOrOne<StringValue> call(XPathContext context, Sequence[] arguments) throws XPathException {                     
      try {
        String json = ((StringValue) arguments[0].head()).getStringValue();
        if (StringUtils.isBlank(json)) {
          return ZeroOrOne.empty();
        }        
        return new ZeroOrOne<StringValue>(new StringValue(StringEscapeUtils.escapeJson(json)));                
      } catch (Exception e) {
        throw new XPathException("Error escaping JSON string", e);
      }                    
    }
  }
  
}