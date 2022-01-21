package nl.armatiek.xslweb.saxon.functions.script;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.AtomicArray;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class Invoke extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_SCRIPT, "invoke");

  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 2;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 10;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { 
        SequenceType.SINGLE_STRING, 
        SequenceType.SINGLE_STRING, 
        SequenceType.ATOMIC_SEQUENCE,
        SequenceType.ATOMIC_SEQUENCE,
        SequenceType.ATOMIC_SEQUENCE,
        SequenceType.ATOMIC_SEQUENCE,
        SequenceType.ATOMIC_SEQUENCE,
        SequenceType.ATOMIC_SEQUENCE,
        SequenceType.ATOMIC_SEQUENCE,
        SequenceType.ATOMIC_SEQUENCE};
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.ATOMIC_SEQUENCE;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new InvokeCall();
  }
  
  private static class InvokeCall extends ExtensionFunctionCall {
    
    private static ScriptEngine engine;
    
    private synchronized ScriptEngine getScriptEngine() {
      if (engine == null) {
        ScriptEngineManager factory = new ScriptEngineManager();
        engine = factory.getEngineByName("JavaScript");
      }
      return engine;
    }
    
    private Object[] sequenceToObjectArray(Sequence seq) throws XPathException {
      ArrayList<Object> objectList = new ArrayList<Object>();
      SequenceIterator iter = seq.iterate();
      Item item;
      while ((item = iter.next()) != null) {
        objectList.add(SequenceTool.convertToJava(item));
      }
      return objectList.toArray(new Object[objectList.size()]);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {            
      try {
        String script = ((StringValue) arguments[0].head()).getStringValue();        
        String functionName = ((StringValue) arguments[1].head()).getStringValue();
    
        ArrayList<Object> args = new ArrayList<Object>();                
        args.add(Context.getInstance());
        args.add(getWebApp(context));
        args.add(getRequest(context));
        args.add(getResponse(context));
        
        for (int i=2; i<arguments.length; i++) {
          Sequence seq = arguments[i];                    
          args.add(sequenceToObjectArray(seq));          
        }
        
        ScriptEngine engine = getScriptEngine();
        engine.eval(script);
        Invocable inv = (Invocable) engine;                                              
        Object result = inv.invokeFunction(functionName, args.toArray(new Object[args.size()]));
        
        if (result instanceof Collection) { /* Rhino */
          ArrayList<AtomicValue> valueList = new ArrayList<AtomicValue>();
          for (Object obj : ((Collection<Object>) result)) {
            valueList.add(convertJavaObjectToAtomicValue(obj));
          }
          return new AtomicArray(valueList);
        } else if (result instanceof Map) { /* Nashorn */
          ArrayList<AtomicValue> valueList = new ArrayList<AtomicValue>();
          for (Object obj : ((Map<String, Object>) result).values()) {
            valueList.add(convertJavaObjectToAtomicValue(obj));
          }          
          return new AtomicArray(valueList);
        } else if (result instanceof Object[]) {
          ArrayList<AtomicValue> valueList = new ArrayList<AtomicValue>();
          for (Object obj : (Object[]) result) {
            valueList.add(convertJavaObjectToAtomicValue(obj));
          }
          return new AtomicArray(valueList);
        } else {
          return convertJavaObjectToAtomicValue(result);
        }       
      } catch (Exception e) {
        throw new XPathException("Error running script", e);
      }
    }
  }
}