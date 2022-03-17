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
package nl.armatiek.xslweb.saxon.functions.script;

import java.util.Map;

import javax.script.ScriptEngine;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionDefinition;
import nl.armatiek.xslweb.saxon.utils.SaxonUtils;

/**
 * XPath extension function class 
 * 
 * @author Maarten Kroon
 */
public class GetScriptEngine extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_SCRIPT, "get-script-engine");

  public GetScriptEngine(Configuration configuration) {
    super(configuration);
  }
  
  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 1;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 3;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { 
        SequenceType.SINGLE_STRING,
        SequenceType.SINGLE_STRING,
        MapType.SINGLE_MAP_ITEM
    };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.makeSequenceType(new JavaExternalObjectType(configuration, ScriptEngine.class), StaticProperty.ALLOWS_ONE);
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new GetScriptEngineCall();
  }
  
  private static class GetScriptEngineCall extends ExtensionFunctionCall {

    @Override
    public ObjectValue<ScriptEngine> call(XPathContext context, Sequence[] arguments) throws XPathException {            
      try {
        String instanceName = ((StringValue) arguments[0].head()).getStringValue();
        String engineName = (arguments.length > 1) ? ((StringValue) arguments[1].head()).getStringValue() : "JavaScript"; 
        WebApp webapp = getWebApp(context);
        Map<String, Object> extraBindings = (arguments.length > 2) ? SaxonUtils.trieMapToMap((MapItem) arguments[2].head()) : null;
        return new ObjectValue<ScriptEngine>(webapp.getScriptEngine(instanceName, engineName, extraBindings));        
      } catch (Exception e) {
        throw new XPathException("Could not get script engine", e);
      }
    }
  }
}