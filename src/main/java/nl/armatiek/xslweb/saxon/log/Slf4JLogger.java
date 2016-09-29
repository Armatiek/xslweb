package nl.armatiek.xslweb.saxon.log;

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
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ProxyWriter;

import net.sf.saxon.lib.Logger;

/**
 * Saxon Logger that logs to Slf4J logging framework.
 * 
 * @author Maarten Kroon
 */
public class Slf4JLogger extends Logger {
  
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Slf4JLogger.class);
  
  private HttpServletResponse response;
  private boolean firstError;
  private boolean developmentMode;
  
  public Slf4JLogger(HttpServletResponse response, boolean developmentMode) {
    this.response = response;
    this.developmentMode = developmentMode;
  }

  @Override
  public void println(String message, int severity) {
    switch(severity) {
    case Logger.INFO:
      logger.info(message);
      break;
    case Logger.WARNING:
      logger.warn(message);
      break;
    case Logger.ERROR:
      logger.error(message);
      break;
    case Logger.DISASTER:
      logger.error(message);
      break;
    }
  }

  @Override
  public StreamResult asStreamResult() {
    Writer w = new ProxyWriter(new StringWriter()) {
      @Override
      public void flush() throws IOException {        
        logger.info(out.toString());
        super.flush();
      }
    };    
    return new StreamResult(w);        
  }
  
  private void writeToResponse(String message) {
    if (response == null) {
      return;
    }
    try {
      if (developmentMode) {
        if (firstError) {
          response.setContentType("text/plain;charset=UTF-8");
          firstError = false;
        }
        IOUtils.copy(new StringReader(message), response.getOutputStream(), "UTF-8");
      }
    } catch (Exception e) {
      logger.error("Could not write error to HttpServletResponse", e);
    }
  }

  @Override
  public void error(String message) {
    super.error(message);
    writeToResponse(message);
  }

  @Override
  public void disaster(String message) {
    super.disaster(message);
    writeToResponse(message);
  }
  
}