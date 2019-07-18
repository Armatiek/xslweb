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

/**
 * Base class for all pipeline steps
 * 
 * @author Maarten Kroon
 */
public abstract class PipelineStep {
  
  protected String name;
  protected boolean log;
  
  public PipelineStep(String name, boolean log) {  
    this.name = name;
    this.log = log;
  }
  
  public String getName() {
    return this.name;
  }
  
  public boolean getLog() {
    return this.log;
  }

}
