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
package nl.armatiek.xslweb.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;

public class WebAppFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException { }
  
  public static WebApp getWebApp(ServletRequest request) {
    HttpServletRequest req = (HttpServletRequest) request;
    String path = StringUtils.defaultString(req.getPathInfo()) + req.getServletPath();      
    return Context.getInstance().getWebApp(path);
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;            
    WebApp webApp = getWebApp(request);    
    if (webApp == null) {
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else if (webApp.isClosed()) {
      resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    } else {
      req.setAttribute(Definitions.ATTRNAME_WEBAPP, webApp);
      chain.doFilter(request, response);
    }        
  }

  @Override
  public void destroy() { }

}
