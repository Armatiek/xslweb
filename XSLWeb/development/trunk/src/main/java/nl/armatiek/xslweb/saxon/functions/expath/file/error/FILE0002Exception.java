package nl.armatiek.xslweb.saxon.functions.expath.file.error;

import java.io.File;

public class FILE0002Exception extends ExpectedFileException {
  
  private static final long serialVersionUID = 1L;

  public FILE0002Exception(File file) {
    super(String.format("The specified path already exists (%s)", file.getAbsolutePath()), "FILE0002");
  }

}
