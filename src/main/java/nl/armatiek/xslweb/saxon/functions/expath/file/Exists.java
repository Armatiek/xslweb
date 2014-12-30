package nl.armatiek.xslweb.saxon.functions.expath.file;

import java.io.File;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FileException;

public class Exists extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_EXPATH_FILE, "exists");

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
    return new SequenceType[] { SequenceType.SINGLE_STRING };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {    
    return SequenceType.SINGLE_BOOLEAN;
  }
  
  @Override
  public boolean hasSideEffects() {    
    return false;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new ExistsCall();
  }
  
  private static class ExistsCall extends FileExtensionFunctionCall {
        
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {      
      try {
        File file = getFile(((StringValue) arguments[0].head()).getStringValue());                                       
        return BooleanValue.get(file.exists());        
      } catch (Exception e) {
        throw new FileException("Other file error", e, FileException.ERROR_IO);
      }
    } 
  }
}