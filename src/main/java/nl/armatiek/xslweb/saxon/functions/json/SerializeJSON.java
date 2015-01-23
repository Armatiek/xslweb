package nl.armatiek.xslweb.saxon.functions.json;

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

import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXResult;

import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;
import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLOutputFactory;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class SerializeJSON extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_JSON, "serialize-json");

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
    return new SequenceType[] { SequenceType.ANY_SEQUENCE };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.SINGLE_STRING;
  }
  
  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new SerializeToJSONCall();
  }

  private static class SerializeToJSONCall extends ExtensionFunctionCall {
    
    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {                            
      StringWriter sw = new StringWriter();      
      JsonXMLConfig config = new JsonXMLConfigBuilder().        
          prettyPrint(true).
          build();    
      XMLOutputFactory jFactory = new JsonXMLOutputFactory(config);                  
      TransformerFactory tFactory = new TransformerFactoryImpl();      
      try {
        SequenceIterator iter = arguments[0].iterate(); 
        Item item;
        while ((item = iter.next()) != null) {
          if (item instanceof NodeInfo) {
            Transformer transformer = tFactory.newTransformer();            
            XMLStreamWriter xsw = jFactory.createXMLStreamWriter(sw);            
            transformer.transform((NodeInfo) item, new StAXResult(xsw));                                   
          } else {
            sw.append(item.getStringValue());
          }
        }
      } catch (Exception e) {
        throw new XPathException(e);
      }
      return StringValue.makeStringValue(sw.toString());              
    }
  }
  
}