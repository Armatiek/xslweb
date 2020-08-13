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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.Resource;
import nl.armatiek.xslweb.configuration.WebApp;

public class StaticResourceFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException { }
  
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;
    WebApp webApp = (WebApp) req.getAttribute(Definitions.ATTRNAME_WEBAPP);
    String path = StringUtils.defaultString(req.getPathInfo()) + req.getServletPath();
    Resource resource = webApp.matchesResource(webApp.getRelativePath(path));
    if (resource == null) {
      // Request must result in an XSLT transformation:
      chain.doFilter(request, response);
    } else {
      // Request must return a static resource:
      resp.setContentType(resource.getMediaType());
      String cacheBusterId = webApp.getCacheBusterId();
      if (cacheBusterId != null) {
        path = StringUtils.remove(path, cacheBusterId);
      }
      File file = webApp.getStaticFile(path);
      long ifModifiedSince;
      if (!file.isFile()) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      } else if ((ifModifiedSince = req.getDateHeader("If-Modified-Since")) > -1 && (file.lastModified() < ifModifiedSince + 1000)) {
        resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      } else {
        String cacheControl = "";
        Date currentDate = new Date();
        long now = currentDate.getTime();
        Duration duration = resource.getDuration();
        if (duration != null) {
          long ms = duration.getTimeInMillis(currentDate);
          cacheControl = "max-age=" + ms / 1000;
          resp.setDateHeader("Expires", now + ms);
        }
        String extraCacheControl = resource.getExtraCacheControl();
        if (StringUtils.isNoneBlank(extraCacheControl)) {
          cacheControl = cacheControl + StringUtils.prependIfMissing(extraCacheControl.trim(), ",");
        }
        if (cacheControl.length() > 0) {
          resp.addHeader("Cache-Control", cacheControl);
        }
        resp.setDateHeader("Last-Modified", file.lastModified());
        resp.setContentLength((int) file.length());
        FileUtils.copyFile(file, resp.getOutputStream());
      }
    }
  }

  @Override
  public void destroy() { }

}