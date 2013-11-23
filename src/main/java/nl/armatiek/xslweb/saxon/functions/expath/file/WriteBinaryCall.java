package nl.armatiek.xslweb.saxon.functions.expath.file;

import java.io.File;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0003Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0004Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE9999Exception;

import org.apache.commons.io.FileUtils;

public class WriteBinaryCall extends FileExtensionFunctionCall {
  
  private static final long serialVersionUID = 1L;
  
  private boolean append;
  
  public WriteBinaryCall(boolean append) {
    this.append = append;
  }
  
  @SuppressWarnings("rawtypes")
  public SequenceIterator<BooleanValue> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {      
    try {                      
      File file = getFile(((StringValue) arguments[0].next()).getStringValue());
      File parentFile = file.getParentFile();
      if (!parentFile.exists()) {
        throw new FILE0003Exception(parentFile);
      }     
      if (file.isDirectory()) {
        throw new FILE0004Exception(file);
      }
      FileUtils.writeByteArrayToFile(file, ((Base64BinaryValue) arguments[1].next()).getBinaryValue(), append);                
      return SingletonIterator.makeIterator(BooleanValue.TRUE);
    } catch (Exception e) {
      throw new FILE9999Exception(e);
    }
  } 
}
