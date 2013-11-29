package nl.armatiek.xslweb.saxon.functions.expath.file.error;

import net.sf.saxon.trans.XPathException;

public abstract class ExpectedFileException extends XPathException {
  
  private static final long serialVersionUID = 1L;
  
  public ExpectedFileException(String message) {
    super(message); 
  }
  
  public ExpectedFileException(String message, String errorCode) {
    super(message, errorCode); 
  }
  
}
