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

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.event.StreamWriterToReceiver;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrMore;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.stax.XMLStreamWriterDestination;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xmlindex.Session;
import nl.armatiek.xmlindex.error.XMLIndexException;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

/**
 * 
 * @author Maarten Kroon
 */
public class QueryAdHoc extends ExtensionFunctionDefinition {
  
  private static final Logger logger = LoggerFactory.getLogger(QueryAdHoc.class);
    
  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_XMLINDEX, "query-adhoc");

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
    return 6;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { 
        SequenceType.makeSequenceType(new JavaExternalObjectType(Session.class), StaticProperty.ALLOWS_ONE),
        SequenceType.SINGLE_STRING, 
        SequenceType.OPTIONAL_ANY_URI,
        SequenceType.makeSequenceType(MapType.ANY_MAP_TYPE, StaticProperty.ALLOWS_ZERO_OR_ONE),
        SequenceType.SINGLE_BOOLEAN,
        SequenceType.OPTIONAL_BOOLEAN, 
        SequenceType.SINGLE_BOOLEAN };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.ANY_SEQUENCE;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new QueryAdHocCall();
  }
    
  private static class QueryAdHocCall extends TransformOrQueryCall {

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {            
      @SuppressWarnings("unchecked")
      Session session = ((ObjectValue<Session>) arguments[0].head()).getObject();
      String xquery = ((StringValue) arguments[1].head()).getStringValue();
      
      Item baseURIItem = arguments[2].head();
      URI baseURI;
      try {
        baseURI = (baseURIItem != null) ? new URI(((StringValue) baseURIItem).getStringValue()) :
          getWebApp(context).getHomeDir().toURI();
      } catch (URISyntaxException use) {
        throw new XPathException("Error in syntax of baseURI", use);
      }
        
      Map<QName, XdmValue> params = null;
      if (arguments.length > 3)
        params = mapToParams((HashTrieMap) arguments[3].head());
      boolean throwErrors = true;
      boolean timing = false;
      BooleanValue bool;
      Int64Value duration = null;
      if (arguments.length > 4 && (bool = (BooleanValue) arguments[4].head()) != null)
        throwErrors = bool.getBooleanValue();
      if (arguments.length > 5 && (bool = (BooleanValue) arguments[5].head()) != null)
        timing = bool.getBooleanValue();
      TinyBuilder builder = new TinyBuilder(context.getConfiguration().makePipelineConfiguration());
      try {
        try {
          XMLStreamWriter xsw = new StreamWriterToReceiver(builder);
          XMLStreamWriterDestination dest = new XMLStreamWriterDestination(xsw);
          ErrorListener errorListener = new ErrorListener("on index \"" + session.getIndex().getIndexName() + "\"");            
          try {
            StopWatch stopwatch = null;
            if (timing) {
              stopwatch = new StopWatch();
              stopwatch.start();
            }
            session.query(new StringReader(xquery), baseURI, dest, params, errorListener, null);
            if (timing) {
              stopwatch.stop();
              duration =  Int64Value.makeIntegerValue(stopwatch.getTime(TimeUnit.MICROSECONDS));
            }
          } catch (SaxonApiException sae) {
            XPathException xpe = this.getXPathException(errorListener, sae);
            if (xpe != null)
              throw xpe;
            else
              throw sae;
          }
          NodeInfo root = builder.getCurrentRoot();
          Sequence result = (root != null) ? root : EmptySequence.getInstance();
          if (timing) {
            ArrayList<Item> results = new ArrayList<Item>();             
            results.add(duration);                                        
            results.add(root);
            return new ZeroOrMore<Item>(results);
          } else 
            return result;
        } catch (XPathException xpe) {
          throw xpe;
        } catch (SaxonApiException | XMLIndexException e) {
          if (e.getCause() instanceof XPathException)
            throw (XPathException) e.getCause();
          else 
            throw new XPathException(e.getMessage(), e);
        } catch (Exception e) {
          throw new XPathException(e.getMessage(), e);
        }
      } catch (Exception e) {
        if (throwErrors)
          throw e;
        logger.error("Error executing XQuery on index \"" + session.getIndex().getIndexName() + "\"", e);
        return getErrorResults(builder, e, timing);
      }
    }
  }
  
}