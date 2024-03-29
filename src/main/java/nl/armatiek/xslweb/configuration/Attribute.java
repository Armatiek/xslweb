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

import java.io.Serializable;

/**
 * Attribute that is stored by XPath extension functions in the Context, 
 * a WebApp or Session object and can contain primitive types or 
 * NodeInfo objects.
 * 
 * @author Maarten Kroon
 */
public class Attribute implements Serializable {
  
  private static final long serialVersionUID = -6139074295723291672L;
  
  private Object value;
  private String type;
  
  public Attribute(Object value, String type) {    
    this.value = value;
    this.type = type;
  }
  
  public Object getValue() {
    return value;
  }
  
  public String getType() {
    return this.type;
  }
  
}