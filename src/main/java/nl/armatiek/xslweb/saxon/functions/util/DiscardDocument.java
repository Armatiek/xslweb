package nl.armatiek.xslweb.saxon.functions.util;

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

import net.sf.saxon.Controller;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.DocumentURI;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Definitions;

/**
 * XPath extension function class for
 * 
 * @author Maarten Kroon
 */
public class DiscardDocument extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_UTIL, "discard-document");

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
    return 1;
  }

  @Override
  public SequenceType[] getArgumentTypes() {    
    return new SequenceType[] { SequenceType.OPTIONAL_DOCUMENT_NODE };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.OPTIONAL_DOCUMENT_NODE;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new DiscardDocumentCall();
  }
  
  private static class DiscardDocumentCall extends ExtensionFunctionCall {

    @Override
    public ZeroOrOne<NodeInfo> call(XPathContext context, Sequence[] arguments) throws XPathException {            
      if (arguments.length == 0) {
        return new ZeroOrOne<NodeInfo>(null);
      }
      DocumentInfo doc = ((DocumentInfo) arguments[0].head()); 
      if (doc == null) {
        return new ZeroOrOne<NodeInfo>(null);
      }
      Controller c = context.getController();
      String uri = c.getDocumentPool().getDocumentURI(doc);
      if (uri != null) {
        c.removeUnavailableOutputDestination(new DocumentURI(uri));
      }
      c.getDocumentPool().discard(doc.getTreeInfo());
      return new ZeroOrOne<NodeInfo>(doc);
    }
  }
}