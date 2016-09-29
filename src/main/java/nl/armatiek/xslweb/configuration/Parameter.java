package nl.armatiek.xslweb.configuration;

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

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.ItemTypeFactory;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import nl.armatiek.xslweb.error.XSLWebException;
import nl.armatiek.xslweb.utils.XMLUtils;

import org.w3c.dom.Element;

public class Parameter {
  
  private ItemTypeFactory itemTypeFactory;
  private String uri;
  private String name;  
  private String type;
  private List<XdmItem> value;
  
  public Parameter(Processor processor, String uri, String name, String type, String value) {    
    this.itemTypeFactory = new ItemTypeFactory(processor);
    this.uri = uri;
    this.name = name;
    this.type = type;    
    addValue(value);
  }
  
  public Parameter(Processor processor, String uri, String name, String type) {    
    this(processor, uri, name, type, null);
  }
  
  public Parameter(Processor processor, Element paramElem) {
    this(processor, paramElem.getAttribute("uri"), paramElem.getAttribute("name"), paramElem.getAttribute("type"));    
    Element valueElem = XMLUtils.getFirstChildElement(paramElem);
    while (valueElem != null) {
      addValue(valueElem.getTextContent());
      valueElem = XMLUtils.getNextSiblingElement(valueElem);
    }                   
  }
  
  public void addValue(String value) {
    if (value == null) {
      return;
    }
    try {
      if (this.value == null) {
        this.value = new ArrayList<XdmItem>();
      }
      ItemType itemType = itemTypeFactory.getAtomicType(new QName(Definitions.NAMESPACEURI_XMLSCHEMA, this.type));
      this.value.add(new XdmAtomicValue(value, itemType));        
    } catch (SaxonApiException sae) {
      throw new XSLWebException("Error adding parameter value", sae);
    }
  }
  
  public String getURI() {
    return uri;
  }
  
  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public Iterable<XdmItem> getValue() {
    return value;
  }
  
}