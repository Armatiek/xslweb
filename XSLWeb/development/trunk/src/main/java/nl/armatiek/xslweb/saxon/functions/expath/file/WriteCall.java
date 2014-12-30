package nl.armatiek.xslweb.saxon.functions.expath.file;

import java.io.File;
import java.io.OutputStream;
import java.util.Properties;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FileException;

import org.apache.commons.io.FileUtils;

public class WriteCall extends FileExtensionFunctionCall {

  private boolean append;
  
  public WriteCall(boolean append) {
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
      Properties props = null;
      if (arguments.length > 2) {
        props = getOutputProperties((NodeInfo) arguments[2].head());
      }
      OutputStream os = FileUtils.openOutputStream(file, append);
      try {
        serialize(arguments[1], os, props);
      } finally {
        os.close();
      }
      return EmptySequence.getInstance();
    } catch (Exception e) {
      throw new FileException("Other file error", e, FileException.ERROR_IO);
    }
  }
}