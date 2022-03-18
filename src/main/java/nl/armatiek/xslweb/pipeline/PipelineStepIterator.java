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

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;

public class PipelineStepIterator<T> implements Iterator<PipelineStep> {
  
  private List<PipelineStep> pipelineSteps;
  private HttpServletRequest request;
  private int index = 0;
  
  public PipelineStepIterator(final List<PipelineStep> pipelineSteps, final HttpServletRequest request) {
    this.pipelineSteps = pipelineSteps;
    this.request = request;
  }

  @Override
  public boolean hasNext() {
    return pipelineSteps.size() > index;
  }

  @Override
  public PipelineStep next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    PipelineStep nextPipelineStep = pipelineSteps.get(index);
    if (nextPipelineStep instanceof ConditionalPipelineStep) {
      List<PipelineStep> evaluatedSteps = ((ConditionalPipelineStep) nextPipelineStep).evaluatePipelineSteps(request); 
      pipelineSteps.remove(index);
      pipelineSteps.addAll(index, evaluatedSteps);
      return pipelineSteps.get(index++);
    } else {
      index++;
      return nextPipelineStep;
    }
  }
  
} /*
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

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;

public class PipelineStepIterator implements Iterator<PipelineStep> {
  
  private List<PipelineStep> pipelineSteps;
  private HttpServletRequest request;
  private int index = 0;
  
  public PipelineStepIterator(final List<PipelineStep> pipelineSteps, final HttpServletRequest request) {
    this.pipelineSteps = pipelineSteps;
    this.request = request;
  }

  @Override
  public boolean hasNext() {
    return pipelineSteps.size() > index;
  }

  @Override
  public PipelineStep next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    PipelineStep nextPipelineStep = pipelineSteps.get(index);
    if (nextPipelineStep instanceof ConditionalPipelineStep) {
      List<PipelineStep> evaluatedSteps = ((ConditionalPipelineStep) nextPipelineStep).evaluatePipelineSteps(request); 
      pipelineSteps.remove(index);
      pipelineSteps.addAll(index, evaluatedSteps);
      return pipelineSteps.get(index++);
    } else {
      index++;
      return nextPipelineStep;
    }
  }
  
} 