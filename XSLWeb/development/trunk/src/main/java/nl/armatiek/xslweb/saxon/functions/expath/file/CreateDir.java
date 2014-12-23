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
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0002Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE9999Exception;

import org.apache.commons.io.FileUtils;

public class CreateDir extends FileExtensionFunctionDefinition {

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
    return SequenceType.SINGLE_BOOLEAN;
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
          throw new FILE0002Exception(dir);
        }                      
        FileUtils.forceMkdir(dir);
        return BooleanValue.TRUE;
      } catch (ExpectedFileException e) {
        throw e;
      } catch (Exception e) {
        throw new FILE9999Exception(e);
      }
    } 
  }
}