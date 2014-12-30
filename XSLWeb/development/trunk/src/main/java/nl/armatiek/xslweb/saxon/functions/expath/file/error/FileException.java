package nl.armatiek.xslweb.saxon.functions.expath.file.error;

import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;

public class FileException extends XPathException {
  
  private static final long serialVersionUID = 1L;
  
  public static final String ERROR_PATH_NOT_EXIST = "not-found";
  public static final String ERROR_PATH_EXISTS = "exists";
  public static final String ERROR_PATH_NOT_DIRECTORY = "no-dir";
  public static final String ERROR_PATH_IS_DIRECTORY = "is-dir";
  public static final String ERROR_UNKNOWN_ENCODING = "unknown-encoding";
  public static final String ERROR_INDEX_OUT_OF_BOUNDS = "out-of-range";
  public static final String ERROR_IO = "io-error";

  public FileException(String message, String code) {
    super(message);
    setErrorCodeQName(new StructuredQName("file", "http://expath.org/ns/file", code));
  }
  
  public FileException(String message, Exception cause, String code) {
    super(message, cause);
    setErrorCodeQName(new StructuredQName("file", "http://expath.org/ns/file", code));
  }

}
