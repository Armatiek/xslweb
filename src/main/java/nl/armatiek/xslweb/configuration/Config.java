package nl.armatiek.xslweb.configuration;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import nl.armatiek.xslweb.error.XSLWebException;

import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
  
  private static final Logger logger = LoggerFactory.getLogger(Config.class);
  
  private static Config _instance;
  
  private Map<String, WebApp> webApps = Collections.synchronizedMap(new HashMap<String, WebApp>());
  private Scheduler quartzScheduler;
  private Schema webAppSchema;
  private Properties properties;
  private boolean developmentMode = false;
  private String localHost; 
  private int port = 80;  
  private File homeDir;
  
  private Config() {
    try {
      initHomeDir();      
      initProperties();
      initScheduler();
      initXMLSchemas();
      initWebApps();
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
  
  public void open() throws Exception {
    logger.info("Opening XSLWeb Context ...");
    logger.info("Starting Quartz scheduler ...");
    quartzScheduler.start();
    logger.info("Started Quartz scheduler.");
    logger.info("XSLWeb Context opened.");
  }
  
  public void close() throws SchedulerException {
    logger.info("Closing XSLWeb Context ...");
    logger.info("Shutting down Quartz scheduler ...");
    quartzScheduler.shutdown(!Config.getInstance().isDevelopmentMode());
    logger.info("Shutdown Quartz scheduler complete.");
    logger.info("XSLWeb Context closed.");
  }
  
  /**
   * Returns a file object denoting the Infofuze home directory
   * 
   * @throws FileNotFoundException
   */
  private void initHomeDir() {    
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
  
  private void initProperties() throws IOException {       
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
  }
  
  private void initScheduler() throws Exception {
    File quartzFile = new File(homeDir, "config" + File.separatorChar + Definitions.FILENAME_QUARTZ);
    if (!quartzFile.isFile()) {
      throw new FileNotFoundException("Could not find quartz properties file \"" + quartzFile.getAbsolutePath() + "\"");
    }    
    logger.info("Initializing Quartz scheduler using properties file \"" + quartzFile.getAbsolutePath() + "\" ...");
    SchedulerFactory sf = new StdSchedulerFactory(quartzFile.getAbsolutePath());
    quartzScheduler = sf.getScheduler();
  }
  
  private void initXMLSchemas() throws Exception {   
    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    File schemaFile = new File(homeDir, "config/xsd/xslweb/webapp.xsd");
    if (!schemaFile.isFile()) {
      logger.warn(String.format("XML Schema \"%s\" not found", schemaFile.getAbsolutePath()));
    } else {
      webAppSchema = factory.newSchema(schemaFile);
    }
  }
  
  private void initWebApps() throws Exception {
    File webAppsDir = new File(homeDir, "webapps");
    File[] dirs = webAppsDir.listFiles();
    for (File dir : dirs) {
      File[] webAppFiles = dir.listFiles((FilenameFilter) new NameFileFilter("webapp.xml"));
      if (webAppFiles.length == 0) {
        continue;
      }
      File file = webAppFiles[0];
      try {       
        WebApp webApp = new WebApp(webAppFiles[0], webAppSchema);
        webApps.put(webApp.getName(), webApp);     
      } catch (Exception e) {
        logger.error(String.format("Error creating webapp \"%s\"", file.getAbsolutePath()), e);
      }
    }    
  }
  
  public File getHomeDir() {
    return this.homeDir;
  }
  
  public Properties getProperties() {
    if (developmentMode) {
      try {
        initProperties();
      } catch (Exception e) {
        throw new XSLWebException("Error reading properties", e);
      }
    }    
    return this.properties;    
  }
  
  public Scheduler getScheduler() {
    return quartzScheduler;
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
  
  public WebApp getWebApp(String path) {
    String name = StringUtils.substringBefore(path.substring(1), "/");
    WebApp webApp = webApps.get(name);
    if (webApp == null) {
      webApp = webApps.get("root");
    }
    return webApp;    
  }
  
}