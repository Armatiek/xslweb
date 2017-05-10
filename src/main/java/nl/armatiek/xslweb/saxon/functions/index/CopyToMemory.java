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
/*
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

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExplicitLocation;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xmlindex.saxon.tree.XMLIndexNodeInfo;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

/**
 * 
 * @author Maarten Kroon
 */
public class CopyToMemory extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_XMLINDEX, "copy-to-memory");

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
    return new SequenceType[] { SequenceType.SINGLE_NODE };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.SINGLE_NODE;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new CopyToMemoryCall();
  }
  
  private static class CopyToMemoryCall extends ExtensionFunctionCall {

    @Override
    public NodeInfo call(XPathContext context, Sequence[] arguments) throws XPathException {            
      try {
        NodeInfo contextNode = ((NodeInfo) arguments[0].head());
        if (!(contextNode instanceof XMLIndexNodeInfo))
          throw new XPathException("Node is not an XMLIndexNodeInfo");
        PipelineConfiguration pipe = context.getConfiguration().makePipelineConfiguration();
        pipe.getParseOptions().getParserFeatures().remove("http://apache.org/xml/features/xinclude");        
        TinyBuilder builder = new TinyBuilder(pipe);
        builder.open();
        contextNode.copy(builder, 0, ExplicitLocation.UNKNOWN_LOCATION);
        builder.close();
        return builder.getCurrentRoot();
      } catch (XPathException xpe) {
        throw xpe;
      } catch (Exception e) {
        throw new XPathException("Error copying node to memory", e);
      }
    }
  }
  
}