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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.serializer.RequestSerializer;

public class RequestSerializerFilter implements Filter {
  
  private static final Logger logger = LoggerFactory.getLogger(RequestSerializerFilter.class);

  private String lineSeparator;
  
  @Override
  public void init(FilterConfig filterConfig) throws ServletException { 
    lineSeparator = System.lineSeparator();
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
      throws IOException, ServletException {
    WebApp webApp = null;
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;
    try {             
      webApp = (WebApp) request.getAttribute(Definitions.ATTRNAME_WEBAPP);    
      RequestSerializer requestSerializer = new RequestSerializer(req, webApp);
      try {
        NodeInfo requestNodeInfo = requestSerializer.serializeToNodeInfo();
        request.setAttribute(Definitions.ATTRNAME_REQUESTXML, requestNodeInfo);      
        if (webApp.getDevelopmentMode()) {
          StringWriter sw = new StringWriter();
          try {
            Serializer ser = webApp.getProcessor().newSerializer(sw);
            ser.setOutputProperty(Serializer.Property.INDENT, "yes");
            ser.serializeNode(new XdmNode(requestNodeInfo));
            logger.debug("----------\nREQUEST XML:" + lineSeparator + sw.toString());
          } finally {
            sw.close();
          }
        }      
        chain.doFilter(request, response);        
      } finally {
        requestSerializer.close();
      }
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