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
package nl.armatiek.xslweb.pipeline;

import java.io.File;
import java.io.OutputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.ErrorListener;

import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.NullDestination;
import nl.armatiek.xslweb.configuration.WebApp;

public class StylesheetExportFileStep extends SerializerStep {
  
  private String xslPath;  
  
  public StylesheetExportFileStep(String xslPath, String name, boolean log) {
    super(name, log);
    this.xslPath = xslPath;    
  }
  
  public String getXslPath() {
    return this.xslPath;
  }

  @Override
  public Destination getDestination(WebApp webApp, HttpServletRequest req, HttpServletResponse resp, 
      OutputStream os, Properties outputProperties, ErrorListener errorListener) throws XMLStreamException {
    try {
      byte[] sef;
      if (new File(xslPath).isAbsolute()) {
        sef = webApp.tryStylesheetExportFile(xslPath, errorListener);
      } else {
        sef = webApp.tryStylesheetExportFile(new File(webApp.getHomeDir(), "xsl" + "/" + xslPath).getAbsolutePath(), errorListener);
      }
      os.write(sef);
    } catch (Exception e) {
      throw new XMLStreamException(e);
    }
    return new NullDestination();
  }
  
}