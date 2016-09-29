package nl.armatiek.xslweb.saxon.functions.common;

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

import java.util.Collection;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Attribute;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

public abstract class SetAttributeCall extends ExtensionFunctionCall {

  protected abstract void setAttributes(String name, Collection<Attribute> attrs, XPathContext context);
  
  @Override
  public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {            
    String name = ((StringValue) arguments[0].head()).getStringValue();    
    Collection<Attribute> attrs = null;
    if (arguments.length > 1) {
      attrs = sequenceToAttributeCollection(arguments[1]);
    }            
    setAttributes(name, attrs, context);
    return EmptySequence.getInstance();        
  }
}