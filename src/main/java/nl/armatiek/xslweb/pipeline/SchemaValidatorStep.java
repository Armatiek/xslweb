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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Pipeline step that performs a XML Schema validation
 * 
 * @author Maarten Kroon
 */
public class SchemaValidatorStep extends PipelineStep {
  
  private List<String> schemaPaths;
  private Properties properties;
  private Properties features;
  private String xslParamName;
  private String xslParamNamespace;
    
  public SchemaValidatorStep(String name, boolean log, String xslParamNamespace, String xslParamName) {
    super(name, log);
    this.xslParamNamespace = xslParamNamespace;
    this.xslParamName = xslParamName;
  }
  
  public void addSchemaPath(String schemaPath) {
    if (schemaPaths == null) {
      schemaPaths = new ArrayList<String>();
    }
    schemaPaths.add(schemaPath);
  }
  
  public void addProperty(String name, String value) {
    if (properties == null) {
      properties = new Properties();
    }
    properties.setProperty(name, value);
  }
  
  public void addFeature(String name, String value) {
    if (features == null) {
      features = new Properties();
    }
    features.setProperty(name, value);
  }
  
  public String getXslParamName() {
    return xslParamName;
  }
  
  public String getXslParamNamespace() {
    return xslParamNamespace;
  }
    
  public List<String> getSchemaPaths() {    
    return schemaPaths;
  }
  
  public Properties getProperties() {
    return properties;
  }
  
  public Properties getFeatures() {
    return features;
  }
  
}