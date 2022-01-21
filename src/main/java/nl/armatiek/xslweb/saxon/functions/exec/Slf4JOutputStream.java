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
package nl.armatiek.xslweb.saxon.functions.exec;

import org.apache.commons.exec.LogOutputStream;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import com.nimbusds.oauth2.sdk.util.StringUtils;

public class Slf4JOutputStream extends LogOutputStream {
  
  private final Logger logger;
  private final Level level;
  private final String messagePrefix;

  public Slf4JOutputStream(Logger logger, Level level, String messagePrefix) {
    this.logger = logger;
    this.level = level;
    this.messagePrefix = messagePrefix;
  }

  @Override
  protected void processLine(String line, int logLevel) {
    if (StringUtils.isBlank(line)) {
      return;
    }
    String message = messagePrefix + line;
    switch (level) {
    case INFO:
      logger.info(message);
      break;
    case DEBUG:
      logger.debug(message);
      break;
    case WARN:
      logger.warn(message);
      break;
    case ERROR:
      logger.error(message);
      break;
    case TRACE:
      logger.trace(message);
      break;
    }
  }

}