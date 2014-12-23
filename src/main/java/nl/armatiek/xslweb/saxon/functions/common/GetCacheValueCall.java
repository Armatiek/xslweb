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

public abstract class GetCacheValueCall extends ExtensionFunctionCall {

  protected abstract Collection<Attribute> getAttributes(String cacheName, String keyName, XPathContext context);

  @Override
  public ZeroOrMore<Item> call(XPathContext context, Sequence[] arguments) throws XPathException {            
    try {
      String cacheName = ((StringValue) arguments[0].head()).getStringValue();
      String keyName = ((StringValue) arguments[1].head()).getStringValue();            
      Collection<Attribute> attrs = getAttributes(cacheName, keyName, context);      
      return attributeCollectionToSequence(attrs, context);            
    } catch (Exception e) {
      throw new XPathException("Could not get cache value", e);
    }
  }
}