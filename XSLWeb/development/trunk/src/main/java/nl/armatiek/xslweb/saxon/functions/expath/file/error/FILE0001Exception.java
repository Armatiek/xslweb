package nl.armatiek.xslweb.saxon.functions.expath.file.error;

import net.sf.saxon.trans.XPathException;

public class FILE0001Exception extends XPathException {
  
  private static final long serialVersionUID = 1L;

  public FILE0001Exception(String path) {
    super(String.format("The specified path does not exist (%s)", path), "FILE0001");
  }

}
