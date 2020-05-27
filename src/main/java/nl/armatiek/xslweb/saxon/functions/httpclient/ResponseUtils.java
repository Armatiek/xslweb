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

import javax.xml.transform.sax.SAXSource;

import org.apache.commons.io.input.BOMInputStream;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;

import net.sf.saxon.event.Sender;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExplicitLocation;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.om.CodedName;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NodeInfo;
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
  
  /*
  private static MimeType unknownMimeType = new MimeType(Definitions.MIMETYPE_BINARY);
  
  private static final ThreadLocal<DateFormat> DATE_HEADER_FORMAT =
      new ThreadLocal<DateFormat>() {
        @Override protected DateFormat initialValue() {
          DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
          format.setTimeZone(TimeZone.getTimeZone("GMT"));
          return format;
        }
      };
  */
  
  public static NodeInfo buildResponseElement(final Response response, final XPathContext context, final WebApp webApp) throws XPathException {
    TinyBuilder builder = new TinyBuilder(context.getConfiguration().makePipelineConfiguration());
    builder.setStatistics(context.getConfiguration().getTreeStatistics().SOURCE_DOCUMENT_STATISTICS); 
    builder.setLineNumbering(false);
    builder.open();
    builder.startDocument(0);
    
    Fingerprints fingerprints = webApp.getFingerprints();
    NamePool namePool = context.getConfiguration().getNamePool();
    
    // Root element: 
    builder.startElement(new CodedName(fingerprints.HTTPCLIENT_RESPONSE, "http", namePool), Untyped.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0);
    builder.attribute(new CodedName(fingerprints.STATUS, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, Integer.toString(response.code()), null, 0);
    builder.attribute(new CodedName(fingerprints.MESSAGE, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, response.message(), null, 0);
    
    // Response headers:
    Headers responseHeaders = response.headers();
    for (int i=0; i<responseHeaders.size(); i++) {
      builder.startElement(new CodedName(fingerprints.HTTPCLIENT_HEADER, "http", namePool), Untyped.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0);
      builder.attribute(new CodedName(fingerprints.NAME, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, responseHeaders.name(i), null, 0);
      builder.attribute(new CodedName(fingerprints.VALUE, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, responseHeaders.value(i), null, 0);
      builder.endElement();
    }
    
    // Response body:
    ResponseBody body = response.body();
    builder.startElement(new CodedName(fingerprints.HTTPCLIENT_BODY, "http", namePool), Untyped.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0);
    MediaType mediaType = body.contentType();
    if (mediaType != null) {
      builder.attribute(new CodedName(fingerprints.MEDIATYPE, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, mediaType.toString(), null, 0);
      builder.attribute(new CodedName(fingerprints.METHOD, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, Types.getMethodForMediaType(mediaType), null, 0);
    } else {
      builder.attribute(new CodedName(fingerprints.METHOD, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, "binary", null, 0);
    }
 
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
  
  /*
  private static void addHeader(TinyBuilder builder, NamePool namePool, Fingerprints fingerprints, String name, String value) throws XPathException {
    builder.startElement(new CodedName(fingerprints.HTTPCLIENT_HEADER, "http", namePool), Untyped.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0);
    builder.attribute(new CodedName(fingerprints.NAME, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, name, null, 0);
    builder.attribute(new CodedName(fingerprints.VALUE, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, value, null, 0);
    builder.endElement();
  }
  
  public static NodeInfo buildResponseElement(final File file, final XPathContext context, final WebApp webApp) throws XPathException {
    String mimeType = MimeUtil.getMostSpecificMimeType(MimeUtil.getMimeTypes(file, unknownMimeType)).toString();
    
    TinyBuilder builder = new TinyBuilder(context.getConfiguration().makePipelineConfiguration());
    builder.setStatistics(context.getConfiguration().getTreeStatistics().SOURCE_DOCUMENT_STATISTICS); 
    builder.setLineNumbering(false);
    builder.open();
    builder.startDocument(0);
    
    Fingerprints fingerprints = webApp.getFingerprints();
    NamePool namePool = context.getConfiguration().getNamePool();
    
    // Root element: 
    builder.startElement(new CodedName(fingerprints.HTTPCLIENT_RESPONSE, "http", namePool), Untyped.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0);
    builder.attribute(new CodedName(fingerprints.STATUS, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, Integer.toString((file.isFile()) ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_FOUND), null, 0);
    builder.attribute(new CodedName(fingerprints.MESSAGE, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, (file.isFile()) ? "" : "Not found", null, 0);
    
    // Response headers:
    addHeader(builder, namePool, fingerprints, "Content-Type", mimeType);
    addHeader(builder, namePool, fingerprints, "Content-Length", Long.toString(file.length()));
    addHeader(builder, namePool, fingerprints, "Last-Modified", DATE_HEADER_FORMAT.get().format(new Date(file.lastModified())));
    
    // Response body:
    builder.startElement(new CodedName(fingerprints.HTTPCLIENT_BODY, "http", namePool), Untyped.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0);
    if (mimeType != null) {
      builder.attribute(new CodedName(fingerprints.MEDIATYPE, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, mimeType, null, 0);
      builder.attribute(new CodedName(fingerprints.METHOD, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, Types.parseType(mimeType).toString().toLowerCase(), null, 0);
    } else {
      builder.attribute(new CodedName(fingerprints.METHOD, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, "binary", null, 0);
    }
 
    builder.endElement();
   
    builder.endElement();
    
    builder.endDocument();
    builder.close();
    return NodeInfoUtils.getFirstChildElement(builder.getCurrentRoot());
  }
  */

}