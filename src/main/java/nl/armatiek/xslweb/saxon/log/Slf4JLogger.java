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
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.lib.Logger;

import org.apache.commons.io.output.ProxyWriter;

/**
 * Saxon Logger that logs to Slf4J logging framework.
 * 
 * @author Maarten Kroon
 */
public class Slf4JLogger extends Logger {
  
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Slf4JLogger.class);

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
  
}