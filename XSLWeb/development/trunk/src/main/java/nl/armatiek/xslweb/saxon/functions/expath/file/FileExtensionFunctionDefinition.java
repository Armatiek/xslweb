package nl.armatiek.xslweb.saxon.functions.expath.file;

import net.sf.saxon.lib.ExtensionFunctionDefinition;

public abstract class FileExtensionFunctionDefinition extends ExtensionFunctionDefinition {

  @Override
  public boolean hasSideEffects() {    
    return true;
  }

}