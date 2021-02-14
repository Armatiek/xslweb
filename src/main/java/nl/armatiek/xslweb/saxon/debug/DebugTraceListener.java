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
package nl.armatiek.xslweb.saxon.debug;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

import org.slf4j.LoggerFactory;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.GlobalParam;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.om.Item;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.Traceable;
import net.sf.saxon.trans.Mode;
import nl.armatiek.xslweb.configuration.WebApp;

public abstract class DebugTraceListener implements TraceListener {
  
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DebugTraceListener.class);

  private WebApp webApp;
  private DebugClient client;
  private Item currentItem;
  private Stack<GlobalParam> globalParamStack = new Stack<GlobalParam>();
  private ArrayList<GlobalParam> globalParams = new ArrayList<GlobalParam>();
  private Stack<GlobalVariable> globalVariableStack = new Stack<GlobalVariable>();
  private ArrayList<GlobalVariable> globalVariables = new ArrayList<GlobalVariable>();
  
  public DebugTraceListener(WebApp webApp, DebugClient client) {
    this.webApp = webApp;
    this.client = client;
  }

  @Override
  public void enter(Traceable instruction, Map<String, Object> properties, XPathContext context) {
    if (instruction instanceof GlobalParam) {
      globalParamStack.push((GlobalParam) instruction);
    } else if (instruction instanceof GlobalVariable) {
      globalVariableStack.push((GlobalVariable) instruction);
    }
    Location loc = instruction.getLocation();
    String systemId = loc.getSystemId();
    if (systemId == null || systemId.endsWith("/xsl/system/response/response.xsl")) {
      return;
    }
    try {
      client.breakThread(webApp, instruction, properties, context, currentItem, 
          globalParams, globalVariables, getInstructionLabel(instruction));
    } catch (Exception e) {
      logger.error("Error breaking thread", e);
    }
  }
  
  /*
  protected int level(Traceable info) {
    if (info instanceof TraceableComponent) {
        return 1;
    } if (info instanceof Instruction) {
        return 2;
    } else {
        return 3;
    }
  }
  */

  @Override
  public void leave(Traceable instruction) {
    if (instruction instanceof GlobalParam) {
      GlobalParam param = globalParamStack.pop();
      globalParams.add(param);
    } else if (instruction instanceof GlobalVariable) {
      GlobalVariable var = globalVariableStack.pop();
      globalVariables.add(var);
    }
  }
  
  @Override
  public void endRuleSearch(Object ruleObj, Mode mode, Item item) {
    if (!mode.isModeTracing()) {
      mode.setModeTracing(true);
    }
  }
  
  @Override
  public void setOutputDestination(Logger stream) { }

  @Override
  public void open(Controller controller) { 
    // client.openPipeline();
  }

  @Override
  public void close() { 
    // client.closePipeline();
  }

  @Override
  public void startCurrentItem(Item currentItem) {
    this.currentItem = currentItem;
  }

  @Override
  public void endCurrentItem(Item currentItem) {
    this.currentItem = null;
  }
  
  public abstract String getInstructionLabel(Traceable info);

}