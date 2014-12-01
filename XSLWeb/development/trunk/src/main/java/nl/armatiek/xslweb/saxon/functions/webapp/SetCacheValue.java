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
import nl.armatiek.xslweb.saxon.functions.common.SetCacheValueCall;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class SetCacheValue extends ExtensionFunctionDefinition {

  private static final long serialVersionUID = 1L;
  
  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_WEBAPP, "set-cache-value");

  public StructuredQName getFunctionQName() {
    return qName;
  }

  public int getMinimumNumberOfArguments() {
    return 4;
  }

  public int getMaximumNumberOfArguments() {
    return 4;
  }

  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING, SequenceType.ANY_SEQUENCE, SequenceType.SINGLE_INTEGER };
  }

  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.SINGLE_BOOLEAN;
  }

  @SuppressWarnings("serial")
  public ExtensionFunctionCall makeCallExpression() {
    return new SetCacheValueCall() {
      @Override
      protected void setAttributes(String cacheName, String keyName, Collection<Attribute> attrs, int duration, XPathContext context) {
        WebApp webApp = (WebApp) context.getController().getParameter("{" + Definitions.NAMESPACEURI_XSLWEB_WEBAPP + "}webapp");        
        webApp.setCacheValue(cacheName, keyName, attrs, duration);        
      }
    };
  }
  
}