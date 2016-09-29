package nl.armatiek.xslweb.web.servlet;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

public class XSLWebHttpServletRequest implements HttpServletRequest {
  
  private String encoding = "UTF-8";
  private String pathInfo;
  private String queryString;
  private Map<String, Object> attributes = new HashMap<String, Object>();
  private Map<String, String[]> parameters = new HashMap<String, String[]>();
  private ServletContext context;
  private HttpSession session;  
  
  public XSLWebHttpServletRequest(ServletContext context, String path) {
    this.context = context;
    this.session = new XSLWebHttpSession(context);
    String[] parts = path.split("\\?");
    if (parts.length > 1) {
      this.pathInfo = parts[0];            
      this.queryString = parts[1];
      Map<String, ArrayList<String>> paramMap = new HashMap<String, ArrayList<String>>();
      List<NameValuePair> params = URLEncodedUtils.parse(queryString, Charset.forName("UTF-8"));
      for (NameValuePair param : params) {
        ArrayList<String> values = (ArrayList<String>) paramMap.get(param.getName());
        if (values == null) {
          values = new ArrayList<String>(); 
        }
        values.add(param.getValue());        
        paramMap.put(param.getName(), values);
      }               
      for (Map.Entry<String, ArrayList<String>> entry : paramMap.entrySet()) {
        ArrayList<String> values = entry.getValue();
        parameters.put(entry.getKey(), values.toArray(new String[values.size()]));                
      }            
    } else {
      this.pathInfo = path;
    }     
    
  }

  @Override
  public Object getAttribute(String name) {
    return attributes.get(name);
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    return Collections.enumeration(new ArrayList<String>());
  }

  @Override
  public String getCharacterEncoding() {
    return encoding;
  }

  @Override
  public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
    this.encoding = encoding;
  }

  @Override
  public int getContentLength() {
    return -1;
  }

  @Override
  public String getContentType() {
    return null;
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return new ServletInputStream() {
      @Override
      public int read() throws IOException {
        return -1;
      }
    };
  }

  @Override
  public String getParameter(String name) {
    String[] values = parameters.get(name);
    if (values == null) {
      return null;
    }
    return values[0];
  }

  @Override
  public Enumeration<String> getParameterNames() {    
    return Collections.enumeration(parameters.keySet());        
  }

  @Override
  public String[] getParameterValues(String name) {
    String[] values = parameters.get(name);
    if (values == null) {
      return null;
    }
    return values;
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    return parameters;
  }

  @Override
  public String getProtocol() {
    return "HTTP/1.1";
  }

  @Override
  public String getScheme() {
    return "http";
  }

  @Override
  public String getServerName() {
    return "localname";
  }

  @Override
  public int getServerPort() {
    return 80;
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return new BufferedReader(new StringReader(""));
  }

  @Override
  public String getRemoteAddr() {
    return "127.0.0.1";
  }

  @Override
  public String getRemoteHost() {
    return "127.0.0.1";
  }

  @Override
  public void setAttribute(String name, Object o) {
    attributes.put(name, o);
  }

  @Override
  public void removeAttribute(String name) {
    attributes.remove(name);
  }

  @Override
  public Locale getLocale() {
    return Locale.getDefault();
  }

  @Override
  public Enumeration<Locale> getLocales() {
    ArrayList<Locale> list = new ArrayList<Locale>();
    list.add(getLocale());
    return Collections.enumeration(list);
  }

  @Override
  public boolean isSecure() {
    return false;
  }

  @Override
  public RequestDispatcher getRequestDispatcher(String path) {
    return null;
  }

  @Override
  public String getRealPath(String path) {
    return context.getRealPath(path);
  }

  @Override
  public int getRemotePort() {
    return 55451;
  }

  @Override
  public String getLocalName() {
    return "127.0.0.1";
  }

  @Override
  public String getLocalAddr() {
    return "127.0.0.1";
  }

  @Override
  public int getLocalPort() {
    return 80;
  }

  @Override
  public ServletContext getServletContext() {
    return context;
  }

  @Override
  public AsyncContext startAsync() throws IllegalStateException {
    return null;
  }

  @Override
  public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
    return null;
  }

  @Override
  public boolean isAsyncStarted() {
    return false;
  }

  @Override
  public boolean isAsyncSupported() {
    return false;
  }

  @Override
  public AsyncContext getAsyncContext() {
    return null;
  }

  @Override
  public DispatcherType getDispatcherType() {
    return DispatcherType.REQUEST;
  }

  @Override
  public String getAuthType() {    
    return null;
  }

  @Override
  public Cookie[] getCookies() {    
    return null;
  }

  @Override
  public long getDateHeader(String name) {    
    return -1;
  }

  @Override
  public String getHeader(String name) {    
    return null;
  }

  @Override
  public Enumeration<String> getHeaders(String name) {
    return Collections.enumeration(new ArrayList<String>());
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    return Collections.enumeration(new ArrayList<String>());
  }

  @Override
  public int getIntHeader(String name) {    
    return -1;
  }

  @Override
  public String getMethod() {
    return "GET";
  }

  @Override
  public String getPathInfo() {    
    return pathInfo;
  }

  @Override
  public String getPathTranslated() {
    return (this.pathInfo != null ? getRealPath(this.pathInfo) : null);
  }

  @Override
  public String getContextPath() {
    return context.getContextPath();    
  }

  @Override
  public String getQueryString() {    
    return queryString;
  }

  @Override
  public String getRemoteUser() {    
    return null;
  }

  @Override
  public boolean isUserInRole(String role) {    
    return false;
  }

  @Override
  public Principal getUserPrincipal() {  
    return null;
  }

  @Override
  public String getRequestedSessionId() {  
    return null;
  }

  @Override
  public String getRequestURI() {        
    return getContextPath() + getPathInfo();
  }

  @Override
  public StringBuffer getRequestURL() {
    StringBuffer url = new StringBuffer(getScheme()).append("://").append(this.getServerName());
    if (this.getServerPort() > 0
      && (("http".equalsIgnoreCase(getScheme()) && this.getServerPort() != 80) || ("https".equalsIgnoreCase(getScheme()) && getServerPort() != 443))) {
      url.append(':').append(this.getServerPort());
    }
    if (StringUtils.isNotBlank(getRequestURI())) {
      url.append(getRequestURI());
    }
    return url;
  }

  @Override
  public String getServletPath() {
    return ""; // matched on /*
  }

  @Override
  public HttpSession getSession(boolean create) {
    return session;
  }

  @Override
  public HttpSession getSession() {
    return session;
  }

  @Override
  public boolean isRequestedSessionIdValid() {
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromCookie() {
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromURL() {
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromUrl() {
    return false;
  }

  @Override
  public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
    return false;
  }

  @Override
  public void login(String username, String password) throws ServletException { }

  @Override
  public void logout() throws ServletException { }

  @Override
  public Collection<Part> getParts() throws IOException, ServletException {
    return new ArrayList<Part>();
  }

  @Override
  public Part getPart(String name) throws IOException, ServletException {    
    return null;
  }

}