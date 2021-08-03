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
import java.util.ArrayList;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.web.filter.PipelineGeneratorFilter;
import nl.armatiek.xslweb.web.filter.RequestSerializerFilter;
import nl.armatiek.xslweb.web.filter.SetCharacterEncodingFilter;
import nl.armatiek.xslweb.web.filter.WebAppFilter;

public class InternalRequest {
  
  private static final Logger logger = LoggerFactory.getLogger(InternalRequest.class);
  
  public int execute(String path, OutputStream os, boolean isJobRequest) throws ServletException, IOException {
    try {
      ArrayList<Filter> filters = new ArrayList<Filter>();
      XSLWebFilterConfig emptyConfig = new XSLWebFilterConfig();
      
      Filter filter;
      filter = new SetCharacterEncodingFilter();
      XSLWebFilterConfig config = new XSLWebFilterConfig();
      config.addInitParameter("encoding", "UTF-8");
      filter.init(config);
      filters.add(filter);
      
      filter = new WebAppFilter();
      filter.init(emptyConfig);
      filters.add(filter);
      
      filter = new RequestSerializerFilter();
      filter.init(emptyConfig);
      filters.add(filter);
      
      filter = new PipelineGeneratorFilter();
      filter.init(emptyConfig);
      filters.add(filter);
      
      /*
      filter = new CachingFilter();
      filter.init(emptyConfig);
      filters.add(filter);
      */
      
      HttpServlet servlet = new XSLWebServlet();
      servlet.init();
            
      XSLWebFilterChain filterChain = new XSLWebFilterChain(servlet, filters.toArray(new Filter[filters.size()]));
      
      ServletContext servletContext = Context.getInstance().getServletContext();
      
      ServletRequest request = new XSLWebHttpServletRequest(servletContext, path);
      ServletResponse response = new XSLWebHttpServletResponse(os);
      
      WebApp webApp = null;
      if (isJobRequest) {
        webApp = WebAppFilter.getWebApp(request);
        if (webApp != null) {
          webApp.incJobRequestCount();
        }
      }
      
      try {
        filterChain.doFilter(request, response);
      } finally {
        if (webApp != null) {
          webApp.decJobRequestCount();
        } 
      }
      
      return ((HttpServletResponse) response).getStatus();
      
    } catch (Exception e) {
      logger.error("Error executing internal servlet request to \"" + path + "\"", e);
      throw e;
    }            
  }
  
  public int execute(String path, OutputStream os) throws ServletException, IOException {
    return this.execute(path, os, false);
  }

}