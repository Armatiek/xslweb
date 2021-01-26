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

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.datatype.Duration;

import org.apache.commons.lang3.StringUtils;

import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.Resource;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.web.servlet.FileServlet;

public class StaticResourceFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException { }
  
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;
    WebApp webApp = (WebApp) req.getAttribute(Definitions.ATTRNAME_WEBAPP);
    final String path = StringUtils.defaultString(req.getPathInfo()) + req.getServletPath();
    Resource resource = webApp.matchesResource(webApp.getRelativePath(path));
    if (resource == null) {
      // Request must result in an XSLT transformation:
      chain.doFilter(request, response);
    } else {
      String method = req.getMethod().toUpperCase();
      if (!method.equals("HEAD") && !method.equals("GET")) {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method \"" + method + "\" not supported");
        return;
      } 
      
      // Request must return a static resource:
      FileServlet fileServlet = new FileServlet() {

        private static final long serialVersionUID = 1L;

        @Override
        protected File getFile(HttpServletRequest request) {
          String finalPath = path;
          String cacheBusterId = webApp.getCacheBusterId();
          if (cacheBusterId != null) {
            finalPath = StringUtils.remove(finalPath, cacheBusterId);
          }
          return webApp.getStaticFile(finalPath);
        }
        
        @Override
        protected long getExpireTime(HttpServletRequest request, File file) {
          Duration duration = resource.getDuration();
          if (duration != null) {
            return duration.getTimeInMillis(new Date()) / 1000;
          };
          return super.getExpireTime(request, file);
        }
        
        @Override
        protected String getContentType(HttpServletRequest request, File file) {
          String contentType = resource.getMediaType();
          if (contentType == null || contentType.equals("")) {
            return super.getContentType(request, file);
          }
          return contentType;
        }
        
      };
      
      if (method.equals("GET")) {
        fileServlet.doGet((HttpServletRequest) request, (HttpServletResponse) response);
      } else if (method.equals("HEAD")) {
        fileServlet.doHead((HttpServletRequest) request, (HttpServletResponse) response);
      }
    
    }
  }

  @Override
  public void destroy() { }

}