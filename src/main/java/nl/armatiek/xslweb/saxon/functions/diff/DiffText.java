package nl.armatiek.xslweb.saxon.functions.diff;

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

import java.util.LinkedList;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExplicitLocation;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.linked.LinkedTreeBuilder;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;
import com.sksamuel.diffpatch.DiffMatchPatch;
import com.sksamuel.diffpatch.DiffMatchPatch.Diff;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class DiffText extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_DIFF, "diff-text");

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
    return 2;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.SINGLE_NODE;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new DiffTextCall();
  }

  private static class DiffTextCall extends ExtensionFunctionCall {

    @Override
    public ZeroOrOne<NodeInfo> call(XPathContext context, Sequence[] arguments) throws XPathException {
      try {
        String text1 = ((StringValue) arguments[0].head()).getStringValue();
        String text2 = ((StringValue) arguments[1].head()).getStringValue();
        
        DiffMatchPatch dmp = new DiffMatchPatch();
        
        LinkedList<DiffMatchPatch.Diff> diffs = dmp.diff_main(text1, text2);
        
        LinkedTreeBuilder builder = (LinkedTreeBuilder) TreeModel.LINKED_TREE.makeBuilder(context.getController().makePipelineConfiguration());
        builder.setLineNumbering(false);
        builder.open();
        builder.startDocument(0);
        builder.startElement(new NoNamespaceName("diff"), AnyType.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0);
        builder.startContent();
        for (Diff diff : diffs) {
          String tagName = null;
          switch (diff.operation) {
          case INSERT:
            tagName = "ins";
            break;
          case DELETE:
            tagName = "del";
            break;
          case EQUAL:
            tagName = "eq";
            break;
          } 
          builder.startElement(new NoNamespaceName(tagName), AnyType.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0);
          builder.startContent();
          builder.characters(diff.text, ExplicitLocation.UNKNOWN_LOCATION, 0);
          builder.endElement();
        }
        builder.endElement();
        builder.endDocument();
        builder.close();
        NodeInfo nodeInfo = builder.getCurrentRoot();
        return new ZeroOrOne<NodeInfo>(nodeInfo);
      } catch (Exception e) {
        throw new XPathException("Error differencing nodes", e);
      }
    }
  }

}