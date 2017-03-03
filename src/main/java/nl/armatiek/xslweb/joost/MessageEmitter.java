package nl.armatiek.xslweb.joost;

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

import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import net.sf.joost.emitter.XmlEmitter;

public class MessageEmitter extends XmlEmitter {
  
  private static final Logger logger = LoggerFactory.getLogger(MessageEmitter.class);

  public MessageEmitter() {
    super(new StringWriter(), "UTF-8", null);
  }
  
  @Override
  public void endDocument() throws SAXException {
    super.endDocument();
    logger.info(writer.toString()); 
  }
}