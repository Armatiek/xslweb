package nl.armatiek.xslweb.saxon.functions.expath.file;

import java.io.File;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.ExpectedFileException;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0001Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0004Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE9999Exception;

import org.apache.commons.io.FileUtils;

public class Delete extends FileExtensionFunctionDefinition {

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
    return SequenceType.SINGLE_BOOLEAN;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new DeleteCall();
  }
  
  private static class DeleteCall extends FileExtensionFunctionCall {
        
    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {      
      try {         
        File file = getFile(((StringValue) arguments[0].head()).getStringValue());
        boolean recursive = false;
        if (arguments.length > 1) {
          recursive = ((BooleanValue) arguments[1].head()).getBooleanValue();
        }                
        if (!file.exists()) {
          throw new FILE0001Exception(file);          
        }
        if (file.isDirectory() && !recursive && file.list().length > 0) {
          throw new FILE0004Exception(file, String.format("The specified path points to a non-empty directory (%s)", file.getAbsolutePath()));
        }        
        FileUtils.forceDelete(file);                
        return BooleanValue.TRUE;
      } catch (ExpectedFileException e) {
        throw e;
      } catch (Exception e) {
        throw new FILE9999Exception(e);
      }
    } 
  }
}