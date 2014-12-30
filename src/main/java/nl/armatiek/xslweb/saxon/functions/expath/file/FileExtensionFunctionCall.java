package nl.armatiek.xslweb.saxon.functions.expath.file;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

public abstract class FileExtensionFunctionCall extends ExtensionFunctionCall {

  protected File getFile(String path) throws URISyntaxException {
    return (path.startsWith("file:")) ? new File(new URI(path)) : new File(path);
  }

}
