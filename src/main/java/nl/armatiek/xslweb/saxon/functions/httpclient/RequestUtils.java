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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import javax.xml.transform.OutputKeys;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.SaxonOutputKeys;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.saxon.utils.NodeInfoUtils;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class RequestUtils {
  
  private static final String[] outputKeys = {OutputKeys.METHOD, SaxonOutputKeys.BYTE_ORDER_MARK, OutputKeys.CDATA_SECTION_ELEMENTS, OutputKeys.DOCTYPE_PUBLIC, 
      OutputKeys.DOCTYPE_SYSTEM, OutputKeys.ENCODING, SaxonOutputKeys.ESCAPE_URI_ATTRIBUTES, OutputKeys.INDENT, SaxonOutputKeys.NORMALIZATION_FORM, 
      OutputKeys.OMIT_XML_DECLARATION, OutputKeys.STANDALONE, SaxonOutputKeys.SUPPRESS_INDENTATION, SaxonOutputKeys.UNDECLARE_PREFIXES };
  
  public static RequestBody getRequestBody(final NodeInfo bodyElem, final Sequence bodies, 
      final int bodyCount, final XPathContext context) throws XPathException {
    String mediaTypeAttr = bodyElem.getAttributeValue("", "media-type");
    if (StringUtils.isBlank(mediaTypeAttr)) {
      throw new XPathException("http:body/@media-type and http:multipart/@media-type must be specified", "HC005");
    }
    MediaType mediaType = MediaType.parse(mediaTypeAttr);
    if (mediaType == null) {
      throw new XPathException("Error parsing http:body/@media-type", "HC005");
    }
    String src = bodyElem.getAttributeValue("", "src");
    if (src != null) {
      if (bodyElem.hasChildNodes()) {
        throw new XPathException("http:body can not have both an attribute \"src\" and child nodes", "HC004");
      }
      if (NodeInfoUtils.getCount(bodyElem.iterateAxis(AxisInfo.ATTRIBUTE)) > 2) {
        throw new XPathException("The src attribute on the body element is mutually exclusive with all other attribute (except the media-type)", "HC004");
      }
      if (!src.startsWith("file:")) {
        throw new XPathException("Only \"file:/\" uris are supported in http:body/@src", "HC005");
      }
      try {
        File file = Paths.get(new URL(src).toURI()).toFile();
        if (!file.isFile()) {
          throw new XPathException("File not found (src: \"" + src + "\", resolved to: \"" + file.getAbsolutePath() + "\")", "HC005");
        }
        if (Types.isXmlType(mediaTypeAttr)) {
          /* Remove any BOM's: */
          try (InputStream is = new BOMInputStream(FileUtils.openInputStream(file))) {
            return RequestBody.create(IOUtils.toByteArray(is), mediaType);
          } catch (IOException ioe) {
            throw new XPathException("Error creating request body", ioe);
          }  
        } else {
          return RequestBody.create(file, mediaType);
        }
      } catch (URISyntaxException | MalformedURLException e) {
        throw new XPathException("Syntax error in uri in http:body/@src (\"" + src + "\")", "HC005");
      }
    }
    
    String method = bodyElem.getAttributeValue("", "method");
    
    if (method == null) {
      method = Types.getMethodForMediaType(mediaType);
    }
    
    if (method.equals("xml") || method.equals("xhtml")) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
      Processor processor = new Processor(context.getConfiguration());
      Serializer ser = processor.newSerializer(baos);
      
      // Set serialization parameters:
      String val;
      for (String key: outputKeys) {
        if ((val = bodyElem.getAttributeValue("", key)) != null)
          ser.setOutputProperty(new QName(key), val);
      }
      if ((val = bodyElem.getAttributeValue("", "output-version")) != null)
        ser.setOutputProperty(new QName("", "version"), val);
      
      NodeInfo nodeInfo;
      if (bodyElem.hasChildNodes()) {
        nodeInfo = NodeInfoUtils.getFirstChildElement(bodyElem);
      } else {
        nodeInfo = (NodeInfo) SequenceTool.itemAt(bodies, bodyCount).head();
      }
      
      // Serialize XML:
      try {
        ser.serializeNode(new XdmNode(nodeInfo));
      } catch (SaxonApiException sae) {
        throw new XPathException("Error serializing request body: " + sae.getMessage(), sae);
      }
      return RequestBody.create(baos.toByteArray(), mediaType);
    } else if (method.equals("text") || method.equals("html")) {
      String value;
      if (bodyElem.hasChildNodes()) {
        value = bodyElem.getStringValue().trim();
      } else {
        value = ((StringValue) SequenceTool.itemAt(bodies, bodyCount)).getStringValue().trim();
      }
      return RequestBody.create(value, mediaType);
    } else if (method.equals("binary")) {
      byte[] value;
      if (bodyElem.hasChildNodes()) {
        value =  Base64BinaryValue.decode(bodyElem.getStringValue().trim());
      } else {
        value = ((Base64BinaryValue) SequenceTool.itemAt(bodies, bodyCount)).getBinaryValue();
      }
      return RequestBody.create(value, mediaType);
    } else {
      throw new XPathException("Unsupported method \"" + method + "\"", "HC005");
    }
  }
  
  public static RequestBody getMultipartRequestBody(final NodeInfo bodyElem, final Sequence bodies, 
      final XPathContext context) throws XPathException {
    String mediaTypeAttr = bodyElem.getAttributeValue("", "media-type");
    if (StringUtils.isBlank(mediaTypeAttr)) {
      throw new XPathException("http:body/@media-type must be specified", "HC005");
    }
    MediaType mediaType = MediaType.parse(mediaTypeAttr);
    if (!mediaType.type().equals("multipart")) {
      throw new XPathException("http:multipart/@media-type must have a \"multipart\" main type", "HC004");
    }
    
    String boundary = bodyElem.getAttributeValue("", "boundary");
    
    MultipartBody.Builder multipartBodyBuilder = (boundary == null) ? new MultipartBody.Builder() : new MultipartBody.Builder(boundary); 
    multipartBodyBuilder.setType(mediaType);
    
    int bodyCount = 0;
    Headers.Builder headersBuilder = new Headers.Builder();
    NodeInfo childElem = NodeInfoUtils.getFirstChildElement(bodyElem);
    while (childElem != null) {
      switch (childElem.getLocalPart()) {
      case "header":
        headersBuilder.add(
            childElem.getAttributeValue("", "name"), 
            childElem.getAttributeValue("", "value"));
        break;
      case "body":
        multipartBodyBuilder.addPart(headersBuilder.build(), getRequestBody(childElem, bodies, bodyCount++, context));
        headersBuilder = new Headers.Builder();
        break;
      }
      childElem = NodeInfoUtils.getNextSiblingElement(childElem);
    }
    return multipartBodyBuilder.build();
  }

}