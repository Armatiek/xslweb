package nl.armatiek.xslweb.saxon.functions.expath.file;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Definitions;

public class WriteText extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_EXPATH_FILE, "write-text");

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
    return 3;
  }

  @Override
  public SequenceType[] getArgumentTypes() {    
    return new SequenceType[] { 
        SequenceType.SINGLE_STRING, 
        SequenceType.SINGLE_STRING, 
        SequenceType.SINGLE_STRING };
  }
  
  @Override
  public boolean hasSideEffects() {    
    return true;
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {    
    return SequenceType.OPTIONAL_BOOLEAN;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new WriteTextCall(false);
  }
  
}