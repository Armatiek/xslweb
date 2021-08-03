package nl.armatiek.xslweb.saxon.functions.request;

import java.util.ArrayList;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Attribute;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;
import nl.armatiek.xslweb.saxon.functions.common.GetAttributeCall;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class GetAttribute extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_REQUEST, "get-attribute");

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
    return SequenceType.ANY_SEQUENCE;
  }
  
  @Override
  public boolean hasSideEffects() {    
    return false;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new GetAttributeCall() {      
      @SuppressWarnings("unchecked")
      @Override
      protected ArrayList<Attribute> getAttributes(String name, XPathContext context) {                               
        return (ArrayList<Attribute>) getRequest(context).getAttribute(name);
      }
    };    
  }
  
}