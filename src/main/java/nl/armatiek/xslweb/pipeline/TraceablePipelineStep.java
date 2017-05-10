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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.StandardLogger;
import net.sf.saxon.lib.TraceListener;
import nl.armatiek.xslweb.configuration.WebApp;

public abstract class TraceablePipelineStep extends ParameterizablePipelineStep {
  
  public enum TraceType { NONE, BASIC, TIMING }
  
  protected TraceListener traceListener;
  protected ByteArrayOutputStream baos;
  protected PrintStream ps;
  protected WebApp webApp;
    
  public TraceablePipelineStep(WebApp webApp, String name, boolean log) {
    super(name, log);   
    this.webApp = webApp;
  }

  public TraceListener getTraceListener(TraceType traceType) {
    this.baos = new ByteArrayOutputStream();
    this.ps = new PrintStream(baos);
    Logger logger = new StandardLogger(ps);
    this.traceListener = getTraceListenerInternal(traceType);
    this.traceListener.setOutputDestination(logger);
    return this.traceListener;
  }
  
  protected abstract TraceListener getTraceListenerInternal(TraceType traceType);
  
  public String getTracing() {
    if (traceListener == null)
      return null;
    this.traceListener.close();
    this.ps.flush();
    return new String(baos.toByteArray(), StandardCharsets.UTF_8);
  }
  
}