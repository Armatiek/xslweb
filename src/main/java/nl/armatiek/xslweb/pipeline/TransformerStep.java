package nl.armatiek.xslweb.pipeline;

import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.trace.XSLTTraceListener;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.saxon.trace.XSLWebTimingTraceListener;

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

public class TransformerStep extends TraceablePipelineStep {
  
  private String xslPath;  
  
  public TransformerStep(WebApp webApp, String xslPath, String name, boolean log) {
    super(webApp, name, log);
    this.xslPath = xslPath;    
  }
  
  public String getXslPath() {
    return this.xslPath;
  }
  
  @Override
  protected TraceListener getTraceListenerInternal(TraceType traceType) {
    if (traceType.equals(TraceType.BASIC))
      return new XSLTTraceListener();
    else if (traceType.equals(TraceType.TIMING))
      return new XSLWebTimingTraceListener(webApp);
    return null;
  }
  
}