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
package nl.armatiek.xslweb.saxon.functions.dynfunc;

import org.apache.commons.lang3.ArrayUtils;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.QualifiedNameValue;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class Call extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_DYNFUNC, "call");

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
    return 17;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { SequenceType.SINGLE_QNAME, 
        SequenceType.ANY_SEQUENCE, SequenceType.ANY_SEQUENCE, SequenceType.ANY_SEQUENCE, SequenceType.ANY_SEQUENCE, 
        SequenceType.ANY_SEQUENCE, SequenceType.ANY_SEQUENCE, SequenceType.ANY_SEQUENCE, SequenceType.ANY_SEQUENCE, 
        SequenceType.ANY_SEQUENCE, SequenceType.ANY_SEQUENCE, SequenceType.ANY_SEQUENCE, SequenceType.ANY_SEQUENCE,
        SequenceType.ANY_SEQUENCE, SequenceType.ANY_SEQUENCE, SequenceType.ANY_SEQUENCE, SequenceType.ANY_SEQUENCE, };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.ANY_SEQUENCE;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new CallFunctionCall();
  }
  
  private static class CallFunctionCall extends ExtensionFunctionCall {
    
    @Override
    public Sequence<?> call(XPathContext context, Sequence[] arguments) throws XPathException {            
      String functionName = ((QualifiedNameValue) arguments[0].head()).getClarkName();        
      ExtensionFunctionDefinition funcDef = getWebApp(context).getExtensionFunctionDefinition(functionName);
      if (funcDef == null) {
        throw new XPathException("No function with name \"" + functionName + "\" is registered", "DF010");
      }
      return funcDef.makeCallExpression().call(context, ArrayUtils.subarray(arguments, 1, arguments.length));
    }
    
  }
  
}