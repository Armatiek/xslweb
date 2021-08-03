package nl.armatiek.xslweb.saxon.functions.webapp;

import java.util.ArrayList;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Attribute;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;
import nl.armatiek.xslweb.saxon.functions.common.GetCacheValueCall;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class GetCacheValue extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_WEBAPP, "get-cache-value");

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
    return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING };
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
    return new GetCacheValueCall() {            
      @Override
      protected ArrayList<Attribute> getAttributes(String cacheName, String cacheKey, XPathContext context) {               
        return getWebApp(context).getCacheValue(cacheName, cacheKey);
      }
    };    
  }
  
}