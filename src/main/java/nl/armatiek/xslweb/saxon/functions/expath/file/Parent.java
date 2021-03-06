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

import java.io.File;
import java.io.IOException;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FileException;

/**
 * XPath extension function that transforms the given path into an absolute path, 
 * as specified by file:resolve-path, and returns the parent directory.
 * 
 * @author Maarten Kroon
 * @see <a href="http://expath.org/spec/file">EXPath File Module</a>
 */
public class Parent extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_EXPATH_FILE, "parent");

  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 1;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 1;
  }

  @Override
  public SequenceType[] getArgumentTypes() {    
    return new SequenceType[] { SequenceType.SINGLE_STRING };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {    
    return SequenceType.OPTIONAL_STRING;
  }
  
  @Override
  public boolean hasSideEffects() {    
    return false;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new ParentCall();
  }
  
  private static class ParentCall extends FileExtensionFunctionCall {
        
    @Override
    public ZeroOrOne<StringValue> call(XPathContext context, Sequence[] arguments) throws XPathException {
      try {
        String path = ((StringValue) arguments[0].head()).getStringValue();
        if (path.equals("/")) {
          return new ZeroOrOne<StringValue>(null);
        }
        File file = getFile(path);
        try {
          File parent = new File(file.getParent());
          return new ZeroOrOne<StringValue>(new StringValue(parent.getCanonicalPath() + File.separatorChar));
        } catch (IOException e) {
        }
        return null;
      } catch (Exception e) {
        throw new FileException("Other file error", e, FileException.ERROR_IO);
      }
    }
  }
}