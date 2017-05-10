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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import net.sf.saxon.dom.DOMWriter;
import net.sf.saxon.event.NamespaceReducer;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExplicitLocation;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.CopyOptions;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.BooleanValue;
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
public class AddDocument extends ExtensionFunctionDefinition {
  
  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_XMLINDEX, "add-document");

  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 3;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 3;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { 
        SequenceType.makeSequenceType(new JavaExternalObjectType(Session.class), StaticProperty.ALLOWS_ONE),
        SequenceType.SINGLE_STRING, 
        SequenceType.SINGLE_NODE };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.OPTIONAL_BOOLEAN;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new AddDocumentCall();
  }
  
  @Override
  public boolean hasSideEffects() {
    return true;
  }
  
  private static class AddDocumentCall extends ExtensionFunctionCall {

    @Override
    public ZeroOrOne<BooleanValue> call(XPathContext context, Sequence[] arguments) throws XPathException {            
      @SuppressWarnings("unchecked")
      Session session = ((ObjectValue<Session>) arguments[0].head()).getObject();
      String uri = ((StringValue) arguments[1].head()).getStringValue();
      NodeInfo node = ((NodeInfo) arguments[2].head());
      if (node.getNodeKind() != Type.DOCUMENT && node.getNodeKind() != Type.ELEMENT)
        throw new XPathException("Could not add document; supplied node is not a document or element node");
      node = unwrapNodeInfo(node);
      try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setXIncludeAware(true);
        dbf.setNamespaceAware(true);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.newDocument();
        DOMWriter writer = new DOMWriter();
        writer.setPipelineConfiguration(context.getConfiguration().makePipelineConfiguration());
        writer.setNode(doc);
        Receiver receiver = new NamespaceReducer((Receiver) writer);
        node.copy(receiver, CopyOptions.TYPE_ANNOTATIONS | CopyOptions.LOCAL_NAMESPACES, 
            ExplicitLocation.UNKNOWN_LOCATION);
        session.addDocument(uri, doc);
        return ZeroOrOne.empty();
      } catch (Exception e) {
        throw new XPathException("Error adding document \"" + uri + "\"", e);
      }
    }
  }
  
}