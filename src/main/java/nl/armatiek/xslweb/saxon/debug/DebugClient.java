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

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.macias.sse.events.MessageEvent;
import info.macias.sse.servlet3.ServletEventTarget;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.GlobalParam;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.om.Item;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.trace.Traceable;
import net.sf.saxon.trans.XPathException;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.error.XSLWebException;

public class DebugClient {
  
  private static final Logger logger = LoggerFactory.getLogger(DebugClient.class);
  
  private static final int MAX_BREAKPOINTS = 256;
  
  private HttpServletRequest req;
  private ServletEventTarget eventTarget;
  private Map<String, Map<Integer, Breakpoint>> breakpoints;
  private BreakpointInfo currentBreakpointInfo;
  private volatile boolean stepMode = false;
  private boolean active = false;
  
  private final static Object obj = new Object();
  
  public DebugClient(HttpServletRequest req) throws IOException {
    this.req = req;
    this.breakpoints = new TreeMap<String, Map<Integer, Breakpoint>>();
  }
  
  public void setServletEventTarget(ServletEventTarget eventTarget) throws IOException {
    this.eventTarget = eventTarget;
    for (Cookie cookie: req.getCookies()) {
      if ("breakpoints".equals(cookie.getName())) {
        JSONObject breakpointJson = new JSONObject(URLDecoder.decode(cookie.getValue(), "UTF-8"));
        for (String key: breakpointJson.keySet()) {
          JSONObject breakpoint = breakpointJson.getJSONObject(key);
          setBreakpoint(
              breakpoint.getString("path"), 
              breakpoint.getInt("line"), 
              breakpoint.optInt("column", -1), 
              breakpoint.optString("condition", null), 
              breakpoint.optBoolean("active", true));
        }
        break;
      }
    }
  }
  
  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public void setBreakpoint(String path, int line, int column, String condition, boolean active) throws IOException {
    if (breakpoints.size() >= MAX_BREAKPOINTS) {
      throw new XSLWebException("Maximum number of breakpoints reached");
    }
    Map<Integer, Breakpoint> breakpointsForPath = breakpoints.get(path);
    if (breakpointsForPath == null) {
      breakpointsForPath = new TreeMap<Integer, Breakpoint>();
      breakpoints.put(path, breakpointsForPath);
    }
    breakpointsForPath.put(line, new Breakpoint(path, line, column, condition, active));
    send("setBreakpoint", path + "#" + line);
  }
  
  public void removeBreakpoint(String path, int line) throws IOException {
    Map<Integer, Breakpoint> breakpointsForPath = breakpoints.get(path);
    if (breakpointsForPath != null) {
      breakpointsForPath.remove(line);
      if (breakpointsForPath.isEmpty()) {
        breakpoints.remove(path);
      }
    }
    send("removeBreakpoint", path + "#" + line);
  }
  
  public void toggleBreakpoint(String path, int line) throws IOException {
    Map<Integer, Breakpoint> breakpointsForPath = breakpoints.get(path);
    if (breakpointsForPath != null) {
      Breakpoint breakpoint = breakpointsForPath.get(line);
      if (breakpoint != null) {
        breakpoint.setActive(!breakpoint.isActive());
      }
    }
    send("toggleBreakpoint", path + "#" + line);
  }
  
  public void setBreakpointCondition(String path, int line, String condition) throws IOException {
    Map<Integer, Breakpoint> breakpointsForPath = breakpoints.get(path);
    if (breakpointsForPath != null) {
      Breakpoint breakpoint = breakpointsForPath.get(line);
      if (breakpoint != null) {
        breakpoint.setCondition(condition);
      }
    }
    send("setBreakpointCondition", String.format("{\"path\":\"%s\",\"line\":%d,\"condition\":\"%s\"}", 
        StringEscapeUtils.escapeJson(path), line, StringEscapeUtils.escapeJson(condition)));
  }
  
  public void removeAllBreakpoints() throws IOException {
    breakpoints.clear();
    send("removeAllBreakpoints", "-");
  }
  
  public Integer[] getBreakpointLines(String path) {
    Map<Integer, Breakpoint> breakpointsForPath = breakpoints.get(path);
    if (breakpointsForPath == null) {
      return new Integer[] {};
    }
    Set<Integer> lines = breakpointsForPath.keySet();
    return lines.toArray(new Integer[lines.size()]);
  }
  
  public String getAllBreakpoints() {
    StringBuilder json = new StringBuilder();
    if (breakpoints.isEmpty()) {
      json.append("[]");
    } else {
      json.append("[");
      int recId = 1;
      for (Map.Entry<String, Map<Integer, Breakpoint>> entry : breakpoints.entrySet()) {
        for (Map.Entry<Integer, Breakpoint> entry2 : entry.getValue().entrySet()) {
          Breakpoint bp = entry2.getValue();
          json.append(
              String.format(
                  "{\"recid\":%d,\"path\":\"%s\",\"line\":%d,\"column\":%d,\"active\":%b,\"condition\":\"%s\"},", 
                  recId++, StringEscapeUtils.escapeJson(bp.getPath()), bp.getLine() + 1, bp.getColumn(), 
                  bp.isActive(), StringEscapeUtils.escapeJson((bp.getCondition() != null) ? bp.getCondition() : ""))); 
        }
      }
      json.deleteCharAt(json.length()-1);
      json.append("]");
    }
    return json.toString();
  }
  
  public void run() throws IOException {
    stepMode = false;
    try {
      synchronized (obj) {
        obj.notifyAll();
      }
      send("stateChanged", "run");
    } catch (IllegalMonitorStateException imse) {
      logger.error("Error initiating debug run mode", imse);
    }
  }
  
  public void step() throws IOException {
    stepMode = true;
    try {
      synchronized (obj) {
        obj.notifyAll();
      }
      send("stateChanged", "step");
    } catch (IllegalMonitorStateException imse) {
      logger.error("Error initiating debug step mode", imse);
    }
  }
  
  public String evaluateXPath(String expression) throws XPathException, SaxonApiException {
    if (currentBreakpointInfo == null || currentBreakpointInfo.getContext() == null) {
      return "No current context";
    }
    return EvaluateXPath.serializedStringValue(currentBreakpointInfo, currentBreakpointInfo.getContext(), expression);
  }
  
  public String getSerializedSequence(String id) throws SaxonApiException {
    if (currentBreakpointInfo == null || currentBreakpointInfo.getContext() == null) {
      return "No current context";
    }
    return currentBreakpointInfo.getSerializedSequence(id);
  }
  
  /*
  public void openPipeline() {
    try {
      send("openPipeline", "Pipeline opened");
    } catch (IOException ioe) {
      logger.error("Error notifying pipeline open", ioe);
    }
  }
  
  public void closePipeline() {
    try {
      send("closePipeline", "Pipeline closed");
    } catch (IOException ioe) {
      logger.error("Error notifying pipeline close", ioe);
    }
  }
  */
  
  public void sendError(String error) throws IOException {
    send("error", error);
  }
  
  public void sendMessage(String message) throws IOException {
    send("message", message);
  }
  
  public void send(String event, String data) throws IOException {
    eventTarget.send(event, data);
  }
  
  public void send(MessageEvent messageEvent) throws IOException {
    eventTarget.send(messageEvent);
  }
  
  public void open() {
    
  }
  
  public void close() {
    eventTarget.close();
  }
  
  public void breakThread(WebApp webApp, Traceable instruction, Map<String, Object> properties, XPathContext context, 
      Item currentItem, List<GlobalParam> globalParams, List<GlobalVariable> globalVariables, 
      String instructionLabel) throws IOException, SaxonApiException {
    Location loc = instruction.getLocation();
    String path = StringUtils.substringAfterLast(loc.getSystemId(), "/webapps");
    Integer line = loc.getLineNumber() - 1;
    Map<Integer, Breakpoint> breakpointsForPath = breakpoints.get(path);
    if (active && (stepMode || (breakpointsForPath != null && breakpointsForPath.containsKey(line)))) {
      Breakpoint breakpoint = null;
      if (breakpointsForPath != null) {
        breakpoint = breakpointsForPath.get(line);  
      }
      currentBreakpointInfo = new BreakpointInfo(path, webApp, instruction, properties, context, 
          currentItem, globalParams, globalVariables, instructionLabel);
      if (meetsCondition(currentBreakpointInfo, breakpoint)) {
        try {
          send("break", currentBreakpointInfo.toJSON());
          synchronized (obj) {
            obj.wait();
          }
        } catch (InterruptedException e)  {
          Thread.currentThread().interrupt();    
        }
      }
    }
  }
  
  private boolean meetsCondition(BreakpointInfo breakpointInfo, Breakpoint breakpoint) throws IOException {
    if (breakpoint == null) {
      return true;
    }
    String condition = breakpoint.getCondition();
    if (StringUtils.isBlank(condition)) {
      return true;
    }
    try {
      return EvaluateXPath.effectiveBooleanValue(breakpointInfo, breakpointInfo.getContext(), condition);
    } catch (Exception e) {
      send("conditionalBreakpointError", e.getMessage());
      return true;
    }
  }
  
  protected boolean isWebAppInDebugMode(String location) {
    if (!Context.getInstance().getDebugEnable()) {
      return false;
    }
    String name = StringUtils.substringBefore(location.substring(1), "/");
    WebApp webApp = null;
    for (WebApp app: Context.getInstance().getWebApps()) {
      if (app.getName().equals(name)) {
        webApp = app;
        break;
      }
    }
    if (webApp == null) {
      return false;
    }
    return webApp.getDebugMode();
  }
  
}