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

package nl.armatiek.xslweb.configuration;

import java.io.File;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import nl.armatiek.xmlindex.XMLIndex;
import nl.armatiek.xmlindex.lucene.codec.XMLIndexStoredFieldsFormat.Mode;
import nl.armatiek.xslweb.utils.XMLUtils;

/**
 * Index definition
 * 
 * @author Maarten Kroon
 */
public class Index {
  
  private String name; 
  private String path;
  private int maxTermLength;
  private Mode indexCompression = XMLIndex.DEFAULT_INDEX_COMPRESSION;
  
  public Index(XPath xpath, Element indexElem, File homeDir) {
    this.name = XMLUtils.getValueOfChildElementByLocalName(indexElem, "name");
    this.path = XMLUtils.getValueOfChildElementByLocalName(indexElem, "path");   
    this.maxTermLength = XMLUtils.getIntegerValue(XMLUtils.getValueOfChildElementByLocalName(indexElem, "max-term-length"), XMLIndex.DEFAULT_MAX_TERM_LENGTH);
    String mode = XMLUtils.getValueOfChildElementByLocalName(indexElem, "index-compression");
    if (mode != null)
      indexCompression = Mode.valueOf(mode.toUpperCase());
  }
  
  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }
  
  public int getMaxTermLength() {
    return maxTermLength;
  }
  
  public Mode getIndexCompression() {
    return indexCompression;
  }
  
}