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

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XPath extension function that logs to Slf4J logging framework.
 * 
 * @author Maarten Kroon
 */
public class Log extends ExtensionFunctionDefinition {

  private static final Logger log = LoggerFactory.getLogger(Log.class);
  
  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_LOG, "log");

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
    return 3;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { 
        SequenceType.SINGLE_STRING, 
        SequenceType.makeSequenceType(AnyItemType.getInstance(), StaticProperty.ALLOWS_ZERO_OR_MORE),
        SequenceType.OPTIONAL_NODE };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.OPTIONAL_BOOLEAN;
  }
  
  @Override
  public boolean hasSideEffects() {
    return true;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new LogCall();
  }
  
  private static class LogCall extends ExtensionFunctionCall {

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
      try {
        String level = ((StringValue) arguments[0].head()).getStringValue();
        Properties props = null;
        if (arguments.length == 3) {
          props = getOutputProperties((NodeInfo) arguments[2].head());
        }
        StringWriter sw = new StringWriter();
        serialize(arguments[1], sw, props, getWebApp(context).getProcessor());  
        String message = sw.toString();
        if (level.equals("ERROR")) {
          log.error(message);
        } else if (level.equals("WARN")) {
          log.warn(message);
        } else if (level.equals("INFO")) {
          log.info(message);
        } else if (level.equals("DEBUG")) {
          log.debug(message);          
        } else {
          throw new XPathException(String.format("Level %s not supported", level));          
        }          
        return EmptySequence.getInstance();
      } catch (Exception e) {
        throw new XPathException("Could not log message", e);
      }
    }
  }
}