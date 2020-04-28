package nl.armatiek.xslweb.saxon.functions.response;

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

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

public class AddDateHeader extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_RESPONSE, "add-date-header");

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
    return 2;
  }

  @Override
  public SequenceType[] getArgumentTypes() {    
    return new SequenceType[] { SequenceType.SINGLE_STRING, 
        SequenceType.makeSequenceType(BuiltInAtomicType.DATE_TIME, StaticProperty.ALLOWS_ONE) };
  }
  
  @Override
  public boolean hasSideEffects() {    
    return true;
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {    
    return SequenceType.OPTIONAL_BOOLEAN;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new AddDateHeaderCall();
  }
  
  private static class AddDateHeaderCall extends ExtensionFunctionCall {
        
    @Override
    public Sequence<?> call(XPathContext context, Sequence[] arguments) throws XPathException {            
      String name = ((StringValue) arguments[0].head()).getStringValue();      
      long date = ((DateTimeValue) arguments[1].head()).getCalendar().getTime().getTime();      
      getResponse(context).setDateHeader(name, date);                                                            
      return EmptySequence.getInstance();
    } 
  }
}