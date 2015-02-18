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

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FileException;

/**
 * XPath extension function that creates a temporary directory and all non-existing 
 * parent directories and returns the full path to the created directory.
 * 
 * @author Maarten Kroon
 * @see <a href="http://expath.org/spec/file">EXPath File Module</a>
 */
public class CreateTempDir extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_EXPATH_FILE, "create-temp-dir");

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
        SequenceType.SINGLE_STRING,
        SequenceType.SINGLE_STRING };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {    
    return SequenceType.SINGLE_STRING;
  }
  
  @Override
  public boolean hasSideEffects() {    
    return true;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new CreateTempDirCall();
  }
  
  private static class CreateTempDirCall extends FileExtensionFunctionCall {
        
    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {      
      try {   
        String prefix = ((StringValue) arguments[0].head()).getStringValue();
        String suffix = ((StringValue) arguments[1].head()).getStringValue();
        File dir = null;
        if (arguments.length == 3) {
          dir = getFile(((StringValue) arguments[2].head()).getStringValue());
        }
        File test;
        if (dir == null) {
          test = File.createTempFile(prefix, suffix);
        } else {
          if (dir.isFile()) {
            throw new FileException(String.format("Specified directory \"%s\" points to an existing file", 
                dir.getAbsolutePath()), FileException.ERROR_PATH_EXISTS);
          }
          test = File.createTempFile(prefix, suffix, dir);
        }
        String path = test.getCanonicalPath();
        test.delete();
        File temp = new File(path);
        temp.mkdirs();
        temp.deleteOnExit();
        return StringValue.makeStringValue(temp.getCanonicalPath());
      } catch (FileException fe) {
        throw fe;
      } catch (Exception e) {
        throw new FileException("Other file error", e, FileException.ERROR_IO);
      }
    } 
  }
}