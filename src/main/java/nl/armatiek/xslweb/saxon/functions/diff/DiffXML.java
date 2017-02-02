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

import java.util.HashMap;
import java.util.Map;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;
import nl.armatiek.xslweb.saxon.functions.diff.node.Document;
import nl.armatiek.xslweb.saxon.functions.diff.patch.Patch;
import nl.armatiek.xslweb.saxon.functions.diff.util.DiffUtils;
import nl.armatiek.xslweb.saxon.functions.diff.xydiff.DeltaConstructor;
import nl.armatiek.xslweb.saxon.functions.diff.xydiff.XyDiff;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class DiffXML extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_DIFF, "diff-xml");

  public enum WhitespaceHandlingMethod {
    ALL,
    IGNORABLE,
    NONE 
  }
  
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
    return new SequenceType[] { SequenceType.SINGLE_NODE, SequenceType.SINGLE_NODE, 
        SequenceType.OPTIONAL_STRING, SequenceType.OPTIONAL_STRING };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.SINGLE_NODE;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new DiffCall();
  }

  private static class DiffCall extends ExtensionFunctionCall {

    @Override
    public ZeroOrOne<NodeInfo> call(XPathContext context, Sequence[] arguments) throws XPathException {
      try {
        NodeInfo node1 = (NodeInfo) arguments[0].head();
        NodeInfo node2 = (NodeInfo) arguments[1].head();
        
        String output = null;
        Item outputItem;
        if (arguments.length > 2 && (outputItem = arguments[2].head()) != null)
          output = outputItem.getStringValue();
        
        String whitespaceHandling = "all";
        Item whitespaceHandlingItem;
        if (arguments.length > 3 && (whitespaceHandlingItem = arguments[3].head()) != null)
          whitespaceHandling = whitespaceHandlingItem.getStringValue();
        
        if (!whitespaceHandling.equals("all") && !whitespaceHandling.equals("ignorable") && !whitespaceHandling.equals("none")) {
          throw new XPathException("Whitespace handling method \"" + whitespaceHandling + "\" not supported");
        }
        
        Map<String, String> uriToPrefixMap = new HashMap<String, String>();
        WhitespaceHandlingMethod ws = WhitespaceHandlingMethod.valueOf(whitespaceHandling.toUpperCase());
        
        Document doc1 = DiffUtils.createDocumentFromNodeInfo(node1, uriToPrefixMap, ws);
        Document doc2 = DiffUtils.createDocumentFromNodeInfo(node2, uriToPrefixMap, ws);
        
        XyDiff xydiff = new XyDiff(doc1, doc2);
        DeltaConstructor c = xydiff.diff();
        Document delta = c.getDeltaDocument();
        
        if (output == null || output.equals("deltaxml")) {
        
          Patch patch = new Patch();
          patch.patchDeltaV2(doc1, delta);
          
          uriToPrefixMap.put(Patch.NAMESPACE_DELTAXML, Patch.PREFIX_DELTAXML);
          uriToPrefixMap.put(Patch.NAMESPACE_DXA, Patch.PREFIX_DXA);
          uriToPrefixMap.put(Patch.NAMESPACE_DXX, Patch.PREFIX_DXX);
          
          NodeInfo deltaVNodeInfo = DiffUtils.createNodeInfoFromDocument(doc1, 
              context.getController().makePipelineConfiguration(), uriToPrefixMap);
          
          return new ZeroOrOne<NodeInfo>(deltaVNodeInfo);
          
        } else if (output.equals("xydelta")) {
          NodeInfo deltaNodeInfo = DiffUtils.createNodeInfoFromDocument(delta, context.getController().makePipelineConfiguration(), null);
          return new ZeroOrOne<NodeInfo>(deltaNodeInfo);
        } else {
          throw new XPathException("Unsupported output type \"" + output + "\"");
        }
        
      } catch (Exception e) {
        throw new XPathException("Error differencing nodes", e);
      }
    }
  }

}