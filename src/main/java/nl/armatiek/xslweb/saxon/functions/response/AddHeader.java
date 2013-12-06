package nl.armatiek.xslweb.saxon.functions.response;

import javax.servlet.http.HttpServletResponse;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Definitions;

public class AddHeader extends ExtensionFunctionDefinition {

  private static final long serialVersionUID = 1L;
  
  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_RESPONSE, "headers");

  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 2;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 2;
  }

  @Override
  public SequenceType[] getArgumentTypes() {    
    return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {    
    return SequenceType.SINGLE_BOOLEAN;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new ResponseHeaderCall();
  }
  
  private static class ResponseHeaderCall extends ExtensionFunctionCall {
        
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    public SequenceIterator<BooleanValue> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {      
      try {                
        String name = ((IntegerValue) arguments[0].next()).getStringValue();
        String value = ((IntegerValue) arguments[1].next()).getStringValue();       
        HttpServletResponse response = (HttpServletResponse) context.getController().getParameter("{" + Definitions.NAMESPACEURI_XSLWEB_RESPONSE + "}response");        
        response.setHeader(name, value);                
        return SingletonIterator.makeIterator(BooleanValue.get(true));        
      } catch (Exception e) {
        throw new XPathException("Error setting HTTP response header", e);
      }
    } 
  }
}