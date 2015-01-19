package nl.armatiek.xslweb.saxon.functions.common;

import java.util.Collection;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Attribute;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

public abstract class SetCacheValueCall extends ExtensionFunctionCall {

  protected abstract void setAttributes(String cacheName, String keyName, Collection<Attribute> attrs, 
      int tti, int ttl, XPathContext context);

  @Override
  public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {            
    String cacheName = ((StringValue) arguments[0].head()).getStringValue();
    String keyName = ((StringValue) arguments[1].head()).getStringValue();        
    int tti = (int) ((IntegerValue) arguments[3].head()).longValue();
    int ttl = (int) ((IntegerValue) arguments[4].head()).longValue();
    Collection<Attribute> attrs = sequenceToAttributeCollection(arguments[2]);       
    setAttributes(cacheName, keyName, attrs, tti, ttl, context);
    return EmptySequence.getInstance();
  }
}