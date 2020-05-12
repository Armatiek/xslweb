package nl.armatiek.xslweb.saxon.functions.function;

import java.util.ArrayList;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.ZeroOrMore;
import net.sf.saxon.trans.XPathException;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

public abstract class DynamicExtensionFunctionCall extends ExtensionFunctionCall {

  protected Item toItem(Object obj) throws XPathException {
    return (obj instanceof Item) ? (Item) obj : convertJavaObjectToAtomicValue(obj);
  }
  
  protected ZeroOrMore<Item<?>> convertToZeroOrMore(Object obj) throws XPathException {
    ArrayList<Item<?>> results = new ArrayList<Item<?>>();
    if (obj.getClass().isArray()) {
      Object[] objects = (Object[]) obj;
      for (Object o : objects) {
        results.add(toItem(o));
      }
    } else {
      results.add(toItem(obj));
    }
    return new ZeroOrMore<Item<?>>(results);
  }

}