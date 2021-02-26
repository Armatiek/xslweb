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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.saxon.expr.StackFrame;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.GlobalParam;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.ParameterSet;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.WebApp;

public class EvaluateXPath {
  
  private static XPathSelector getXPathSelector(BreakpointInfo breakpointInfo, XPathContext context, String expression) throws SaxonApiException, XPathException {
    WebApp webApp = breakpointInfo.getWebApp();
    
    Map<StructuredQName, XdmValue> variables = new HashMap<StructuredQName, XdmValue>();  
    
    /* Global variables: */
    for (GlobalVariable var: breakpointInfo.getGlobalVariables()) {
      Sequence value;
      try {
        value = var.evaluateVariable(context);
      } catch (XPathException xpe) {
        value = new StringValue("ERROR: " + xpe.getMessage());
      }
      variables.put(var.getVariableQName(), XdmValue.wrap(value));
    }
    
    /* Global parameters: */
    Map<StructuredQName, Sequence> params = new HashMap<StructuredQName, Sequence>();
    for (StructuredQName name: context.getController().getExecutable().getGlobalParameters().keySet()) {
      Sequence value = context.getController().getParameter(name);
      if (!(value instanceof ObjectValue)) {
        params.put(name, value);
      }
    }
    
    for (GlobalParam param : breakpointInfo.getGlobalParams()) {
      Sequence value;
      try {
        value = param.evaluateVariable(context);
      } catch (XPathException xpe) {
        value = new StringValue("ERROR: " + xpe.getMessage());
      }
      params.put(param.getVariableQName(), value);
    }
    
    if (!params.isEmpty()) {
      for (Map.Entry<StructuredQName, Sequence> entry : params.entrySet()) {
        StructuredQName name = entry.getKey();
        Sequence value = entry.getValue();
        variables.put(name, XdmValue.wrap(value));
      }
    }
    
 
    /* Local parameters: */
    ParameterSet locals = context.getLocalParameters();
    if (locals != null && locals.size() > 0) {
      setParameterSet(context.getLocalParameters(), variables);
    } 
    
    /* Tunnel parameters: */
    ParameterSet tunnels = context.getTunnelParameters();
    if (tunnels != null && tunnels.size() > 0) {
      setParameterSet(context.getTunnelParameters(), variables);
    }
    
    /* Local variables: */
    StackFrame stack = context.getStackFrame();
    SlotManager map = stack.getStackFrameMap();
    Sequence[] values = stack.getStackFrameValues();
    if (map.getNumberOfVariables() > 0) {
      for (int i = 0; i < map.getNumberOfVariables(); i++) {
        if (values[i] != null) {
          StructuredQName name = (StructuredQName)map.getVariableMap().get(i);
          if (variables.containsKey(name)) {
            continue;
          }
          variables.put(name, XdmValue.wrap(values[i]));
        } 
      } 
    }
    
    XPathCompiler compiler = webApp.getProcessor().newXPathCompiler();
    compiler.setLanguageVersion("3.1");
    
    compiler.declareNamespace("fn", "http://www.w3.org/2005/xpath-functions");
    
    if (context.getContextItem() instanceof NodeInfo) {
      NodeInfo node = (NodeInfo) context.getContextItem();
      NamespaceMap nsMap = node.getAllNamespaces();
      nsMap.forEach(binding -> { compiler.declareNamespace(binding.getPrefix(), binding.getURI()); });
    }
    
    Iterator<StructuredQName> it = variables.keySet().iterator();
    while (it.hasNext()) {
      compiler.declareVariable(new QName(it.next()));
    }
   
    XPathExecutable executable = compiler.compile(expression);
    XPathSelector selector = executable.load();
    if (context.getContextItem() instanceof NodeInfo) {
      selector.setContextItem(new XdmNode((NodeInfo) context.getContextItem()));
    }
    
    Iterator<Map.Entry<StructuredQName, XdmValue>> it2 = variables.entrySet().iterator();
    while (it2.hasNext()) {
      Map.Entry<StructuredQName, XdmValue> pair = (Map.Entry<StructuredQName, XdmValue>) it2.next();
      selector.setVariable(new QName(pair.getKey()), pair.getValue());
    }
    
    return selector;
  }
  
  public static String serializedStringValue(BreakpointInfo breakpointInfo, XPathContext context, String expression) throws Exception, XPathException {
    XPathSelector selector = getXPathSelector(breakpointInfo, context, expression);
    XdmValue result = selector.evaluate();
    WebApp webApp = breakpointInfo.getWebApp();
    return DebugUtils.getDisplayText(webApp, result.getUnderlyingValue(), "expanded");
  }
  
  public static boolean effectiveBooleanValue(BreakpointInfo breakpointInfo, XPathContext context, String expression) throws SaxonApiException, XPathException {
    XPathSelector selector = getXPathSelector(breakpointInfo, context, expression);
    return selector.effectiveBooleanValue();
  }
  
  private static void setParameterSet(ParameterSet params, Map<StructuredQName, XdmValue> variables) throws SaxonApiException, XPathException {
    ParameterSet ps;
    try {
      ps = new ParameterSet(params, 0);
      ps.materializeValues();
    } catch (XPathException err) {
      ps = params;
    } 
    for (int i=0; i < ps.size(); i++) {
      StructuredQName name = ps.getParameterNames()[i];
      if (variables.containsKey(name)) {
        continue;
      }
      variables.put(name, XdmValue.wrap(ps.getValue(i)));
    }
  }
  
}