package nl.armatiek.xslweb.saxon.configuration;

import javax.xml.transform.TransformerException;

import net.sf.saxon.Configuration;

public class XSLWebConfiguration extends Configuration {

  private static final long serialVersionUID = 1L;
  
  public XSLWebConfiguration() throws TransformerException {
    XSLWebInitializer initializer = new XSLWebInitializer();
    initializer.initialize(this);            
  }

}