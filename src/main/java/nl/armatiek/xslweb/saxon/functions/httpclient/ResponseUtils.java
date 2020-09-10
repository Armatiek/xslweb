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
package nl.armatiek.xslweb.saxon.functions.httpclient;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.transform.sax.SAXSource;

import org.apache.commons.io.input.BOMInputStream;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;

import net.sf.saxon.event.Sender;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.CodedName;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.LargeAttributeMap;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SmallAttributeMap;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Fingerprints;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.saxon.functions.httpclient.Types.Type;
import nl.armatiek.xslweb.saxon.utils.NodeInfoUtils;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ResponseUtils {
  
  public static NodeInfo buildResponseElement(final Response response, final XPathContext context, final WebApp webApp) throws XPathException {
    TinyBuilder builder = new TinyBuilder(context.getConfiguration().makePipelineConfiguration());
    builder.setStatistics(context.getConfiguration().getTreeStatistics().SOURCE_DOCUMENT_STATISTICS); 
    builder.setLineNumbering(false);
    builder.open();
    builder.startDocument(0);
    
    Fingerprints fingerprints = webApp.getFingerprints();
    NamePool namePool = context.getConfiguration().getNamePool();
    
    NamespaceMap nsMap = NamespaceMap.of("http", Types.EXT_NAMESPACEURI);
    
    // Root element: 
    ArrayList<AttributeInfo> attrList = new ArrayList<AttributeInfo>();
    attrList.add(new AttributeInfo(new CodedName(fingerprints.STATUS, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, Integer.toString(response.code()), Loc.NONE, 0));
    attrList.add(new AttributeInfo(new CodedName(fingerprints.MESSAGE, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, response.message(), Loc.NONE, 0));
    AttributeMap attrMap = new SmallAttributeMap(attrList);
    builder.startElement(new CodedName(fingerprints.HTTPCLIENT_RESPONSE, "http", namePool), Untyped.getInstance(), attrMap, nsMap, Loc.NONE, 0);
    
    // Response headers:
    Headers responseHeaders = response.headers();
    for (int i=0; i<responseHeaders.size(); i++) {
      ArrayList<AttributeInfo> hAttrList = new ArrayList<AttributeInfo>();
      hAttrList.add(new AttributeInfo(new CodedName(fingerprints.NAME, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, responseHeaders.name(i), Loc.NONE, 0));
      hAttrList.add(new AttributeInfo(new CodedName(fingerprints.VALUE, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, responseHeaders.value(i), Loc.NONE, 0));
      AttributeMap hAttrMap = new LargeAttributeMap(hAttrList);
      builder.startElement(new CodedName(fingerprints.HTTPCLIENT_HEADER, "http", namePool), Untyped.getInstance(), hAttrMap, nsMap, Loc.NONE, 0);
      builder.endElement();
    }
    
    // Response body:
    ResponseBody body = response.body();    
    MediaType mediaType = body.contentType();
    ArrayList<AttributeInfo> bAttrList = new ArrayList<AttributeInfo>();
    if (mediaType != null) {
      bAttrList.add(new AttributeInfo(new CodedName(fingerprints.MEDIATYPE, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, mediaType.toString(), Loc.NONE, 0));
      bAttrList.add(new AttributeInfo(new CodedName(fingerprints.METHOD, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, Types.getMethodForMediaType(mediaType), Loc.NONE, 0));
    } else {
      bAttrList.add(new AttributeInfo(new CodedName(fingerprints.METHOD, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, "binary", Loc.NONE, 0));
    }
    AttributeMap bAttrMap = new SmallAttributeMap(bAttrList);
    builder.startElement(new CodedName(fingerprints.HTTPCLIENT_BODY, "http", namePool), Untyped.getInstance(), bAttrMap, nsMap, Loc.NONE, 0);
    builder.endElement();
    
    builder.endElement();
    
    builder.endDocument();
    builder.close();
    return NodeInfoUtils.getFirstChildElement(builder.getCurrentRoot());
  }
  
  public static Item buildResponseContent(final Response response, final XPathContext context, 
      final NodeInfo requestElem, final String overrideMediaType, final WebApp webApp) throws XPathException, IOException {
    ResponseBody body = response.body();
    MediaType specifiedMediaType = body.contentType();
    MediaType mediaType = null;
    if (overrideMediaType != null) {
      mediaType = MediaType.parse(overrideMediaType);
      if (mediaType == null) {
        throw new XPathException("Error parsing media type (\"" + overrideMediaType + "\")", "HC005");
      }
    } else if (specifiedMediaType != null) {
      mediaType = specifiedMediaType;
    } else {
      mediaType = MediaType.parse("application/octet-stream");
    }
    
    String type = mediaType.type();
    String subtype = mediaType.subtype();
    String fullType = type + '/' + subtype;
    
    Type contentType = Types.parseType(fullType);
    switch (contentType) {
    case TEXT:
      return new StringValue(body.string());
    case XML:
    case XHTML:
    case HTML:
      try {
        TinyBuilder builder = new TinyBuilder(context.getConfiguration().makePipelineConfiguration());
        builder.setStatistics(context.getConfiguration().getTreeStatistics().SOURCE_DOCUMENT_STATISTICS);
        InputSource inputSource = new InputSource(new BOMInputStream(body.byteStream()));
        inputSource.setSystemId(response.request().url().toString());
        SAXSource source = new SAXSource(inputSource);
        source.setSystemId(response.request().url().toString());
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setDTDValidationMode(Validation.STRIP);
        parseOptions.setSchemaValidationMode(Validation.STRIP);
        parseOptions.setSpaceStrippingRule(NoElementsSpaceStrippingRule.getInstance());
        parseOptions.setEntityResolver(new HttpClientEntityResolver(context, requestElem, webApp));
        parseOptions.setLineNumbering(false);
        if (contentType.equals(Type.HTML)) {
          parseOptions.setXMLReader(new Parser());
        } else {
          parseOptions.setXIncludeAware(true);
        }
        Sender.send(source, builder, parseOptions);
        builder.close();
        return builder.getCurrentRoot();
      } catch (XPathException e) {
        e.setErrorCode("HC002");
        throw e;
      }
    default:
      return new Base64BinaryValue(body.bytes()); 
    }
    
  }
  
}