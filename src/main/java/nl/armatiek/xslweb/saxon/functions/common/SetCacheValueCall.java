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
package nl.armatiek.xslweb.saxon.functions.common;

import java.util.ArrayList;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Attribute;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

public abstract class SetCacheValueCall extends ExtensionFunctionCall {

  protected abstract void setAttributes(String cacheName, String keyName, ArrayList<Attribute> attrs, 
      int tti, int ttl, XPathContext context);

  @Override
  public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {            
    String cacheName = ((StringValue) arguments[0].head()).getStringValue();
    String keyName = ((StringValue) arguments[1].head()).getStringValue();        
    ArrayList<Attribute> attrs = sequenceToAttributeCollection(arguments[2], context, true);
    int tti = -1;
    int ttl = -1;
    Item item;
    if (arguments.length > 3 && (item = (IntegerValue) arguments[3].head()) != null) {
      tti = (int) ((IntegerValue) item).longValue();
    }
    if (arguments.length > 4 && (item = (IntegerValue) arguments[4].head()) != null) {
      ttl = (int) ((IntegerValue) item).longValue();
    }      
    setAttributes(cacheName, keyName, attrs, tti, ttl, context);
    return EmptySequence.getInstance();
  }

}