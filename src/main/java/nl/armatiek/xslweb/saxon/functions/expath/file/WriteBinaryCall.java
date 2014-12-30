package nl.armatiek.xslweb.saxon.functions.expath.file;

import java.io.File;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FileException;

import org.apache.commons.io.FileUtils;

public class WriteBinaryCall extends FileExtensionFunctionCall {
  
  private boolean append;
  
  public WriteBinaryCall(boolean append) {
    this.append = append;
  }
  
  @Override
  public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {      
    try {                      
      File file = getFile(((StringValue) arguments[0].head()).getStringValue());
      File parentFile = file.getParentFile();
      if (!parentFile.exists()) {
        throw new FileException(String.format("Parent directory \"%s\" does not exist", 
            parentFile.getAbsolutePath()), FileException.ERROR_PATH_NOT_DIRECTORY);
      }     
      if (file.isDirectory()) {
        throw new FileException(String.format("Path \"%s\" points to a directory", 
            file.getAbsolutePath()), FileException.ERROR_PATH_IS_DIRECTORY);
      }
      FileUtils.writeByteArrayToFile(file, ((Base64BinaryValue) arguments[1].head()).getBinaryValue(), append);                
      return EmptySequence.getInstance();
    } catch (Exception e) {
      throw new FileException("Other file error", e, FileException.ERROR_IO);
    }
  } 
}
