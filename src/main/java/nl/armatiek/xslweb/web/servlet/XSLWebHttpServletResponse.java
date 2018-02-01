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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class XSLWebHttpServletResponse implements HttpServletResponse {
  
  private OutputStream os;
  private int status;
  
  public XSLWebHttpServletResponse(OutputStream os) {
    this.os = os;  
  }

  @Override
  public String getCharacterEncoding() {
    return "UTF-8";
  }

  @Override
  public String getContentType() {
    return null;
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    return new DelegatingServletOutputStream(os);
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    return new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
  }

  @Override
  public void setCharacterEncoding(String charset) { }

  @Override
  public void setContentLength(int len) { }

  @Override
  public void setContentType(String type) { }

  @Override
  public void setBufferSize(int size) { }

  @Override
  public int getBufferSize() {  
    return 0;
  }

  @Override
  public void flushBuffer() throws IOException {
    
  }

  @Override
  public void resetBuffer() { }

  @Override
  public boolean isCommitted() {  
    return false;
  }

  @Override
  public void reset() { }

  @Override
  public void setLocale(Locale loc) { }

  @Override
  public Locale getLocale() {
    return null;
  }

  @Override
  public void addCookie(Cookie cookie) { }

  @Override
  public boolean containsHeader(String name) {  
    return false;
  }

  @Override
  public String encodeURL(String url) {    
    return url;
  }

  @Override
  public String encodeRedirectURL(String url) {
    return encodeURL(url);
  }

  @Override
  public String encodeUrl(String url) {
    return encodeURL(url);
  }

  @Override
  public String encodeRedirectUrl(String url) {
    return encodeRedirectURL(url);
  }

  @Override
  public void sendError(int sc, String msg) throws IOException { }

  @Override
  public void sendError(int sc) throws IOException { }
  
  @Override
  public void sendRedirect(String location) throws IOException { }

  @Override
  public void setDateHeader(String name, long date) { }

  @Override
  public void addDateHeader(String name, long date) { }

  @Override
  public void setHeader(String name, String value) { }

  @Override
  public void addHeader(String name, String value) { }

  @Override
  public void setIntHeader(String name, int value) { }

  @Override
  public void addIntHeader(String name, int value) { }

  @Override
  public void setStatus(int sc) {  
    status = sc;
  }

  @Override
  public void setStatus(int sc, String sm) { 
    status = sc;
  }

  @Override
  public int getStatus() {    
    return status;
  }

  @Override
  public String getHeader(String name) {  
    return null;
  }

  @Override
  public Collection<String> getHeaders(String name) {
    return new ArrayList<String>();
  }

  @Override
  public Collection<String> getHeaderNames() {
    return new ArrayList<String>();
  }

}