package nl.armatiek.xslweb.saxon.functions.expath.file;

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

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;

/**
 * XPath extension function that returns the value of the operating system-specific 
 * line separator, which usually is &#10; on UNIX-based systems, &#13;&#10; on 
 * Windows systems and &#13; on Mac systems.
 * 
 * @author Maarten Kroon
 * @see <a href="http://expath.org/spec/file">EXPath File Module</a>
 */
public class LineSeparator extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_EXPATH_FILE, "line-separator");

  private static final String NEWLINE = String.format("%n", new Object[0]);
  
  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 0;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 0;
  }

  @Override
  public SequenceType[] getArgumentTypes() {    
    return new SequenceType[] { };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {    
    return SequenceType.SINGLE_STRING;
  }
  
  @Override
  public boolean hasSideEffects() {    
    return false;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new LineSeparatorCall();
  }
  
  private static class LineSeparatorCall extends FileExtensionFunctionCall {
        
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {                                                                             
      return new StringValue(NEWLINE);             
    } 
  }
}