package nl.armatiek.xslweb.saxon.configuration;

import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.utils.XMLUtils;
import nl.armatiek.xslweb.utils.XSLWebUtils;

public class XSLWebConfiguration {
  
  private static final Logger logger = LoggerFactory.getLogger(XSLWebConfiguration.class);

  private XSLWebInitializer initializer;
  private Configuration config;
  private List<ExtensionFunctionDefinition> extensionFunctions = new ArrayList<ExtensionFunctionDefinition>();
  
  public XSLWebConfiguration(WebApp webApp, Node saxonConfigNode, String systemId) throws Exception {    
    this.config = createConfiguration(saxonConfigNode, systemId);
    this.initializer = new XSLWebInitializer();
    this.initializer.initialize(this.config);
    addCustomExtensionFunctions(webApp);
  }
  
  public Configuration getConfiguration() {
    return this.config;
  }
  
  private Configuration createConfiguration(Node saxonConfigNode, String systemId) throws Exception {
    Configuration config;
    if (saxonConfigNode == null)
      config = new Configuration();
    else {
      Source configSource = new StreamSource(new StringReader(XMLUtils.nodeToString(saxonConfigNode)), systemId);
      config = Configuration.readConfiguration(configSource);
    }
    
    logger.info("Creating Saxon " + config.getEditionCode() + " configuration ...");
    return config;
  }
  
  private void addCustomExtensionFunctions(WebApp webApp) throws Exception {            
    File libDir = new File(webApp.getHomeDir(), "lib");
    if (!libDir.isDirectory()) {
      return;
    }
    List<File> jarPaths = new ArrayList<File>();                
    jarPaths.addAll(FileUtils.listFiles(libDir, new WildcardFileFilter("*.jar"), DirectoryFileFilter.DIRECTORY));
    if (jarPaths.isEmpty() && !XSLWebUtils.hasSubDirectories(libDir)) {
      return;
    }
    jarPaths.add(libDir);
    Collection<File> saxonJars = FileUtils.listFiles(new File(Context.getInstance().getWebInfDir(), "lib"), 
        new WildcardFileFilter("*saxon*.jar", IOCase.INSENSITIVE), FalseFileFilter.INSTANCE);    
    // jarPaths.addAll(saxonJars);
    
    logger.info("Initializing custom extension functions ...");
    int count = 0;
    
    URL[] urls = new URL[jarPaths.size()];
    for (int i=0; i<jarPaths.size(); i++) {
      urls[i] = jarPaths.get(i).toURI().toURL();
    }
    URLClassLoader childClassLoader = new URLClassLoader(urls, this.getClass().getClassLoader());
    
    ClassInfoList extensionFunctionClasses = new ClassGraph().enableAllInfo().overrideClassLoaders(childClassLoader).scan().getSubclasses(ExtensionFunctionDefinition.class);
    for (ClassInfo classInfo: extensionFunctionClasses) {
      if (classInfo.isAbstract()) {
        continue;
      }
      String className = classInfo.getName();
      if (initializer.isFunctionRegistered(className) || saxonJars.contains(classInfo.getClasspathElementFile()) || className.endsWith("DynamicExtensionFunctionDefinition")) {
        continue;
      }  
      count++;
      logger.info(String.format("Adding custom extension function class \"%s\" ...", className)); 
      Class<?> clazz = classInfo.loadClass();
      registerExtensionFunction((ExtensionFunctionDefinition) clazz.getDeclaredConstructor().newInstance());
    }
    if (count == 0) {
      logger.info("No custom extension functions found.");
    }
    
  }

  public void registerExtensionFunction(ExtensionFunctionDefinition function) {    
    config.registerExtensionFunction(function);
    if (extensionFunctions != null) {
      extensionFunctions.add(function);
    }
  }
  
  public Iterator<ExtensionFunctionDefinition> getRegisteredExtensionFunctions() {
    return extensionFunctions.iterator();
  }

}