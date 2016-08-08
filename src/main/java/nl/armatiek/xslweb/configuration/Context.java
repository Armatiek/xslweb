package nl.armatiek.xslweb.configuration;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletContext;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.ehcache.CacheManager;
import nl.armatiek.xslweb.error.XSLWebException;
import nl.armatiek.xslweb.utils.XSLWebUtils;

/**
 * 
 * 
 * @author Maarten
 */
public class Context {
  
  private static final Logger logger = LoggerFactory.getLogger(Context.class);
  
  private static Context _instance;
  
  private Map<String, WebApp> webApps = Collections.synchronizedMap(new HashMap<String, WebApp>());
  private Map<String, Collection<Attribute>> attributes = Collections.synchronizedMap(new HashMap<String, Collection<Attribute>>());
  private CacheManager cacheManager;
  private ServletContext servletContext;
  private FileAlterationMonitor monitor;
  private Schema webAppSchema;
  private Properties properties;
  private boolean parserHardening;
  private String contextPath;
  private File webInfDir; 
  private File homeDir;
  private volatile boolean isOpen = false;
  
  private Context() { }
  
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
    
    initHomeDir();      
    initProperties();      
    initXMLSchemas();
    initFileAlterationObservers();
    initCacheManager();
    initWebApps();
    
    logger.info("Starting webapps file alteration monitor ...");
    monitor.start();
  
    isOpen = true;
    logger.info("XSLWeb Context opened.");
  }
  
  public void close() throws Exception {
    logger.info("Closing XSLWeb Context ...");
    
    isOpen = false;
    
    logger.info("Stopping webapps file alteration monitor ...");    
    monitor.stop();
    
    logger.info("Closing webapps ...");
    for (WebApp app : webApps.values()) {
      app.close();      
    }
    
    logger.info("Shutting down cache manager ...");
    cacheManager.shutdown();
        
    logger.info("XSLWeb Context closed.");
  }
  
  /**
   * Returns a file object denoting the XSLWeb home directory
   * 
   * @throws FileNotFoundException
   */
  private void initHomeDir() {    
    String home = null;
    // Try JNDI
    try {
      javax.naming.Context c = new InitialContext();
      home = (String) c.lookup("java:comp/env/" + Definitions.PROJECT_NAME + "/home");
      logger.info("Using JNDI xslweb.home: " + home);
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
      String error = "FATAL: Could not find system property or JNDI for \"" + Definitions.PROJECT_NAME + ".home\"";
      logger.error(error);
      throw new XSLWebException(error);
    }
    homeDir = new File(home);
    if (!homeDir.isDirectory()) {
      String error = "FATAL: Directory \"" + Definitions.PROJECT_NAME + ".home\" not found";
      logger.error(error);
      throw new XSLWebException(error);
    }    
  }
  
  private void initProperties() throws Exception {
    File propsFile = new File(homeDir, "config" + File.separatorChar + Definitions.FILENAME_PROPERTIES);
    this.properties = XSLWebUtils.readProperties(propsFile);
    boolean trustAllCerts = new Boolean(properties.getProperty(Definitions.PROPERTYNAME_TRUST_ALL_CERTS, "false")).booleanValue();
    if (trustAllCerts) {
      TrustManager[] trustAllCertsManager = new TrustManager[] { new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
      }};
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCertsManager, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }
    this.parserHardening = new Boolean(this.properties.getProperty(Definitions.PROPERTYNAME_PARSER_HARDENING, "false"));
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
    
  public void reloadWebApp(File webAppDefFile, boolean createNew) {        
    try {
      Thread.sleep(1000);     
    } catch (Exception e) { }
    if (!webAppDefFile.isFile()) {
      return;
    }
    synchronized(webApps) {
      String webAppName = webAppDefFile.getParentFile().getName();             
      WebApp webApp = webApps.get(webAppName);
      Map<String, Collection<Attribute>> attributes = null;
      if (webApp != null) {
        logger.info(String.format("Stopping existing webapp \"%s\" ...", webAppName));
        try {
          attributes = webApp.getAttributes();
          webApp.close();
          // webApps.remove(webAppName);
        } catch (Exception e) {
          logger.error(String.format("Error stopping existing webapp \"%s\"", webAppName), e);
        }
      }
      if (!createNew) {
        return;
      }
      logger.info(String.format("Creating new webapp \"%s\" ...", webAppName));
      try {       
        webApp = new WebApp(webAppDefFile);
        if (attributes != null) {
          webApp.setAttributes(attributes);
        }
        webApp.open();
        webApps.put(webAppName, webApp);
      } catch (Exception e) {
        logger.error(String.format("Error creating new webapp \"%s\"", webAppName), e);
      }      
    }    
  }
  
  private void initFileAlterationObservers() {
    File webAppsDir = new File(homeDir, "webapps");        
    IOFileFilter webAppFiles = FileFilterUtils.and(FileFilterUtils.fileFileFilter(), FileFilterUtils.nameFileFilter("webapp.xml"));    
    IOFileFilter filter = FileFilterUtils.or(FileFilterUtils.directoryFileFilter(), webAppFiles);    
    FileAlterationObserver webAppObserver = new FileAlterationObserver(webAppsDir, filter);
    webAppObserver.addListener(new FileAlterationListenerAdaptor() {

      @Override
      public void onFileCreate(File file) {
        logger.info("New webapp detected ..."); 
        reloadWebApp(file, true);
      }

      @Override
      public void onFileChange(File file) {
        logger.info("Change in webapp definition detected ...");
        reloadWebApp(file, true);
      }

      @Override
      public void onFileDelete(File file) {
        logger.info("Deletion of webapp detected ...");
        reloadWebApp(file, true);
      }
      
    });
    
    monitor = new FileAlterationMonitor(3000);
    monitor.addObserver(webAppObserver);    
  }
  
  private void initWebApps() throws Exception {
    File webAppsDir = new File(homeDir, "webapps");    
    File[] dirs = webAppsDir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
    for (File dir : dirs) {
      File[] webAppFiles = dir.listFiles((FilenameFilter) new NameFileFilter("webapp.xml"));
      if (webAppFiles.length == 0) {
        continue;
      }
      File file = webAppFiles[0];
      try {       
        WebApp webApp = new WebApp(webAppFiles[0]);
        webApps.put(webApp.getName(), webApp);  
        webApp.open();
      } catch (Exception e) {
        logger.error(String.format("Error creating webapp \"%s\"", file.getAbsolutePath()), e);
      }
    }    
  }
  
  private void initCacheManager() {
    logger.info("Initializing cache manager ...");
    File confFile = new File(getHomeDir(), "config" + File.separatorChar + Definitions.FILENAME_EHCACHE);
    if (confFile.isFile()) {
      cacheManager = new CacheManager(confFile.getAbsolutePath());
    } else {
      logger.error("Could not load cache manager configuration file \"" + confFile.getAbsolutePath() + "\"");
      cacheManager = CacheManager.getInstance();      
    }    
  }
  
  public CacheManager getCacheManager() {
    return cacheManager;
  }
  
  public ServletContext getServletContext() {
    return this.servletContext;
  }
  
  public File getHomeDir() {
    return this.homeDir;
  }
  
  public Properties getProperties() {     
    return this.properties;    
  }
  
  public boolean getParserHardening() {     
    return this.parserHardening;    
  }
  
  public Schema getWebAppSchema() {
    return this.webAppSchema;
  }
  
  public WebApp getWebApp(String path) {
    String name = StringUtils.substringBefore(path.substring(1), "/");
    WebApp webApp = webApps.get(name);
    if (webApp == null) {
      webApp = webApps.get("ROOT");
    }
    return webApp;    
  }
  
  public void setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
    this.contextPath = servletContext.getContextPath();
    this.webInfDir = new File(servletContext.getRealPath("/WEB-INF"));
  }
  
  public String getContextPath() {
    return this.contextPath;
  }
  
  public File getWebInfDir() {
    return this.webInfDir;
  }
  
  public Collection<Attribute> getAttribute(String name) {
    return attributes.get(name);
  }
  
  public void removeAttribute(String name) {
    attributes.remove(name);
  }
  
  public void setAttribute(String name, Collection<Attribute> attrs) {
    if (attrs != null) {
      attributes.put(name, attrs);
    } else {
      attributes.remove(name);
    }
  }
  
  public boolean isOpen() {
    return isOpen;
  }
  
}