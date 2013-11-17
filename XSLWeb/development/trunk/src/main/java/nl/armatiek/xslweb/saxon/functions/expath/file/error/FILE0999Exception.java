package nl.armatiek.xslweb.saxon.functions.expath.file.error;

import net.sf.saxon.trans.XPathException;

public class FILE0999Exception extends XPathException {
  
  private static final long serialVersionUID = 1L;

  public FILE0999Exception(Exception e) {
    super("A generic file system error occurred", "FILE0999");
  }
  
}