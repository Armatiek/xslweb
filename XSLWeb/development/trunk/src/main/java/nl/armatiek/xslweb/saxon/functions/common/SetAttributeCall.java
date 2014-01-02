package nl.armatiek.xslweb.saxon.functions.common;

import java.util.ArrayList;
import java.util.Collection;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Value;
import nl.armatiek.xslweb.configuration.Attribute;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

public abstract class SetAttributeCall extends ExtensionFunctionCall {

  private static final long serialVersionUID = 1L;
  
  protected abstract void setAttributes(String name, Collection<Attribute> attrs, XPathContext context);

  @SuppressWarnings("rawtypes")
  public SequenceIterator<BooleanValue> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {            
    String name = ((StringValue) arguments[0].next()).getStringValue();      
    if (arguments.length == 1) {
      Context.getInstance().removeAttribute(name);       
    } else {
      ArrayList<Attribute> attrs = new ArrayList<Attribute>();
      Item item;
      while ((item = arguments[1].next()) != null) {                
        Object value;
        String type;
        boolean isSerialized;
        if (item instanceof NodeInfo) {
          value = serialize((NodeInfo) item);
          type = "node()";
          isSerialized = true;
        } else {                             
          value = Value.convertToJava(item);
          ItemType itemType = Type.getItemType(item, null);
          type = itemType.toString();
          isSerialized = false;
        }
        attrs.add(new Attribute(value, type, isSerialized));
      }
      setAttributes(name, attrs, context);      
    }
    return SingletonIterator.makeIterator(BooleanValue.get(true));
  }
}