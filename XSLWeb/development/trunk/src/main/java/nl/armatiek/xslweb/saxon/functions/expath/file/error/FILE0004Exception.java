package nl.armatiek.xslweb.saxon.functions.expath.file.error;

import net.sf.saxon.trans.XPathException;

public class FILE0004Exception extends XPathException {
  
  private static final long serialVersionUID = 1L;

  public FILE0004Exception(String path) {
    super(String.format("The specified path points to a directory (%s)", path), "FILE0004");
  }

}
