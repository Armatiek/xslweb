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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import net.sf.saxon.expr.StackFrame;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Actor;
import net.sf.saxon.expr.instruct.AttributeSet;
import net.sf.saxon.expr.instruct.GlobalParam;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.NamedTemplate;
import net.sf.saxon.expr.instruct.ParameterSet;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.instruct.TemplateRule;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.sort.GroupIterator;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.ContextStackFrame;
import net.sf.saxon.trace.ContextStackFrame.ApplyTemplates;
import net.sf.saxon.trace.ContextStackFrame.BuiltInTemplateRule;
import net.sf.saxon.trace.ContextStackFrame.CallTemplate;
import net.sf.saxon.trace.ContextStackFrame.CallingApplication;
import net.sf.saxon.trace.ContextStackFrame.FunctionCall;
import net.sf.saxon.trace.ContextStackFrame.VariableEvaluation;
import net.sf.saxon.trace.Traceable;
import net.sf.saxon.trans.KeyDefinition;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.WebApp;

public class BreakpointInfo {
  
  private String path;
  private WebApp webApp;
  private Traceable instruction;
  private Map<String, Object> properties;
  private XPathContext context;
  private Item currentItem;
  private List<GlobalParam> globalParams;
  private List<GlobalVariable> globalVariables;
  private String instructionLabel;
  private HashMap<String, Sequence> sequenceMap = new HashMap<String, Sequence>();
  
  public BreakpointInfo(String path, WebApp webApp, Traceable instruction, Map<String, Object> properties, 
      XPathContext context, Item currentItem, List<GlobalParam> globalParams, 
      List<GlobalVariable> globalVariables, String instructionLabel) {
    this.path = path;
    this.webApp = webApp;
    this.instruction = instruction;
    this.properties = properties;
    this.context = context;
    this.currentItem = currentItem;
    this.globalParams = globalParams;
    this.globalVariables = globalVariables;
    this.instructionLabel = instructionLabel;
  }
  
  public WebApp getWebApp() {
    return webApp;
  }

  public Traceable getInstruction() {
    return instruction;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public XPathContext getContext() {
    return context;
  }
  
  public Item getCurrentItem() {
    return currentItem;
  }
  
  public List<GlobalParam> getGlobalParams() {
    return globalParams;
  }
  
  public List<GlobalVariable> getGlobalVariables() {
    return globalVariables;
  }
 
  public String getSerializedSequence(String id) throws Exception {
    return DebugUtils.getDisplayText(webApp, sequenceMap.get(id), "expanded");
  }
  
  public String toJSON() throws Exception {
    sequenceMap.clear();
    Location loc = instruction.getLocation();
    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append("\"id\":\"" + StringEscapeUtils.escapeJson(UUID.randomUUID().toString())  + "\",");
    json.append("\"path\":\"" + StringEscapeUtils.escapeJson(path)  + "\",");
    json.append("\"line\":" + Integer.toString(instruction.getLocation().getLineNumber()-1)  + ",");
    json.append("\"column\":" + Integer.toString(instruction.getLocation().getColumnNumber()-1) + ",");
    json.append("\"variablesRecords\": [");
    int recId = 1;
    if (context != null) { 
      Set<StructuredQName> localParamNames = new HashSet<StructuredQName>();
      ParameterSet locals = context.getLocalParameters();
      if (locals != null && locals.size() > 0) {
        addRecord(json, recId++, "Local parameters", "", false, true);
        recId = showParameterSet(context.getLocalParameters(), json, recId, localParamNames, null);
      } 
      Set<StructuredQName> tunnelParamNames = new HashSet<StructuredQName>();
      ParameterSet tunnels = context.getTunnelParameters();
      if (tunnels != null && tunnels.size() > 0) {
        addRecord(json, recId++, "Tunnel parameters", "", false, true);
        recId = showParameterSet(context.getTunnelParameters(), json, recId, tunnelParamNames, null);
      } 
      StackFrame stack = context.getStackFrame();
      SlotManager map = stack.getStackFrameMap();
      Sequence[] values = stack.getStackFrameValues();
      if (map.getNumberOfVariables() > 0) {
        boolean foundFirst = false;
        List<RecordInfo> records = new ArrayList<RecordInfo>();
        for (int i = 0; i < map.getNumberOfVariables(); i++) {
          if (values[i] != null) {
            StructuredQName name = (StructuredQName) map.getVariableMap().get(i);
            if (!localParamNames.contains(name) && !tunnelParamNames.contains(name)) {
              if (!foundFirst) {
                addRecord(json, recId++, "Local variables", "", false, true);
              }
              Sequence value = values[i];
              records.add(new RecordInfo("$" + name.getDisplayName(), value));
              foundFirst = true;
            }
          } 
        }
        Collections.sort(records);
        for (RecordInfo recordInfo: records) {
          addRecord(json, recId++, recordInfo.key, DebugUtils.getDisplayText(webApp, recordInfo.value, "compact"), 
              addToSequenceMap(recordInfo.value), false, false);
        }
        
      } 
    }
   
    if (!globalVariables.isEmpty()) {
      addRecord(json, recId++, "Global variables", "", false, true);
      List<RecordInfo> records = new ArrayList<RecordInfo>();
      for (GlobalVariable var: globalVariables) {
        Sequence value;
        try {
          value = var.evaluateVariable(context);
        } catch (XPathException xpe) {
          value = new StringValue("ERROR: " + xpe.getMessage());
        }
        records.add(new RecordInfo("$" + var.getVariableQName().getDisplayName(), value));
      }
      Collections.sort(records);
      for (RecordInfo recordInfo: records) {
        addRecord(json, recId++, recordInfo.key, DebugUtils.getDisplayText(webApp, recordInfo.value, "compact"), 
            addToSequenceMap(recordInfo.value), false, false);
      }
    }
    
    Map<StructuredQName, Sequence> params = new HashMap<StructuredQName, Sequence>();
    for (StructuredQName name: context.getController().getExecutable().getGlobalParameters().keySet()) {
      Sequence value = context.getController().getParameter(name);
      if (!(value instanceof ObjectValue)) {
        params.put(name, value);
      }
    }
    
    for (GlobalParam param : globalParams) {
      Sequence value;
      try {
        value = param.evaluateVariable(context);
      } catch (XPathException xpe) {
        value = new StringValue("ERROR: " + xpe.getMessage());
      }
      params.put(param.getVariableQName(), value);
    }
    
    if (!params.isEmpty()) {
      addRecord(json, recId++, "Global parameters", "", false, true);
      List<RecordInfo> records = new ArrayList<RecordInfo>();
      for (Map.Entry<StructuredQName, Sequence> entry : params.entrySet()) {
        StructuredQName name = entry.getKey();
        Sequence value = entry.getValue();
        records.add(new RecordInfo("$" + name.getDisplayName(), value));
      }
      Collections.sort(records);
      for (RecordInfo recordInfo: records) {
        addRecord(json, recId++, recordInfo.key, DebugUtils.getDisplayText(webApp, recordInfo.value, "compact"), 
            addToSequenceMap(recordInfo.value), false, false);
      }
    }
    
    addRecord(json, recId++, "", "", true, false);
    
    json.append("],");
    
    /* Expression: */
    json.append("\"expressionRecords\": [");
    addRecord(json, recId++, "Expression", StringEscapeUtils.escapeJson(instructionLabel), false, false);
    addRecord(json, recId++, "Mode", StringEscapeUtils.escapeJson(getMode(context)), false, false);
    addRecord(json, recId++, "Line" , Integer.toString(loc.getLineNumber()), false, false);
    addRecord(json, recId++, "Column" , Integer.toString(loc.getColumnNumber()), false, false);
    addRecord(json, recId++, "System id" , StringUtils.defaultString(loc.getSystemId()), false, false);
    addRecord(json, recId++, "Public id" , StringUtils.defaultString(loc.getPublicId()), true, false);
    //BiConsumer<String, Object> f =  (key, value) -> addRecord(json, recId++, key, value.toString(), false, false);
    //instruction.gatherProperties(f);
    json.append("],");
    
    /* Context: */
    json.append("\"contextRecords\": ["); 
    if (context.getContextItem() == null) {
      addRecord(json, recId++, "Context item", "[no context item]", true, false);
    } else {
      AtomicSequence groupingKey = null;
      GroupIterator groupIterator = context.getCurrentGroupIterator();
      if (groupIterator != null) {
        groupingKey = groupIterator.getCurrentGroupingKey();
      }
      Sequence contextItem = context.getContextItem();
      if (contextItem instanceof NodeInfo) {
        String xpath = Navigator.getPath((NodeInfo) context.getContextItem(), context);
        addRecord(json, recId++, "XPath of context item", xpath, null, false, false);  
      }
      addRecord(json, recId++, "Context item", DebugUtils.getDisplayText(webApp, contextItem, "compact"), 
          addToSequenceMap(contextItem), false, false);
      addRecord(json, recId++, "Context position", Integer.toString(context.getCurrentIterator().position()), false, false); 
      if (groupingKey != null) {
        addRecord(json, recId++, "Grouping key", DebugUtils.getDisplayText(webApp, groupingKey, "compact"), 
            addToSequenceMap(groupingKey), false, false);
      }
      String sysId = null;
      int line = -1;
      int col = -1;
      if (contextItem instanceof NodeInfo) {
        NodeInfo nodeInfo = (NodeInfo) contextItem;
        sysId = nodeInfo.getSystemId();
        line = nodeInfo.getLineNumber();
        col = nodeInfo.getColumnNumber(); 
      }
      addRecord(json, recId++, "Source file", StringUtils.isNotEmpty(sysId) ? sysId : "n/a", false, false);
      addRecord(json, recId++, "Line number", (line > -1) ? Integer.toString(line) : "n/a", false, false); 
      addRecord(json, recId++, "Column number", (col > -1) ? Integer.toString(col) : "n/a", true, false);
    }
    json.append("],");
    
    /* Stack: */
    recId = 1;
    json.append("\"stackRecords\": ["); 
    
    ArrayList<ContextStackFrame> frameList = new ArrayList<ContextStackFrame>();
    @SuppressWarnings("unchecked")
    Iterator<ContextStackFrame> frameIter = context.iterateStackFrames();
    while (frameIter.hasNext()) {
      frameList.add(frameIter.next());
    }
    if (!frameList.isEmpty()) {
      frameList.remove(frameList.size()-1);
    }
    
    json.append(
        String.format(
            "{\"recid\":%d,\"container\":\"%s\",\"path\":\"%s\",\"line\":%d,\"mode\":\"%s\",\"context\":\"%s\",\"contextExpanded\":\"%s\"}", 
            recId++,
            StringEscapeUtils.escapeJson(instructionLabel),
            StringEscapeUtils.escapeJson(path),
            loc.getLineNumber() - 1,
            StringEscapeUtils.escapeJson(getMode(context)),
            StringEscapeUtils.escapeJson(DebugUtils.getDisplayText(webApp, context.getContextItem(), "compact")),
            StringEscapeUtils.escapeJson(addToSequenceMap(context.getContextItem()))
        )
    );
    
    if (!frameList.isEmpty()) {
      json.append(",");
    }
    
    Iterator<ContextStackFrame> iter = frameList.iterator();
    while (iter.hasNext()) {
      ContextStackFrame frame = iter.next();
      String label = "";
      if (frame instanceof CallingApplication) {
      } else if (frame instanceof BuiltInTemplateRule) {
        Item contextItem = context.getContextItem();
        if (contextItem instanceof NodeInfo) {
          label = Navigator.getPath((NodeInfo) contextItem);
        } else if (contextItem instanceof AtomicValue) {
          label = "value " + contextItem.toString();
        } else if (contextItem instanceof MapItem) {
          label = "map";
        } else if (contextItem instanceof ArrayItem) {
          label = "array";
        } else if (contextItem instanceof Function) {
          label = "function";
        } else {
          label = "item";
        }
        label = "built-in template rule for " + label;
      } else if (frame instanceof FunctionCall) {
        FunctionCall functionCall = (FunctionCall) frame;
        label = (functionCall.getFunctionName() == null) ? "(anonymous)" : functionCall.getFunctionName().getDisplayName() + "()";
      } else if (frame instanceof ApplyTemplates) {
        label = "xsl:apply-templates";
      } else if (frame instanceof CallTemplate) {
        CallTemplate callTemplate = (CallTemplate) frame;
        String name = callTemplate.getTemplateName() == null ? "??" : callTemplate.getTemplateName().getDisplayName();
        label = "xsl:call-template name=\"" + name + "\"";
      } else if (frame instanceof VariableEvaluation) {
        Object container = frame.getContainer();
        if (container instanceof Actor) {
          StructuredQName name = ((Actor) container).getComponentName();
          String objectName = name == null ? "" : name.getDisplayName();
          if (container instanceof NamedTemplate) {
            label = "xsl:template name=\"" + objectName + "\"";
          } else if (container instanceof UserFunction) {
            label = "xsl:function " + objectName + "()";
          } else if (container instanceof AttributeSet) {
            label = "xsl:attribute-set " + objectName;
          } else if (container instanceof KeyDefinition) {
            label = "xsl:key " + objectName;
          } else if (container instanceof GlobalVariable) {
            StructuredQName qName = ((GlobalVariable) container).getVariableQName();
            if (qName.hasURI(NamespaceConstant.SAXON_GENERATED_VARIABLE)) {
              label = "optimizer-created global variable";
            } else {
              label = "global variable $" + qName.getDisplayName();
            }
          }
        } else if (container instanceof TemplateRule) {
          label = "xsl:template match=\"" + ((TemplateRule) container).getMatchPattern().toString() + "\"";
        }
      }
      
      Item contextItem = frame.getContextItem();
      int line;
      try {
        line = frame.getLineNumber();
      } catch (NullPointerException npe) {
        line = -1;
      }
      
      String systemId;
      try {
        systemId = frame.getSystemId();
      } catch (NullPointerException npe) {
        systemId = null;
      }
      
      String path = (systemId == null) ? "[unknown]" : StringUtils.substringAfterLast(systemId, "/webapps");
      
      json.append(
          String.format(
              "{\"recid\":%d,\"container\":\"%s\",\"path\":\"%s\",\"line\":%d,\"mode\":\"%s\",\"context\":\"%s\",\"contextExpanded\":\"%s\"}", 
              recId++,
              StringEscapeUtils.escapeJson(label),
              StringEscapeUtils.escapeJson(path),
              line - 1,
              StringEscapeUtils.escapeJson(getMode(frame.getContext())),
              StringEscapeUtils.escapeJson(DebugUtils.getDisplayText(webApp, contextItem, "compact")),
              StringEscapeUtils.escapeJson(addToSequenceMap(contextItem))
          )
      );
      
      if (iter.hasNext()) {
        json.append(",");
      }
      
    }
    json.append("]");
    json.append("}");
    return json.toString();
  }
  
  private String addToSequenceMap(Sequence sequence) {
    String key = UUID.randomUUID().toString();
    sequenceMap.put(key, sequence);
    return key;
  }
  
  private int showParameterSet(ParameterSet params, StringBuilder json, int recId, Set<StructuredQName> paramNames, Set<StructuredQName> skipNames) throws Exception {
    ParameterSet ps;
    try {
      ps = new ParameterSet(params, 0);
      ps.materializeValues();
    } catch (XPathException err) {
      ps = params;
    }
    List<RecordInfo> records = new ArrayList<RecordInfo>();
    for (int i=0; i<ps.size();i++) {
      StructuredQName paramName = ps.getParameterNames()[i];
      if (skipNames != null && skipNames.contains(paramName)) {
        continue;
      }
      if (paramNames != null) {
        paramNames.add(paramName);
      }
      Sequence value = ps.getValue(i);
      records.add(new RecordInfo("$" + paramName.getDisplayName(), value));
    }
    Collections.sort(records);
    for (RecordInfo recordInfo: records) {
      addRecord(json, recId++, recordInfo.key, DebugUtils.getDisplayText(webApp, recordInfo.value, "compact"), 
          this.addToSequenceMap(recordInfo.value), false, false);  
    }
    return recId;
  }
  
  private void addRecord(StringBuilder json, int recId, String label, String value, String expandedValue, boolean isLast, boolean isSection) {
    json.append(String.format("{\"recid\":%d,\"label\":\"%s\",\"value\":\"%s\"", recId, StringEscapeUtils.escapeJson(label), StringEscapeUtils.escapeJson(value)));
    if (expandedValue != null) {
      json.append(String.format(",\"expandedValue\":\"%s\"", StringEscapeUtils.escapeJson(expandedValue)));
    }
    if (isSection) {
      json.append(",\"w2ui\":{\"style\":\"font-weight:bold\"}");
    } else {
      json.append(",\"w2ui\":{\"style\":\"margin-left:2em\"}");
    }
    json.append('}');
    if (!isLast) {
      json.append(',');
    }
  }
  
  private void addRecord(StringBuilder json, int recId, String label, String value, boolean isLast, boolean isSection) {
    addRecord(json, recId, label, value, null, isLast, isSection);
  }
  
  private String getMode(XPathContext context) {
    try {
      String mode = context.getCurrentMode().getActor().getModeName().getDisplayName();
      if (mode.equals("xsl:unnamed")) {
        return "#default";
      }
      return mode;
    } catch (NullPointerException npe) {
      return "[unknown]";
    }
  }
  
  private static class RecordInfo implements Comparable<RecordInfo> {
    
    public String key;
    public Sequence value;
    
    public RecordInfo(String key, Sequence value) {
      this.key = key;
      this.value = value;
    }
    
    @Override
    public int compareTo(RecordInfo ri) {
      return this.key.compareTo(ri.key);
    }
    
  }
  
}