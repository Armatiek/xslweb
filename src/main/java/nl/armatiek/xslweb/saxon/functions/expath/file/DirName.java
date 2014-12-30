package nl.armatiek.xslweb.saxon.functions.expath.file;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class DirName extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_EXPATH_FILE, "dir-name");

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
    return SequenceType.SINGLE_STRING;
  }
  
  @Override
  public boolean hasSideEffects() {    
    return false;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new DirNameCall();
  }
  
  private static class DirNameCall extends FileExtensionFunctionCall {
        
    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
      String path = ((StringValue) arguments[0].head()).getStringValue();        
      String dirName;
      if (StringUtils.isBlank(path) || StringUtils.containsNone(path, "\\/")) {
        dirName = ".";
      } else {
        dirName = FilenameUtils.getPathNoEndSeparator(path);        
      }                        
      return new StringValue(dirName);                                  
    } 
  }
}