package nl.armatiek.xslweb.saxon.functions;

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

public class ResponseStatus extends ExtensionFunctionDefinition {

  private static final long serialVersionUID = 1L;
  
  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_RESPONSE, "status");

  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 1;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 1;
  }

  @Override
  public SequenceType[] getArgumentTypes() {    
    return new SequenceType[] { SequenceType.SINGLE_INT };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {    
    return SequenceType.SINGLE_BOOLEAN;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new ResponseStatusCall();
  }
  
  private static class ResponseStatusCall extends ExtensionFunctionCall {
        
    private static final long serialVersionUID = 1L;

    public SequenceIterator<BooleanValue> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {
      String xslPath = "";
      try {                
        long status = ((IntegerValue) arguments[0].next()).longValue();               
        HttpServletResponse response = (HttpServletResponse) context.getController().getParameter(null);        
        response.setStatus((int) status);                
        return SingletonIterator.makeIterator(BooleanValue.get(true));        
      } catch (Exception e) {
        throw new XPathException(String.format("Error executing transformation using \"%s\"", xslPath), e);
      }
    }
    
  }

}