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
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.ErrorListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.SAXDestination;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.serialize.MessageWarner;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.pipeline.PipelineHandler;
import nl.armatiek.xslweb.saxon.errrorlistener.TransformationErrorListener;
import nl.armatiek.xslweb.utils.XSLWebUtils;

public class PipelineGeneratorFilter implements Filter {
  
  private static final Logger logger = LoggerFactory.getLogger(PipelineGeneratorFilter.class);
  
  private File homeDir;
  
  @Override
  public void init(FilterConfig filterConfig) throws ServletException { 
    homeDir = Context.getInstance().getHomeDir(); 
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
      throws IOException, ServletException {
    WebApp webApp = null;
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;
    try {             
      webApp = (WebApp) request.getAttribute(Definitions.ATTRNAME_WEBAPP);
      
      ErrorListener errorListener = new TransformationErrorListener(resp, webApp.getDevelopmentMode());      
      MessageWarner messageWarner = new MessageWarner();
      
      XsltExecutable templates = webApp.getRequestDispatcherTemplates(errorListener, false);
      Xslt30Transformer transformer = templates.load30();
      transformer.setStylesheetParameters(XSLWebUtils.getStylesheetParameters(webApp, req, resp, homeDir));
      transformer.setErrorListener(errorListener);            
      transformer.getUnderlyingController().setMessageEmitter(messageWarner);            
                               
      PipelineHandler pipelineHandler = new PipelineHandler(webApp);
      NodeInfo source = (NodeInfo) req.getAttribute(Definitions.ATTRNAME_REQUESTXML);
      Destination destination = new SAXDestination(pipelineHandler);
      transformer.applyTemplates(source, destination);
      
      req.setAttribute(Definitions.ATTRNAME_PIPELINEHANDLER, pipelineHandler);
      
      chain.doFilter(request, response);
      
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      if (webApp != null && webApp.getDevelopmentMode()) {              
        resp.setContentType("text/plain; charset=UTF-8");        
        e.printStackTrace(new PrintStream(resp.getOutputStream()));        
      } else if (!resp.isCommitted()) {
        resp.resetBuffer();
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setContentType("text/html; charset=UTF-8");
        Writer w = new OutputStreamWriter(resp.getOutputStream(), "UTF-8");
        w.write("<html><body><h1>Internal Server Error</h1></body></html>");
      }
    }
  }

  @Override
  public void destroy() { }

}
