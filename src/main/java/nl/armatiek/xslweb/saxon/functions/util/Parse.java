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

import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.Sender;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyDocumentImpl;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;
import nl.armatiek.xslweb.configuration.Definitions;

/**
 * XPath extension function class for
 * 
 * @author Maarten Kroon
 */
public class Parse extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_UTIL, "parse");

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
    return SequenceType.makeSequenceType(NodeKindTest.DOCUMENT, StaticProperty.EXACTLY_ONE);    
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new ParseCall();
  }
  
  private static class ParseCall extends ExtensionFunctionCall {
    
    @Override
    public ZeroOrOne<NodeInfo> call(XPathContext context, Sequence[] arguments) throws XPathException {
      String xml = ((StringValue) arguments[0].head()).getStringValue();
      try {                
        Controller controller = context.getController();        
        if (controller == null) {
          throw new XPathException("parse() function is not available in this environment");
        }        
        Configuration configuration = controller.getConfiguration();
        StringReader sr = new StringReader(xml);
        InputSource is = new InputSource(sr);                        
        //is.setSystemId(baseURI);
        Source source = new SAXSource(is);
        //source.setSystemId(baseURI);

        Builder b = TreeModel.TINY_TREE.makeBuilder(controller.makePipelineConfiguration());
        Receiver s = b;
        ParseOptions options = new ParseOptions();
        options.setStripSpace(Whitespace.XSLT);
        options.setErrorListener(context.getConfiguration().getErrorListener());

        if (controller.getExecutable().stripsInputTypeAnnotations()) {
          s = configuration.getAnnotationStripper(s);
        }
        s.setPipelineConfiguration(b.getPipelineConfiguration());

        Sender.send(source, s, options);

        TinyDocumentImpl node = (TinyDocumentImpl) b.getCurrentRoot();
        // node.setBaseURI(baseURI);
        node.setSystemId(null);
        b.reset();
        return new ZeroOrOne<NodeInfo>((NodeInfo)node);
      } catch (XPathException err) {
        throw new XPathException("First argument to parse is not a well-formed and namespace-well-formed XML document. XML parser reported: " + err.getMessage(), "FODC0006");
      }

    }
  }
}