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

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.constructs.blocking.LockTimeoutException;
import net.sf.ehcache.constructs.web.AlreadyCommittedException;
import net.sf.ehcache.constructs.web.AlreadyGzippedException;
import net.sf.ehcache.constructs.web.filter.FilterNonReentrantException;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.pipeline.PipelineHandler;
import nl.armatiek.xslweb.web.filter.cache.SimpleCachingHeadersPageCachingFilter;

public class CachingFilter extends SimpleCachingHeadersPageCachingFilter  {
  
  @Override
  protected String calculateKey(HttpServletRequest request) {     
    PipelineHandler pipelineHandler = (PipelineHandler) request.getAttribute(Definitions.ATTRNAME_PIPELINEHANDLER);
    String key = pipelineHandler.getCacheKey();
    if (key == null) {
      key = super.calculateKey(request);
    }
    WebApp webApp = (WebApp) request.getAttribute(Definitions.ATTRNAME_WEBAPP);
    String cacheScope = pipelineHandler.getCacheScope();
    if (cacheScope != null && cacheScope.equals("session")) {
      key = webApp.getName() + "-" + request.getSession().getId() + "-" + key;            
    } else {
      key = webApp.getName() + "-" + key;
    }                   
    return key;
  }

  @Override
  protected CacheManager getCacheManager() {    
    return Context.getInstance().getCacheManager();
  }

  @Override
  protected String getCacheName() {            
    return Definitions.CACHENAME_RESPONSECACHINGFILTER;
  }
  
  @Override
  protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws AlreadyGzippedException, AlreadyCommittedException, FilterNonReentrantException, LockTimeoutException, Exception {
    WebApp webApp = (WebApp) request.getAttribute(Definitions.ATTRNAME_WEBAPP);
    PipelineHandler pipelineHandler = (PipelineHandler) request.getAttribute(Definitions.ATTRNAME_PIPELINEHANDLER);
    if (!webApp.getDevelopmentMode() && pipelineHandler.getCache()) {
      super.doFilter(request, response, chain);                  
    } else {
      chain.doFilter(request, response);
    }
  }
}