package nl.armatiek.xslweb.web.filter;

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

import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.Resource;
import nl.armatiek.xslweb.configuration.WebApp;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class WebAppFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException { }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;        
    String path = StringUtils.defaultString(req.getPathInfo()) + req.getServletPath();      
    WebApp webApp = Context.getInstance().getWebApp(path);    
    if (webApp == null) {
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else if (webApp.isClosed()) {
      resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    } else {
      req.setAttribute(Definitions.ATTRNAME_WEBAPP, webApp);
      Resource resource = webApp.matchesResource(webApp.getRelativePath(path));
      if (resource == null) {                                
        chain.doFilter(request, response);               
      } else {
        resp.setContentType(resource.getMediaType());
        File file = webApp.getStaticFile(path);
        if (!file.isFile()) {
          resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
          Date currentDate = new Date();
          long now = currentDate.getTime();
          long duration = resource.getDuration().getTimeInMillis(currentDate);
          resp.addHeader("Cache-Control", "max-age=" + duration / 1000);
          resp.setDateHeader("Expires", now + duration);
          FileUtils.copyFile(file, resp.getOutputStream());
        }
      }
    }        
  }

  @Override
  public void destroy() { }

}
