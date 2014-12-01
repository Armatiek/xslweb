package nl.armatiek.xslweb.saxon.functions.common;

import java.util.Collection;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Attribute;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

public abstract class GetCacheValueCall extends ExtensionFunctionCall {

  private static final long serialVersionUID = 1L;
  
  protected abstract Collection<Attribute> getAttributes(String cacheName, String keyName, XPathContext context);

  @SuppressWarnings("rawtypes")
  public SequenceIterator<Item> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {            
    try {
      String cacheName = ((StringValue) arguments[0].next()).getStringValue();
      String keyName = ((StringValue) arguments[1].next()).getStringValue();
      Collection<Attribute> attrs = getAttributes(cacheName, keyName, context);      
      return attributeCollectionToSequence(attrs, context);            
    } catch (Exception e) {
      throw new XPathException("Could not get cache value", e);
    }
  }
}