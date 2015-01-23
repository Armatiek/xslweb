package nl.armatiek.xslweb.pipeline;

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

import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLOutputFactory;

public class JSONSerializerStep extends SerializerStep {
  
  public JSONSerializerStep(String name, boolean log) {
    super(name, log);        
  }
  
  public XMLStreamWriter getWriter(OutputStream os, String encoding) throws XMLStreamException {
    JsonXMLConfig config = new JsonXMLConfigBuilder().        
        prettyPrint(true).
        build();    
    XMLOutputFactory factory = new JsonXMLOutputFactory(config);
    return factory.createXMLStreamWriter(os, encoding);
  }
  
}