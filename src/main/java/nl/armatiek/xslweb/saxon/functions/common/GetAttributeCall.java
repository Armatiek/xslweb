package nl.armatiek.xslweb.saxon.functions.common;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.transform.dom.DOMSource;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.xpath.XPathEvaluator;
import nl.armatiek.xslweb.configuration.Attribute;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

import org.w3c.dom.Node;

public abstract class GetAttributeCall extends ExtensionFunctionCall {

  private static final long serialVersionUID = 1L;
  
  protected abstract Collection<Attribute> getAttributes(String name, XPathContext context);

  @SuppressWarnings("rawtypes")
  public SequenceIterator<Item> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {            
    try {
      String name = ((StringValue) arguments[0].next()).getStringValue();        
      Collection<Attribute> attrs = getAttributes(name, context);
      if (attrs == null) {
        return EmptyIterator.emptyIterator();
      }
      ArrayList<Item> results = new ArrayList<Item>();
      XPathEvaluator evaluator = new XPathEvaluator(context.getConfiguration());
      for (Attribute attr : attrs) {
        Object value = attr.getValue();      
        if (value instanceof Node) {                              
          results.add(evaluator.setSource(new DOMSource((Node) value)));                                        
        } else {          
          results.add(convertJavaObjectToAtomicValue(value));          
        }        
      }
      return new ListIterator<Item>(results);
    } catch (Exception e) {
      throw new XPathException("Could not get attribute", e);
    }
  }
}