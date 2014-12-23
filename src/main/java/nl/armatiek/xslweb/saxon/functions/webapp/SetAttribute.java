package nl.armatiek.xslweb.saxon.functions.webapp;

import java.util.Collection;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Attribute;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;
import nl.armatiek.xslweb.saxon.functions.common.SetAttributeCall;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class SetAttribute extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_WEBAPP, "set-attribute");

  public StructuredQName getFunctionQName() {
    return qName;
  }

  public int getMinimumNumberOfArguments() {
    return 1;
  }

  public int getMaximumNumberOfArguments() {
    return 2;
  }

  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.ANY_SEQUENCE };
  }

  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.SINGLE_BOOLEAN;
  }

  public ExtensionFunctionCall makeCallExpression() {
    return new SetAttributeCall() {
      @Override
      protected void setAttributes(String name, Collection<Attribute> attrs, XPathContext context) {
        WebApp webApp = (WebApp) ((ObjectValue<?>)context.getController().getParameter(
            new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_WEBAPP, "webapp"))).getObject();                        
        webApp.setAttribute(name, attrs);
      }
    };
  }
  
}