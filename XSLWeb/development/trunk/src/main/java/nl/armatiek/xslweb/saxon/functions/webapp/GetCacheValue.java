package nl.armatiek.xslweb.saxon.functions.webapp;

import java.util.Collection;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Attribute;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;
import nl.armatiek.xslweb.saxon.functions.common.GetCacheValueCall;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class GetCacheValue extends ExtensionFunctionDefinition {

  private static final long serialVersionUID = 1L;
  
  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_WEBAPP, "get-cache-value");

  public StructuredQName getFunctionQName() {
    return qName;
  }

  public int getMinimumNumberOfArguments() {
    return 2;
  }

  public int getMaximumNumberOfArguments() {
    return 2;
  }

  public SequenceType[] getArgumentTypes() {    
    return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING };
  }

  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.ANY_SEQUENCE;
  }

  @SuppressWarnings("serial")
  public ExtensionFunctionCall makeCallExpression() {
    return new GetCacheValueCall() {            
      @Override
      protected Collection<Attribute> getAttributes(String cacheName, String cacheKey, XPathContext context) {
        WebApp webApp = (WebApp) context.getController().getParameter("{" + Definitions.NAMESPACEURI_XSLWEB_WEBAPP + "}webapp");        
        return webApp.getCacheValue(cacheName, cacheKey);
      }
    };    
  }
  
}