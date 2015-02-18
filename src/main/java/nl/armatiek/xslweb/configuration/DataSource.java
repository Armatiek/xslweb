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

import java.util.Properties;

import nl.armatiek.xslweb.utils.XMLUtils;

import org.w3c.dom.Element;

/**
 * DataSource definition
 * 
 * @author Maarten
 */
public class DataSource {
  
  private String name; 
  private String driverClass;
  private String jdbcUrl;
  private String username;  
  private String password;
  private Properties properties;
  
  public DataSource(Element dataSourceElem) {
    this.name = XMLUtils.getValueOfChildElementByLocalName(dataSourceElem, "name");
    this.driverClass = XMLUtils.getValueOfChildElementByLocalName(dataSourceElem, "driver-class");
    this.jdbcUrl = XMLUtils.getValueOfChildElementByLocalName(dataSourceElem, "jdbc-url");
    this.username = XMLUtils.getValueOfChildElementByLocalName(dataSourceElem, "username");
    this.password = XMLUtils.getValueOfChildElementByLocalName(dataSourceElem, "password");        
    Element propElem = XMLUtils.getFirstChildElementByLocalName(dataSourceElem, "property");
    if (propElem != null) {
      this.properties = new Properties();
    }
    while (propElem != null) {
      properties.setProperty(propElem.getAttribute("name"), propElem.getTextContent());      
      propElem = XMLUtils.getNextSiblingElement(propElem);
    }            
  }
  
  public String getName() {
    return name;
  }

  public String getDriverClass() {
    return driverClass;
  }

  public String getJdbcUrl() {
    return jdbcUrl;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public Properties getProperties() {
    return properties;
  }
  
}