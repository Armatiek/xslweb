package nl.armatiek.xslweb.saxon.functions.common;

import java.util.Collection;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Attribute;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

public abstract class SetCacheValueCall extends ExtensionFunctionCall {

  private static final long serialVersionUID = 1L;
  
  protected abstract void setAttributes(String cacheName, String keyName, Collection<Attribute> attrs, int duration, XPathContext context);

  @SuppressWarnings("rawtypes")
  public SequenceIterator<BooleanValue> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {            
    String cacheName = ((StringValue) arguments[0].next()).getStringValue();
    String keyName = ((StringValue) arguments[1].next()).getStringValue();
    int duration = (int) ((IntegerValue) arguments[3].next()).longValue();
    Collection<Attribute> attrs = sequenceToAttributeCollection(arguments[2]);       
    setAttributes(cacheName, keyName, attrs, duration, context);
    return SingletonIterator.makeIterator(BooleanValue.get(true));
  }
}