package nl.armatiek.xslweb.saxon.functions.common;

import java.util.Collection;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrMore;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Attribute;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

public abstract class GetAttributeCall extends ExtensionFunctionCall {

  protected abstract Collection<Attribute> getAttributes(String name, XPathContext context);

  @Override
  public ZeroOrMore<Item> call(XPathContext context, Sequence[] arguments) throws XPathException {            
    try {
      String name = ((StringValue) arguments[0].head()).getStringValue();                                   
      Collection<Attribute> attrs = getAttributes(name, context);
      return attributeCollectionToSequence(attrs, context);            
    } catch (Exception e) {
      throw new XPathException("Could not get attribute", e);
    }
  }
}