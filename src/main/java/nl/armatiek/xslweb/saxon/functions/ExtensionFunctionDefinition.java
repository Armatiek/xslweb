package nl.armatiek.xslweb.saxon.functions;

import net.sf.saxon.Configuration;

public abstract class ExtensionFunctionDefinition extends net.sf.saxon.lib.ExtensionFunctionDefinition {

  protected Configuration configuration;
  
  public ExtensionFunctionDefinition(Configuration configuration) {
    this.configuration = configuration;
  }
  
}