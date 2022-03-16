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

import java.util.HashSet;
import java.util.Set;

import okhttp3.MediaType;

public class Types {
  
  public static final String EXT_NAMESPACEURI = "http://expath.org/ns/http-client";
  
  /** Media types that must be treated as text types (in addition to text/*): */
  public static Set<String> TEXT_TYPES;
  static {
    TEXT_TYPES = new HashSet<String>();
    TEXT_TYPES.add("application/x-www-form-urlencoded");
    TEXT_TYPES.add("application/xml-dtd");
  }

  /** Media types that must be treated as XML types (in addition to *+xml): */
  public static Set<String> XML_TYPES;
  static {
    XML_TYPES = new HashSet<String>();
    XML_TYPES.add("text/xml");
    XML_TYPES.add("application/xml");
    XML_TYPES.add("text/xml-external-parsed-entity");
    XML_TYPES.add("application/xml-external-parsed-entity");
  }
  
  public enum Type { XML, HTML, XHTML, TEXT, JSON, BINARY, BASE64, HEXBIN, MULTIPART, SRC }
  
  public static String getMethodForMediaType(final MediaType mediaType) {
    Type type = parseType(mediaType.type() + '/' + mediaType.subtype());
    return type.toString().toLowerCase();
  }
  
  public static Type parseType(final String type) {
    if (type.startsWith("multipart/")) {
      return Type.MULTIPART;
    } else if ("text/html".equals(type)) {
      return Type.HTML;
    } else if ("application/xhtml+xml".equals(type)) {
      return Type.XHTML;
    } else if (type.endsWith("+xml") || XML_TYPES.contains(type)) {
      return Type.XML;
    } else if (type.startsWith("text/") || TEXT_TYPES.contains(type)) {
      return Type.TEXT;
    } else if (type.equals("application/json") || type.endsWith("+json")) {
      return Type.JSON;
    } else {
      return Type.BINARY;
    }
  }
  
  public static boolean isXmlType(final String type) {
    return type != null && (type.endsWith("+xml") || XML_TYPES.contains(type));
  }
  
  public static boolean isTextType(final String type) {
    return type != null && (type.startsWith("text/") || TEXT_TYPES.contains(type));
  }
  
  public static boolean isJsonType(final String type) {
    return type != null && (type.equals("application/json") || type.endsWith("+json"));
  }

}