package nl.armatiek.xslweb.saxon.functions.expath.file;

import java.io.File;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.ExpectedFileException;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0001Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0002Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0003Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0004Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE9999Exception;

import org.apache.commons.io.FileUtils;

public class Copy extends FileExtensionFunctionDefinition {

  private static final long serialVersionUID = 1L;
  
  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_EXPATH_FILE, "copy");

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
    return new CopyCall();
  }
  
  private static class CopyCall extends FileExtensionFunctionCall {
        
    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("rawtypes")
    public SequenceIterator<BooleanValue> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {      
      try {         
        File sourceFile = getFile(((StringValue) arguments[0].next()).getStringValue());
        File targetFile = getFile(((StringValue) arguments[1].next()).getStringValue());
        if (!sourceFile.exists()) {
          throw new FILE0001Exception(sourceFile);
        }
        if (sourceFile.isDirectory() && targetFile.isFile()) {
          throw new FILE0002Exception(sourceFile);
        }
        File sourceParent = sourceFile.getParentFile();
        if (!sourceParent.exists()) {
          throw new FILE0003Exception(sourceParent);
        }
        File targetDir = new File(targetFile, sourceFile.getName());        
        if (sourceFile.isFile() && targetDir.isDirectory()) {
          throw new FILE0004Exception(targetDir);
        }        
        if (sourceFile.isFile()) {          
          if (!targetFile.exists() || targetFile.isFile()) {
            FileUtils.copyFile(sourceFile, targetFile);
          } else if (targetFile.isDirectory()) {
            FileUtils.copyFileToDirectory(sourceFile, targetFile);
          }                    
        } else if (sourceFile.isDirectory()) {          
          if (!targetFile.exists()) {            
            FileUtils.copyDirectory(sourceFile, targetFile);           
          } else if (targetFile.isDirectory()) {
            FileUtils.copyDirectoryToDirectory(sourceFile, targetFile);
          }          
        }        
        return SingletonIterator.makeIterator(BooleanValue.TRUE);
      } catch (ExpectedFileException e) {
        throw e;
      } catch (Exception e) {
        throw new FILE9999Exception(e);
      }
    } 
  }
}