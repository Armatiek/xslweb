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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;

public class WebDavFilter implements Filter {
  
  private static final Logger logger = LoggerFactory.getLogger(WebDavFilter.class);

  @Override
  public void init(FilterConfig filterConfig) throws ServletException { }
  
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    WebApp webApp = (WebApp) req.getAttribute(Definitions.ATTRNAME_WEBAPP);
    if (webApp != null && webApp.getWebDavServletBean() != null && req.getPathInfo().startsWith(webApp.getPath() + "/webdav")) {
      Subject subject = null;
      if (webApp.getShiroWebEnvironment() != null) {
        try {
          subject = SecurityUtils.getSubject();
        } catch (Exception e) {
          logger.error("Could not get Shiro security subject", e);
          ((HttpServletResponse) response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Configuration error");
          return;
        } 
      }
      if (subject == null || subject.hasRole("webdav")) {
        webApp.getWebDavServletBean().service(request, response);
      } else {
        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "User does not have role \"webdav\"");
      }
    } else {
      chain.doFilter(request, response);
    }
  }

  @Override
  public void destroy() { }
  
}