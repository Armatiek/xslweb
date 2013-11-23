package nl.armatiek.xslweb.saxon.functions.expath.file;

import net.sf.saxon.lib.ExtensionFunctionDefinition;

public abstract class FileExtensionFunctionDefinition extends ExtensionFunctionDefinition {

  private static final long serialVersionUID = 1L;

  @Override
  public boolean hasSideEffects() {    
    return true;
  }

}