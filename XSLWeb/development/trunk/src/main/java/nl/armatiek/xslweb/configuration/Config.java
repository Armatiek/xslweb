package nl.armatiek.xslweb.configuration;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;

import nl.armatiek.xslweb.error.XSLWebException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
  
  private static final Logger logger = LoggerFactory.getLogger(Config.class);
  
  private static Config _instance;
  
  private Properties properties;
  private boolean developmentMode = false;
  private String localHost; 
  private int port = 80;
  private String controllerXslPath;
  private File homeDir;
  
  private Config() {
    try {
      getHomeDir();      
      getProperties();      
    } catch (Exception e) {
      throw new XSLWebException(e);
    }
  }
  
  /**
   * Returns the singleton Config instance.
   */
  public static synchronized Config getInstance() {
    if (_instance == null) {
      _instance = new Config();
    }
    return _instance;
  }
  
  /**
   * Returns a file object denoting the Infofuze home directory
   * 
   * @throws FileNotFoundException
   */
  public File getHomeDir() {
    if (homeDir == null) {
      String home = null;
      // Try JNDI
      try {
        Context c = new InitialContext();
        home = (String) c.lookup("java:comp/env/" + Definitions.PROJECT_NAME + "/home");
        logger.info("Using JNDI infofuze.home: " + home);
      } catch (NoInitialContextException e) {
        logger.info("JNDI not configured for " + Definitions.PROJECT_NAME + " (NoInitialContextEx)");
      } catch (NamingException e) {
        logger.info("No /" + Definitions.PROJECT_NAME + "/home in JNDI");
      } catch( RuntimeException ex ) {
        logger.warn("Odd RuntimeException while testing for JNDI: " + ex.getMessage());
      } 
      
      // Now try system property
      if (home == null) {
        String prop = Definitions.PROJECT_NAME + ".home";
        home = System.getProperty(prop);
        if (home != null) {
          logger.info("Using system property " + prop + ": " + home);
        }
      }
       
      if (home == null) {
        String error = "FATAL: Could not find system property or JNDI for \"" + Definitions.PROJECT_NAME + ".infofuze.home\"";
        logger.error(error);
        throw new XSLWebException(error);
      }
      homeDir = new File(home);
      if (!homeDir.isDirectory()) {
        String error = "FATAL: Directory \"" + Definitions.PROJECT_NAME + ".infofuze.home\" not found";
        logger.error(error);
        throw new XSLWebException(error);
      }
    } 
    return homeDir;
  }
  
  public Properties getProperties() throws IOException {
    if (properties != null) {
      return properties;
    }    
    File file = new File(homeDir, "config" + File.separatorChar + Definitions.FILENAME_PROPERTIES);
    if (!file.isFile()) {
      throw new FileNotFoundException("Could not find properties file \"" + file.getAbsolutePath() + "\"");
    }
    Properties props = new Properties();
    InputStream is = new BufferedInputStream(new FileInputStream(file));
    try {
      props.load(is);
    } finally {
      is.close();
    }            
    developmentMode = Boolean.parseBoolean(props.getProperty(Definitions.PROPERTYNAME_DEVELOPMENTMODE, "false"));
    port = Integer.parseInt(props.getProperty(Definitions.PROPERTYNAME_PORT, "80"));    
    props.put(Definitions.PROPERTYNAME_LOCALHOST, InetAddress.getLocalHost().getHostName());
    localHost = InetAddress.getLocalHost().getHostName();
    if (!developmentMode) {
      this.properties = props;
    }    
    return props;
  }
  
  public boolean isDevelopmentMode() {
    return developmentMode;
  }
  
  public String getLocalHost() {
    return localHost;
  }
  
  public int getPort() {
    return port;
  }
  
  public String getControllerXslPath() {
    if (controllerXslPath != null) {
      return controllerXslPath;
    }
    String path = new File(getHomeDir(), "xsl" + File.separatorChar + Definitions.FILENAME_CONTROLLER_XSL).getAbsolutePath();
    if (!developmentMode) {
      this.controllerXslPath = path;
    } 
    return path;
  }

}