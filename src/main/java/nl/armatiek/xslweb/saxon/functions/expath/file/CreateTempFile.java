package nl.armatiek.xslweb.saxon.functions.expath.file;

import java.io.File;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FileException;

public class CreateTempFile extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_EXPATH_FILE, "create-temp-file");

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
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {    
    return SequenceType.SINGLE_STRING;
  }
  
  @Override
  public boolean hasSideEffects() {    
    return true;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new CreateTempFileCall();
  }
  
  private static class CreateTempFileCall extends FileExtensionFunctionCall {
        
    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {      
      try {   
        String prefix = ((StringValue) arguments[0].head()).getStringValue();
        String suffix = ((StringValue) arguments[1].head()).getStringValue();
        File dir = null;
        if (arguments.length == 3) {
          dir = getFile(((StringValue) arguments[2].head()).getStringValue());
        }
        File temp;
        if (dir == null) {
          temp = File.createTempFile(prefix, suffix);
        } else {
          if (dir.exists() || dir.isFile()) {
            throw new FileException(String.format("Specified directory \"%s\" does not exist or points to a file", 
              dir.getAbsolutePath()), FileException.ERROR_PATH_NOT_DIRECTORY);
          }
          temp = File.createTempFile(prefix, suffix, dir);
        }
        temp.deleteOnExit();
        return StringValue.makeStringValue(temp.getCanonicalPath());
      } catch (FileException fe) {
        throw fe;
      } catch (Exception e) {
        throw new FileException("Other file error", e, FileException.ERROR_IO);
      }
    } 
  }
}