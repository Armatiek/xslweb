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
package nl.armatiek.xslweb.utils;

import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * This class logs all bytes written to it as output stream with a specified
 * logging level.
 */
public class Slf4JOutputStream extends OutputStream {

  private final Logger logger;
  private final Level level;
  private final String messagePrefix;
  private StringBuffer buffer;

  public Slf4JOutputStream(Logger logger, Level level, String messagePrefix) {
    this.logger = logger;
    this.level = level;
    this.messagePrefix = "";
    buffer = new StringBuffer();
  }

  @Override
  public void write(final int b) {
    if ((char) b == '\n') {
      flush();
      return;
    }
    buffer = buffer.append((char) b);
  }

  public void flush() {
    String message = messagePrefix + buffer.toString();
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
    buffer = new StringBuffer();
  }

}