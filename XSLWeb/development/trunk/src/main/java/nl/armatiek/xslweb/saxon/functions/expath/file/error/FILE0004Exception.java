package nl.armatiek.xslweb.saxon.functions.expath.file.error;

import java.io.File;

public class FILE0004Exception extends ExpectedFileException {
  
  private static final long serialVersionUID = 1L;
  
  public FILE0004Exception(File file) {
    super(String.format("The specified path points to a directory (%s)", file.getAbsolutePath()), "FILE0004");
  }
  
  public FILE0004Exception(File file, String message) {
    super(message, "FILE0004");
  }
  
}
