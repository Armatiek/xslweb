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
package nl.armatiek.xslweb.saxon.functions.webapp;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class RemoveCacheValue extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_WEBAPP, "remove-cache-value");

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
    return new SequenceType[] { 
        SequenceType.SINGLE_STRING,
        SequenceType.SINGLE_STRING};
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.OPTIONAL_BOOLEAN;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new RemoveCacheValueCall();
  }
  
  private static class RemoveCacheValueCall extends ExtensionFunctionCall {

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {            
      try {
        String cacheName = ((StringValue) arguments[0].head()).getStringValue();
        String keyName = ((StringValue) arguments[1].head()).getStringValue();
        getWebApp(context).removeCacheValue(cacheName, keyName);
        return EmptySequence.getInstance();        
      } catch (Exception e) {
        throw new XPathException("Could not remove element from cache", e);
      }
    }
  }
}