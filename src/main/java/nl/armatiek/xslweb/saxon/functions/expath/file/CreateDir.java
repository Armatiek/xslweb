package nl.armatiek.xslweb.saxon.functions.expath.file;

import java.io.File;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FileException;

import org.apache.commons.io.FileUtils;

public class CreateDir extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_EXPATH_FILE, "create-dir");

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
    return SequenceType.OPTIONAL_BOOLEAN;
  }
  
  @Override
  public boolean hasSideEffects() {    
    return true;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new CreateDirCall();
  }
  
  private static class CreateDirCall extends FileExtensionFunctionCall {
        
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {      
      try {                
        File dir = getFile(((StringValue) arguments[0].head()).getStringValue());
        if (dir.isFile()) {
          throw new FileException(String.format("Specified path \"%s\" points to an existing file", 
              dir.getAbsolutePath()), FileException.ERROR_PATH_EXISTS);
        }                      
        FileUtils.forceMkdir(dir);
        return EmptySequence.getInstance();
      } catch (Exception e) {
        throw new FileException("Other file error", e, FileException.ERROR_IO);
      }
    } 
  }
}