package nl.armatiek.xslweb.web.listener;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class WebAppEventListener implements ServletContextListener {

  private final static Logger logger = LoggerFactory.getLogger(WebAppEventListener.class);

  public void contextInitialized(ServletContextEvent sce) {        
    try {
      Context context = Context.getInstance();
      context.setServletContext(sce.getServletContext());    
      ClassLoader effectiveClassLoader = getClass().getClassLoader();
      if (effectiveClassLoader instanceof URLClassLoader) {
        URL[] classPathURLs = ((URLClassLoader) effectiveClassLoader).getURLs();
        ArrayList<String> classPath = new ArrayList<String>();
        for (int i=0; i<classPathURLs.length; i++) {
          classPath.add(new File(classPathURLs[i].toURI()).getAbsolutePath());
        }
        context.setClassPath(String.join(Definitions.CLASSPATH_SEPARATOR, classPath));
      }
      context.open();              
    } catch (Exception e) {
      logger.error("Could not open XSLWeb Context", e);
    }           
  }

  public void contextDestroyed(ServletContextEvent sce) {    
    try { 
      Context.getInstance().close();
    } catch (Exception e) {
      logger.error("Could not close XSLWeb Context", e);
    }    
  }
}