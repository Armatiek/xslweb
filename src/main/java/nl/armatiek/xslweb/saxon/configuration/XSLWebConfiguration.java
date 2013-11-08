package nl.armatiek.xslweb.saxon.configuration;

import net.sf.saxon.Configuration;

public class XSLWebConfiguration extends Configuration {

  private static final long serialVersionUID = 1L;
  
  private static XSLWebConfiguration _instance = null;

  private XSLWebConfiguration() {    
    setURIResolver(null);
    setOutputURIResolver(null);
    setXIncludeAware(true);
    // registerExtensionFunction(new com.armatiek.infofuze.xslt.functions.request.Attribute());
  }

  private synchronized static void createInstance() {
    if (_instance == null) {
      _instance = new XSLWebConfiguration();
    }
  }

  public static XSLWebConfiguration getInstance() {
    if (_instance == null) {
      createInstance();
    }
    return _instance;
  }

}