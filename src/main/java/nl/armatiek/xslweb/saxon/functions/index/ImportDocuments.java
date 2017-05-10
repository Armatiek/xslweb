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

package nl.armatiek.xslweb.saxon.functions.index;

import java.nio.file.Paths;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xmlindex.Session;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

/**
 * 
 * @author Maarten Kroon
 */
public class ImportDocuments extends ExtensionFunctionDefinition {
  
  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_XMLINDEX, "import-documents");

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
    return 4;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { 
        SequenceType.makeSequenceType(new JavaExternalObjectType(Session.class), StaticProperty.ALLOWS_ONE),
        SequenceType.SINGLE_STRING,
        SequenceType.SINGLE_INTEGER,
        SequenceType.SINGLE_STRING };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.OPTIONAL_BOOLEAN;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new ImportDocumentsCall();
  }
  
  @Override
  public boolean hasSideEffects() {
    return true;
  }
  
  private static class ImportDocumentsCall extends ExtensionFunctionCall {

    @Override
    public ZeroOrOne<BooleanValue> call(XPathContext context, Sequence[] arguments) throws XPathException {            
      @SuppressWarnings("unchecked")
      Session session = ((ObjectValue<Session>) arguments[0].head()).getObject();
      String path = ((StringValue) arguments[1].head()).getStringValue();
      int length = arguments.length;
      int maxDepth = Integer.MAX_VALUE;
      String pattern = null;
      if (length > 2)
        maxDepth = (int) ((Int64Value) arguments[2].head()).longValue();
      if (length > 3)
        pattern = ((StringValue) arguments[3].head()).getStringValue();
      try {
        session.addDocuments(Paths.get(path), maxDepth, pattern);
        return ZeroOrOne.empty();
      } catch (Exception e) {
        throw new XPathException("Error importing documents from \"" + path + "\"", e);
      }
    }
  }
  
}