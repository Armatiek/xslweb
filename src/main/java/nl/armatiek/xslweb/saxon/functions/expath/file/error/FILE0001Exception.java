package nl.armatiek.xslweb.saxon.functions.expath.file.error;

import java.io.File;

public class FILE0001Exception extends ExpectedFileException {
  
  private static final long serialVersionUID = 1L;

  public FILE0001Exception(File file) {
    super(String.format("The specified path does not exist (%s)", file.getAbsolutePath()), "FILE0001");
  }

}
