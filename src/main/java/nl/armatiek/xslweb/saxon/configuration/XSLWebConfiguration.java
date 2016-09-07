package nl.armatiek.xslweb.saxon.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.clapper.util.classutil.AbstractClassFilter;
import org.clapper.util.classutil.AndClassFilter;
import org.clapper.util.classutil.ClassFilter;
import org.clapper.util.classutil.ClassFinder;
import org.clapper.util.classutil.ClassInfo;
import org.clapper.util.classutil.ClassLoaderBuilder;
import org.clapper.util.classutil.NotClassFilter;
import org.clapper.util.classutil.SubclassClassFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.lib.FeatureKeys;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.utils.XSLWebUtils;

public class XSLWebConfiguration {
  
  private static final Logger logger = LoggerFactory.getLogger(XSLWebConfiguration.class);

  private XSLWebInitializer initializer;
  private Configuration config;
  private List<ExtensionFunctionDefinition> extensionFunctions = new ArrayList<ExtensionFunctionDefinition>();
  
  public XSLWebConfiguration(WebApp webApp) throws Exception {    
    this.config = createConfiguration();
    this.initializer = new XSLWebInitializer();
    this.initializer.initialize(this.config);
    addCustomExtensionFunctions(webApp);
  }
  
  public Configuration getConfiguration() {
    return this.config;
  }
  
  private Configuration createConfiguration() throws IOException {
    File licenseFile = new File(Context.getInstance().getHomeDir(), "config/saxon-license.lic");
    String className = "net.sf.saxon.Configuration";
    if (licenseFile.isFile()) {
      Properties props = XSLWebUtils.readProperties(licenseFile);
      String edition = props.getProperty("Edition", "-");
      if (edition.equals("PE")) {
        className = "com.saxonica.config.ProfessionalConfiguration";
      } else if (edition.equals("EE")) {
        className = "com.saxonica.config.EnterpriseConfiguration";
      }
      Configuration config = Configuration.makeLicensedConfiguration(this.getClass().getClassLoader(), className);
      config.setConfigurationProperty(FeatureKeys.LICENSE_FILE_LOCATION, licenseFile.getAbsolutePath());
    } else {
      config = new Configuration();
    }
    return config;
  }
  
  private void addCustomExtensionFunctions(WebApp webApp) throws Exception {            
    File libDir = new File(webApp.getHomeDir(), "lib");
    if (!libDir.isDirectory()) {
      return;
    }
    List<File> classPath = new ArrayList<File>();                
    classPath.addAll(FileUtils.listFiles(libDir, new WildcardFileFilter("*.jar"), DirectoryFileFilter.DIRECTORY));
    if (classPath.isEmpty() && !XSLWebUtils.hasSubDirectories(libDir)) {
      return;
    }
    classPath.add(libDir);
    Collection<File> saxonJars = FileUtils.listFiles(new File(Context.getInstance().getWebInfDir(), "lib"), 
        new WildcardFileFilter("*saxon*.jar", IOCase.INSENSITIVE), FalseFileFilter.INSTANCE);    
    classPath.addAll(saxonJars);
    
    logger.info("Initializing custom extension functions ...");
    
    ClassFinder finder = new ClassFinder();
    finder.add(classPath);    
    
    ClassFilter filter =
        new AndClassFilter(            
            // Must extend ExtensionFunctionDefinition class
            new SubclassClassFilter (ExtensionFunctionDefinition.class),
            // Must not be abstract
            new NotClassFilter (new AbstractClassFilter()));
    
    Collection<ClassInfo> foundClasses = new ArrayList<ClassInfo>();    
    finder.findClasses(foundClasses, filter);
    if (foundClasses.isEmpty()) {
      logger.info("No custom extension functions found.");
      return;
    }    
    ClassLoaderBuilder builder = new ClassLoaderBuilder();    
    builder.add(classPath);    
    ClassLoader classLoader = builder.createClassLoader();    
    for (ClassInfo classInfo : foundClasses) { 
      String className = classInfo.getClassName();
      if (initializer.isFunctionRegistered(className) || saxonJars.contains(classInfo.getClassLocation())) {
        continue;
      }      
      Class<?> clazz = classLoader.loadClass(className);
      logger.info(String.format("Adding custom extension function class \"%s\" ...", className));     
      registerExtensionFunction((ExtensionFunctionDefinition) clazz.newInstance());      
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