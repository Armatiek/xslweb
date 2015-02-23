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

import java.io.StringReader;
import java.util.Properties;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stax.StAXSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.NamespaceReducer;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.Sender;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

import org.apache.commons.lang3.StringUtils;

import de.odysseus.staxon.json.JsonXMLInputFactory;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class ParseJSON extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_JSON, "parse-json");

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
    return SequenceType.OPTIONAL_NODE; // TODO: must be document-node()
  }
  
  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new ParseJSONCall();
  }

  private static class ParseJSONCall extends ExtensionFunctionCall {
    
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {                     
      try {
        String json = ((StringValue) arguments[0].head()).getStringValue();
        if (StringUtils.isBlank(json)) {
          return EmptySequence.getInstance();
        }        
        XMLInputFactory factory = new JsonXMLInputFactory();                
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(json));        
        StAXSource source = new StAXSource(reader);        
        Configuration config = context.getConfiguration();        
        PipelineConfiguration pipe = config.makePipelineConfiguration();
        pipe.getParseOptions().getParserFeatures().remove("http://apache.org/xml/features/xinclude");        
        TinyBuilder builder = new TinyBuilder(pipe);        
        SerializerFactory sf = config.getSerializerFactory();
        Receiver receiver = sf.getReceiver(builder, pipe, new Properties());               
        NamespaceReducer reducer = new NamespaceReducer(receiver);
        ParseOptions options = pipe.getParseOptions();
        options.setContinueAfterValidationErrors(true);
        Sender.send(source, reducer, options);                             
        return builder.getCurrentRoot();                
      } catch (Exception e) {
        throw new XPathException("Error parsing JSON string", e);
      }                    
    }
  }
  
}