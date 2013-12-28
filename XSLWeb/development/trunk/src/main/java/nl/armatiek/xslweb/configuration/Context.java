package nl.armatiek.xslweb.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import nl.armatiek.xslweb.error.XSLWebException;
import nl.armatiek.xslweb.utils.XSLWebUtils;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Context {
  
  private static final Logger logger = LoggerFactory.getLogger(Context.class);
  
  private static Context _instance;
  
  private Map<String, WebApp> webApps = Collections.synchronizedMap(new HashMap<String, WebApp>());
  private FileAlterationMonitor monitor;
  private Schema webAppSchema;
  private Properties properties;
  private boolean developmentMode = false;
  private String contextPath;
  private String localHost; 
  private int port = 80;  
  private File homeDir;
  
  private Context() {
    try {
      initHomeDir();      
      initProperties();      
      initXMLSchemas();
      initFileAlterationObservers();
      initWebApps();
    } catch (Exception e) {
      throw new XSLWebException(e);
    }
  }
  
  /**
   * Returns the singleton Config instance.
   */
  public static synchronized Context getInstance() {
    if (_instance == null) {
      _instance = new Context();
    }
    return _instance;
  }

  public void open() throws Exception {
    logger.info("Opening XSLWeb Context ...");
    
    logger.info("Starting webapps file alteration monitor ...");
    monitor.start();
    
    logger.info("XSLWeb Context opened.");
  }
  
  public void close() throws Exception {
    logger.info("Closing XSLWeb Context ...");
    
    logger.info("Stopping webapps file alteration monitor ...");
    monitor.stop();
    
    logger.info("Closing webapps ...");
    for (WebApp app : webApps.values()) {
      app.close();      
    }
        
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
      javax.naming.Context c = new InitialContext();
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
    File propsFile = new File(homeDir, "config" + File.separatorChar + Definitions.FILENAME_PROPERTIES);
    this.properties = XSLWebUtils.readProperties(propsFile);                   
    developmentMode = Boolean.parseBoolean(properties.getProperty(Definitions.PROPERTYNAME_DEVELOPMENTMODE, "false"));
    port = Integer.parseInt(properties.getProperty(Definitions.PROPERTYNAME_PORT, "80"));    
    properties.put(Definitions.PROPERTYNAME_LOCALHOST, InetAddress.getLocalHost().getHostName());
    localHost = InetAddress.getLocalHost().getHostName();    
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
  
  private void onWebAppAltered(File file, String message, boolean createNew) {
    if (!file.isFile()) {
      return;
    }
    String webAppName = file.getParentFile().getName();
    logger.info(String.format(message, webAppName));        
    WebApp webApp = webApps.get(webAppName);       
    if (webApp != null) {
      logger.info(String.format("Stopping existing webapp \"%s\" ...", webAppName));
      try {       
        webApp.close();
        webApps.remove(webAppName);
      } catch (Exception e) {
        logger.error(String.format("Error stopping existing webapp \"%s\"", webAppName), e);
      }
    }
    if (!createNew) {
      return;
    }
    logger.info(String.format("Creating new webapp \"%s\" ...", webAppName));
    try {       
      webApp = new WebApp(file, webAppSchema, homeDir);
      webApps.put(webApp.getName(), webApp);  
      webApp.open();
    } catch (Exception e) {
      logger.error(String.format("Error creating new webapp \"%s\"", webAppName), e);
    }
  }
  
  private void initFileAlterationObservers() {
    File webAppsDir = new File(homeDir, "webapps");    
    IOFileFilter directories = FileFilterUtils.and(FileFilterUtils.directoryFileFilter(), HiddenFileFilter.VISIBLE);
    IOFileFilter files = FileFilterUtils.and(FileFilterUtils.fileFileFilter(), FileFilterUtils.nameFileFilter("webapp.xml"));
    IOFileFilter filter = FileFilterUtils.or(directories, files);    
    FileAlterationObserver webAppObserver = new FileAlterationObserver(webAppsDir, filter);
    webAppObserver.addListener(new FileAlterationListenerAdaptor() {

      @Override
      public void onFileCreate(File file) {
        onWebAppAltered(file, "New webapp \"%s\" detected", true);
      }

      @Override
      public void onFileChange(File file) {
        onWebAppAltered(file, "Change in webapp \"%s\" detected", true);
      }

      @Override
      public void onFileDelete(File file) {
        onWebAppAltered(file, "Deletion of webapp \"%s\" detected", true);
      }
      
    });
    
    monitor = new FileAlterationMonitor(10);
    monitor.addObserver(webAppObserver);    
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
        WebApp webApp = new WebApp(webAppFiles[0], webAppSchema, homeDir);
        webApps.put(webApp.getName(), webApp);  
        webApp.open();
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
  
  public String getContextPath() {
    return this.contextPath;
  }
  
  public void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }
  
}