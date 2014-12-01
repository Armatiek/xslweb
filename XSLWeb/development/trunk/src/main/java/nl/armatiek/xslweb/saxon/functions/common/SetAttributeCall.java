package nl.armatiek.xslweb.saxon.functions.common;

import java.util.Collection;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Attribute;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

public abstract class SetAttributeCall extends ExtensionFunctionCall {

  private static final long serialVersionUID = 1L;
  
  protected abstract void setAttributes(String name, Collection<Attribute> attrs, XPathContext context);

  @SuppressWarnings("rawtypes")
  public SequenceIterator<BooleanValue> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {            
    String name = ((StringValue) arguments[0].next()).getStringValue();                
    Collection<Attribute> attrs = sequenceToAttributeCollection(arguments[1]);     
    setAttributes(name, attrs, context);          
    return SingletonIterator.makeIterator(BooleanValue.get(true));
  }
}