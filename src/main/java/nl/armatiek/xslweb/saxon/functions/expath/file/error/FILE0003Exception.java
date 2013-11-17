package nl.armatiek.xslweb.saxon.functions.expath.file.error;

import java.io.File;

import net.sf.saxon.trans.XPathException;

public class FILE0003Exception extends XPathException {
  
  private static final long serialVersionUID = 1L;

  public FILE0003Exception(File file) {
    super(String.format("The specified path does not point to a directory (%s)", file.getAbsolutePath()), "FILE0003");
  }

}
