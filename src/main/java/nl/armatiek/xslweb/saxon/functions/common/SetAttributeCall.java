package nl.armatiek.xslweb.saxon.functions.common;

import java.util.Collection;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Attribute;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

public abstract class SetAttributeCall extends ExtensionFunctionCall {

  protected abstract void setAttributes(String name, Collection<Attribute> attrs, XPathContext context);

  @Override
  public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {            
    String name = ((StringValue) arguments[0].head()).getStringValue();                
    Collection<Attribute> attrs = sequenceToAttributeCollection(arguments[1]);     
    setAttributes(name, attrs, context);
    return EmptySequence.getInstance();        
  }
}