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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.AtomicArray;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionDefinition;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class InvokeFunction extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_SCRIPT, "invoke-function");

  public InvokeFunction(Configuration configuration) {
    super(configuration);
  }  
  
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
        SequenceType.makeSequenceType(new JavaExternalObjectType(configuration, ScriptEngine.class), StaticProperty.ALLOWS_ONE),
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
    return new InvokeFunctionCall();
  }
  
  private static class InvokeFunctionCall extends ExtensionFunctionCall {
    
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
        ScriptEngine engine = ((ObjectValue<ScriptEngine>) arguments[0].head()).getObject();
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
        throw new XPathException("Error invoking script function", e);
      }
    }
  }
}