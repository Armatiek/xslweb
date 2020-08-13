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
package nl.armatiek.xslweb.shiro;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;

import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;


public class ShiroFilter extends AbstractShiroFilter {

  private ThreadLocal<ServletRequest> servletRequest = new ThreadLocal<ServletRequest>();

  @Override
  protected boolean isEnabled(ServletRequest request, ServletResponse response) throws ServletException, IOException {
    WebApp webApp = (WebApp) request.getAttribute(Definitions.ATTRNAME_WEBAPP);
    return webApp != null && webApp.getShiroWebEnvironment() != null;
  }
  
  @Override
  public FilterChainResolver getFilterChainResolver() {
    ServletRequest request = servletRequest.get();
    if (request == null) {
      return null;
    }
    WebApp webApp = (WebApp) request.getAttribute(Definitions.ATTRNAME_WEBAPP);
    if (webApp != null) {
      return webApp.getShiroWebEnvironment().getFilterChainResolver();
    } 
    return null;
  }
  
  @Override
  public WebSecurityManager getSecurityManager() {
    ServletRequest request = servletRequest.get();
    if (request == null) {
      return null;
    }
    WebApp webApp = (WebApp) request.getAttribute(Definitions.ATTRNAME_WEBAPP);
    if (webApp != null) {
      return webApp.getShiroWebEnvironment().getWebSecurityManager();
    }
    return null;
  }

  @Override
  protected void doFilterInternal(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
    servletRequest.set(req);
    super.doFilterInternal(req, resp, chain);
  }

}