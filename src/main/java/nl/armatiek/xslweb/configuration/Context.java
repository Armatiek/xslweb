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
package nl.armatiek.xslweb.configuration;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletContext;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
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

import eu.medsea.mimeutil.MimeUtil;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.tree.util.DocumentNumberAllocator;
import nl.armatiek.xslweb.error.XSLWebException;
import nl.armatiek.xslweb.utils.XMLUtils;
import nl.armatiek.xslweb.utils.XSLWebUtils;

/**
 * Singleton holding all global objects such as the collection of WebApp objects, 
 * the FileMonitor, Scheduler and such.
 * 
 * @author Maarten Kroon
 */
public class Context {
  
  private static final Logger logger = LoggerFactory.getLogger(Context.class);
  
  private static Context _instance;
  
  private Map<String, WebApp> webApps = Collections.synchronizedMap(new HashMap<String, WebApp>());
  private Map<String, ArrayList<Attribute>> attributes = Collections.synchronizedMap(new HashMap<String, ArrayList<Attribute>>());
  private ServletContext servletContext;
  private String classPath;
  private FileAlterationMonitor monitor;
  private Schema webAppSchema;
  private Properties properties;
  private boolean parserHardening;
  private boolean trustAllCerts;
  private boolean webDAVEnable;
  private boolean debugEnable;
  private File webDAVRoot;
  private String contextPath;
  private File webInfDir; 
  private File homeDir;
  private File queueDir;
  private ScheduledExecutorService queueCleanupScheduler;
  private volatile boolean isOpen = false;
  private Date startTime;
  
  private Context() {
    startTime = new Date();
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
    /* Scan for duplicate classes on classpath: */
    /*
    logger.info("Scanning for duplicate classes ...");
    for (Entry<String, ResourceList> dup : new ClassGraph().scan().getAllResources().classFilesOnly().findDuplicatePaths()) {
      if (dup.getKey().contains("module-info.class")) {
        continue;
      }
      boolean nonFilePath = false;
      for (io.github.classgraph.Resource res : dup.getValue()) {
        if (!"file".equals(res.getURI().getScheme())) {
          nonFilePath = true;
          break;
        }
      }
      if (nonFilePath) {
        logger.warn("Duplicate class: " + dup.getKey());
        int i=1;
        for (io.github.classgraph.Resource res : dup.getValue()) {
          logger.warn("  {}: " + res.getURI(), i++);
        }
      }
    }
    */
    
    logger.info("Opening XSLWeb Context ...");
    
    initHomeDir();      
    initQueueDir();
    initProperties();
    initMimeUtil();
    initXMLSchemas();
    initFileAlterationObservers();
    initWebApps();
    
    logger.info("Starting webapps file alteration monitor ...");
    monitor.start();
  
    isOpen = true;
    logger.info("XSLWeb Context opened.");
  }
  
  public void close() throws Exception {
    logger.info("Closing XSLWeb Context ...");
    
    isOpen = false;
    
    logger.info("Shutting down queue cleanup scheduler ...");
    if (queueCleanupScheduler != null)
      queueCleanupScheduler.shutdownNow();
    
    logger.info("Stopping webapps file alteration monitor ...");    
    if (monitor != null)
      monitor.stop();
    
    logger.info("Closing webapps ...");
    for (WebApp app : webApps.values()) {
      app.close();      
    }
    
    logger.info("Unregistering MIME detectors ...");
    MimeUtil.unregisterMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
    MimeUtil.unregisterMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
        
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
  
  private void initQueueDir() {
    queueDir = new File(homeDir, "queue");    
    queueCleanupScheduler = Executors.newScheduledThreadPool(1);
    queueCleanupScheduler.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        if (!queueDir.isDirectory())
          return;
        long cutoff = System.currentTimeMillis() - (10 * 60 * 1000); // 10 minutes old
        File[] oldFiles = queueDir.listFiles((FileFilter) new AgeFileFilter(cutoff));
        for (File file : oldFiles) {
          if (!FileUtils.deleteQuietly(file))
            logger.error("Could not delete stale file \"" + file.getAbsolutePath() + "\" in queue folder");
        }   
      }
    }, 10, 10, TimeUnit.MINUTES);
  }
  
  private void initProperties() throws Exception {
    File propsFile = new File(homeDir, "config" + File.separatorChar + Definitions.FILENAME_PROPERTIES);
    this.properties = XSLWebUtils.readProperties(propsFile);
    this.trustAllCerts = new Boolean(properties.getProperty(Definitions.PROPERTYNAME_TRUST_ALL_CERTS, "false"));
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
    this.debugEnable = new Boolean(this.properties.getProperty(Definitions.PROPERTYNAME_DEBUG_ENABLE, "false"));
    this.webDAVEnable = new Boolean(this.properties.getProperty(Definitions.PROPERTYNAME_WEBDAV_ENABLE, "false"));
    this.webDAVRoot = new File(this.properties.getProperty(Definitions.PROPERTYNAME_WEBDAV_ROOT, this.getHomeDir().getAbsolutePath()));
    if (this.webDAVEnable && !this.webDAVRoot.isDirectory()) {
      logger.error("WebDAV root directory {} not found or is not a directory", this.webDAVRoot.getAbsolutePath());
    }
  }
  
  private void initMimeUtil() {
    logger.info("Registering MIME detectors ...");
    MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
    MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
  }
  
  private void initXMLSchemas() throws Exception {   
    SchemaFactory factory = XMLUtils.getNonSaxonJAXPSchemaFactory();
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
      Map<String, ArrayList<Attribute>> attributes = null;
      NamePool namePool = null;
      DocumentNumberAllocator documentNumberAllocator = null;
      if (webApp != null) {
        logger.info(String.format("Stopping existing webapp \"%s\" ...", webAppName));
        try {
          attributes = webApp.getAttributes();
          // Store the namepool and documentallocator so they can be set to the new configuration. 
          // Otherwisethe  new and old configurations are not compatible, what could lead to problems 
          // with cached nodes etc:
          namePool = webApp.getConfiguration().getNamePool();
          documentNumberAllocator = webApp.getConfiguration().getDocumentNumberAllocator();
          webApp.close();
          // webApps.remove(webAppName);
        } catch (Exception e) {
          logger.error(String.format("Error stopping existing webapp \"%s\"", webAppName), e);
        }
        logger.info("Executing handler for webapp-reload event ...");
        webApp.executeEvent(Definitions.EVENTNAME_WEBAPPRELOAD);
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
        if (namePool != null) {
          webApp.getConfiguration().setNamePool(namePool);
        }
        if (documentNumberAllocator != null) {
          webApp.getConfiguration().setDocumentNumberAllocator(documentNumberAllocator);
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
    
  public ServletContext getServletContext() {
    return this.servletContext;
  }
  
  public File getHomeDir() {
    return this.homeDir;
  }
  
  public File getQueueDir() {
    return this.queueDir;
  }
  
  public Properties getProperties() {     
    return this.properties;    
  }
  
  public boolean getParserHardening() {     
    return this.parserHardening;    
  }
  
  public Date getStartTime() {
    return startTime;
  }
  
  public boolean getTrustAllCerts() {     
    return this.trustAllCerts; 
  }
  
  public boolean getDebugEnable() {
    return this.debugEnable;
  }
  
  public boolean getWebDAVEnable() {
    return this.webDAVEnable;
  }
  
  public File getWebDAVRoot() {
    return this.webDAVRoot;
  }
  
  public Schema getWebAppSchema() {
    return this.webAppSchema;
  }
  
  public WebApp getWebApp(String path) {
    String name;
    if (StringUtils.isBlank(path)) {
      name = "ROOT";
    } else {
      name = StringUtils.substringBefore(path.substring(1), "/");
    }
    WebApp webApp = webApps.get(name);
    if (webApp == null) {
      webApp = webApps.get("ROOT");
    }
    return webApp;    
  }
  
  public Collection<WebApp> getWebApps() {
    return webApps.values();
  }
  
  public void setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
    this.contextPath = servletContext.getContextPath();
    this.webInfDir = new File(servletContext.getRealPath("/WEB-INF"));
  }
  
  public String getContextPath() {
    return this.contextPath;
  }
  
  public void setClassPath(String classPath) {
    this.classPath = classPath;
  }
  
  public String getClassPath() {
    return this.classPath;
  }
  
  public File getWebInfDir() {
    return this.webInfDir;
  }
  
  public ArrayList<Attribute> getAttribute(String name) {
    return attributes.get(name);
  }
  
  public void removeAttribute(String name) {
    attributes.remove(name);
  }
  
  public void setAttribute(String name, ArrayList<Attribute> attrs) {
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