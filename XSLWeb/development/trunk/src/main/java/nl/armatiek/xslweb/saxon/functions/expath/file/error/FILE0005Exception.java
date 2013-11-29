package nl.armatiek.xslweb.saxon.functions.expath.file.error;


public class FILE0005Exception extends ExpectedFileException {
  
  private static final long serialVersionUID = 1L;

  public FILE0005Exception(String encoding) {
    super(String.format("The specified encoding is not supported (%s)", encoding), "FILE0005");
  }

}
