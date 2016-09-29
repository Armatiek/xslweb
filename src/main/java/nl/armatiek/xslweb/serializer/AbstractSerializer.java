package nl.armatiek.xslweb.serializer;

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.xml.ws.util.xml.ContentHandlerToXMLStreamWriter;

import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.utils.Closeable;
import nl.armatiek.xslweb.utils.XSLWebUtils;

public abstract class AbstractSerializer extends DefaultHandler implements Closeable {
  
  protected HttpServletRequest req;
  protected HttpServletResponse resp;  
  protected OutputStream os;
  protected WebApp webApp;
  protected DefaultHandler altHandler;
  
  public AbstractSerializer(WebApp webApp, HttpServletRequest req, HttpServletResponse resp, OutputStream os) {        
    this.webApp = webApp;
    this.req = req;
    this.resp = resp;
    this.os = os;
    if (req != null) {
      XSLWebUtils.addCloseable(req, this);
    }
  }
  
  public AbstractSerializer(WebApp webApp) {
    this(webApp, null, null, null);
  }
  
  protected DefaultHandler getAltHandler() throws SAXException, XMLStreamException {
    if (this.altHandler == null) {
      XMLOutputFactory output = XMLOutputFactory.newInstance();
      XMLStreamWriter writer = output.createXMLStreamWriter(this.os);
      this.altHandler = new ContentHandlerToXMLStreamWriter(writer);
      this.altHandler.startDocument();
    }
    return this.altHandler;
  }
  
}