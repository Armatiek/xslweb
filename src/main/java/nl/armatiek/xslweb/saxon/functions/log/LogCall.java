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
package nl.armatiek.xslweb.saxon.functions.log;

import java.io.StringWriter;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

public abstract class LogCall extends ExtensionFunctionCall {
  
  protected static final Logger log = LoggerFactory.getLogger(LogCall.class);
  
  protected abstract void log(String message);
  
  @Override
  public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
    try {
      Sequence result = arguments[0];
      Properties props = null;
      if (arguments.length == 3) {
        props = getOutputProperties((NodeInfo) arguments[2].head());
      }
      StringWriter sw = new StringWriter();
      serialize(arguments[1], sw, props, getWebApp(context).getProcessor());  
      String message = sw.toString();
      log(message);
      return result;
    } catch (Exception e) {
      throw new XPathException("Could not log message", e);
    }
  }
}
