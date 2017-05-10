package nl.armatiek.xslweb.saxon.functions.index;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xmlindex.Session;
import nl.armatiek.xmlindex.XMLIndex;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

/**
 * XPath extension function class 
 * 
 * @author Maarten Kroon
 */
public class GetSession extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_XMLINDEX, "get-session");

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
    return SequenceType.makeSequenceType(new JavaExternalObjectType(Session.class), StaticProperty.ALLOWS_ONE);
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new GetSessionCall();
  }
  
  private static class GetSessionCall extends ExtensionFunctionCall {

    @Override
    public ObjectValue<Session> call(XPathContext context, Sequence[] arguments) throws XPathException {            
      try {
        String name = ((StringValue) arguments[0].head()).getStringValue();        
        WebApp webapp = getWebApp(context);
        XMLIndex index = webapp.getXMLIndex(name);
        Session session = index.aquireSession();
        addCloseable(new CloseableSessionWrapper(session), context);
        return new ObjectValue<Session>(session);        
      } catch (Exception e) {
        throw new XPathException("Could not get session", e);
      }
    }
  }
}