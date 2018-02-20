package nl.armatiek.xslweb.utils;

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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang3.StringUtils;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.value.ObjectValue;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.Parameter;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.error.XSLWebException;

/**
 * Miscellaneous XSLWeb specific helper methods.
 *
 * @author Maarten
 */
public class XSLWebUtils {
  
  private static Pattern variablesPattern = Pattern.compile("\\$\\{(.+?)\\}");
  
  public static String resolveProperties(String sourceString, Properties props) {
    if (sourceString == null) {
      return null;
    }
    Matcher m = variablesPattern.matcher(sourceString);
    StringBuffer result = new StringBuffer();
    while (m.find()) {
      String variable = m.group(1);
      String value = props.getProperty(variable);
      if (value == null) {
        throw new XSLWebException(String.format("No value specified for variable \"%s\"", variable));
      }
      String resolved = resolveProperties(value.toString(), props);
      resolved = resolved.replaceAll("([\\\\\\$])", "\\\\$1");
      m.appendReplacement(result, resolved);
    }
    m.appendTail(result);
    return result.toString();
  }
  
  public static Properties readProperties(File propsFile) throws IOException {    
    if (!propsFile.isFile()) {
      throw new FileNotFoundException(String.format("Properties file \"%s\" not found", propsFile.getAbsolutePath()));
    }
    Properties props = new Properties();
    InputStream is = new BufferedInputStream(new FileInputStream(propsFile));
    try {
      props.load(is);
    } finally {
      is.close();
    } 
    return props;
  }
  
  public static Map<String, List<String>> splitQuery(String rawQuery) throws UnsupportedEncodingException {
    final Map<String, List<String>> queryPairs = new LinkedHashMap<String, List<String>>();
    final String[] pairs = rawQuery.split("&");
    for (String pair : pairs) {
      final int idx = pair.indexOf("=");
      final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
      if (!queryPairs.containsKey(key)) {
        queryPairs.put(key, new LinkedList<String>());
      }
      final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
      queryPairs.get(key).add(value);
    }
    return queryPairs;
  }
  
  public static Map<String, List<String>> splitQuery(URI uri) throws UnsupportedEncodingException {
    String rawQuery = uri.getRawQuery();
    if (rawQuery == null)
      return null;
    return splitQuery(rawQuery);
  }
  
  public static boolean hasSubDirectories(File file) {
    return file.listFiles((FileFilter) DirectoryFileFilter.INSTANCE).length > 0;
  }
  
  public static void addStylesheetParameters(Map<QName, XdmValue> params, List<Parameter> parameters) throws IOException {
    if (parameters == null) {
      return;
    }
    for (Parameter param : parameters) {
      QName qname = (param.getURI() != null) ? new QName(param.getURI(), param.getName()) : new QName(param.getName());                 
      params.put(qname, new XdmValue(param.getValue()));                  
    }        
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static Map<QName, XdmValue> getStylesheetParameters(WebApp webApp, HttpServletRequest req, 
      HttpServletResponse resp, File homeDir) throws Exception {
    Map<QName, XdmValue> params = new HashMap<QName, XdmValue>();
    
    /* Property parameters : */
    Properties props = Context.getInstance().getProperties();
    for (String key : props.stringPropertyNames()) {
      String value = props.getProperty(key);      
      params.put(new QName(Definitions.NAMESPACEURI_XSLWEB_CONFIGURATION, key), new XdmAtomicValue(value));
    }    
    params.put(new QName(Definitions.NAMESPACEURI_XSLWEB_CONFIGURATION, "home-dir"), new XdmAtomicValue(homeDir.getAbsolutePath()));
    params.put(new QName(Definitions.NAMESPACEURI_XSLWEB_CONFIGURATION, "webapp-dir"), new XdmAtomicValue(webApp.getHomeDir().getAbsolutePath()));
    params.put(new QName(Definitions.NAMESPACEURI_XSLWEB_CONFIGURATION, "webapp-path"), new XdmAtomicValue(webApp.getPath()));
    params.put(new QName(Definitions.NAMESPACEURI_XSLWEB_CONFIGURATION, "development-mode"), new XdmAtomicValue(webApp.getDevelopmentMode()));
    params.put(new QName(Definitions.NAMESPACEURI_XSLWEB_CONFIGURATION, "version"), new XdmAtomicValue(Definitions.PROJECT_VERSION));
    
    /* Object parameters: */
    params.put(new QName(Definitions.NAMESPACEURI_XSLWEB_REQUEST, "request"),  XdmValue.wrap(new ObjectValue(req)));
    params.put(new QName(Definitions.NAMESPACEURI_XSLWEB_RESPONSE, "response"),  XdmValue.wrap(new ObjectValue(resp)));
    params.put(new QName(Definitions.NAMESPACEURI_XSLWEB_WEBAPP, "webapp"),  XdmValue.wrap(new ObjectValue(webApp)));
    
    /* Webapp parameters: */
    addStylesheetParameters(params, webApp.getParameters());
    
    return params;
  }
  
  @SuppressWarnings("unchecked")
  public static void addCloseable(HttpServletRequest req, Closeable closeable) {
    List<Closeable> closeables = (List<Closeable>) req.getAttribute("xslweb-closeables");
    if (closeables == null) {
      closeables = new ArrayList<Closeable>();
      req.setAttribute("xslweb-closeables", closeables);
    }
    closeables.add(closeable);
  }
  
  public static File getSafeTempFile(String path) {    
    if (StringUtils.isBlank(path)) {
      return null;
    }
    File file = new File(path);
    if (!file.isAbsolute()) {
      return null;
    }
    if (file.toPath().getNameCount() == 0) {
      return null;
    }
    return new File(path);
  }
  
  public static String encodeForURI(String input) {
    StringBuilder resultStr = new StringBuilder();
    for (char ch : input.toCharArray()) {
      if (isUnsafe(ch)) {
        resultStr.append('%');
        resultStr.append(toHex(ch / 16));
        resultStr.append(toHex(ch % 16));
      } else {
        resultStr.append(ch);
      }
    }
    return resultStr.toString();
  }

  private static char toHex(int ch) {
    return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
  }

  private static boolean isUnsafe(char ch) {
    if (ch > 128 || ch < 0)
      return true;
    return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
  }
  
}