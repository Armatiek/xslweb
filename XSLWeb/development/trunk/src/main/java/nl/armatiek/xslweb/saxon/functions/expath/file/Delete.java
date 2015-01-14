package nl.armatiek.xslweb.saxon.functions.expath.file;

import java.io.File;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FileException;

import org.apache.commons.io.FileUtils;

public class Delete extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_EXPATH_FILE, "delete");

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
    return 2;
  }

  @Override
  public SequenceType[] getArgumentTypes() {    
    return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.SINGLE_BOOLEAN };
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
    return new DeleteCall();
  }
  
  private static class DeleteCall extends FileExtensionFunctionCall {
        
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {      
      try {         
        File file = getFile(((StringValue) arguments[0].head()).getStringValue());
        boolean recursive = false;
        if (arguments.length > 1) {
          recursive = ((BooleanValue) arguments[1].head()).getBooleanValue();
        }                
        if (!file.exists()) {
          throw new FileException(String.format("Path \"%s\" does not exist", 
              file.getAbsolutePath()), FileException.ERROR_PATH_NOT_EXIST);         
        }
        if (file.isDirectory() && !recursive && file.list().length > 0) {
          throw new FileException(String.format("Path \"%s\" points to a non-empty directory", 
              file.getAbsolutePath()), FileException.ERROR_PATH_IS_DIRECTORY);
        }        
        FileUtils.forceDelete(file);                
        return EmptySequence.getInstance();
      } catch (FileException fe) {
        throw fe;
      } catch (Exception e) {
        throw new FileException("Other file error", e, FileException.ERROR_IO);
      }
    } 
  }
}