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

import java.util.List;
import java.util.Stack;

import javax.servlet.http.HttpServletRequest;

public class ConditionalPipelineStep extends ParameterizablePipelineStep {
  
  private Stack<Condition> conditions = new Stack<Condition>();
  
  public ConditionalPipelineStep(String name) {
    super(name, false);
  }
 
  public Stack<Condition> getConditions() {
    return conditions;
  }
  
  public void addCondition(Condition condition) {
    conditions.add(condition);
  }
 
  public List<PipelineStep> evaluatePipelineSteps(HttpServletRequest request) {
    for (Condition condition: conditions) {
      if (condition.evaluate(request)) {
        return condition.getPipelineSteps();
      }
    }
    return null; // TODO: throw exception
  }
  
}