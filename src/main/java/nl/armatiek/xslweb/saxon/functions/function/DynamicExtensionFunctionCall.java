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
package nl.armatiek.xslweb.saxon.functions.function;

import java.util.ArrayList;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.ZeroOrMore;
import net.sf.saxon.trans.XPathException;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

public abstract class DynamicExtensionFunctionCall extends ExtensionFunctionCall {

  protected Item toItem(Object obj) throws XPathException {
    return (obj instanceof Item) ? (Item) obj : convertJavaObjectToAtomicValue(obj);
  }
  
  protected ZeroOrMore<Item<?>> convertToZeroOrMore(Object obj) throws XPathException {
    ArrayList<Item<?>> results = new ArrayList<Item<?>>();
    if (obj.getClass().isArray()) {
      Object[] objects = (Object[]) obj;
      for (Object o : objects) {
        results.add(toItem(o));
      }
    } else {
      results.add(toItem(obj));
    }
    return new ZeroOrMore<Item<?>>(results);
  }

}