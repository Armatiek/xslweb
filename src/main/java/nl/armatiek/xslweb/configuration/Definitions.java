package nl.armatiek.xslweb.configuration;

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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import net.sf.saxon.s9api.QName;

/**
 * Class containing all global string identifiers as static final String fields
 * 
 * @author Maarten Kroon
 */
public class Definitions {
  
  public final static String PROJECT_NAME                      = "xslweb";
  public final static String PROJECT_VERSION                   = "4.0.0-RC1";
  public final static String PATHNAME_REQUESTDISPATCHER_XSL    = "xsl/request-dispatcher.xsl";
  public final static String FILENAME_PROPERTIES               = "xslweb.properties";
  public final static String FILENAME_QUARTZ                   = "xslweb-quartz.properties";
  public final static String FILENAME_EHCACHE                  = "xslweb-ehcache.xml";
  
  public final static String XML_EXTENSION                     = "xml";
  public final static String XSL_EXTENSION                     = "xsl";
  public final static String XSD_EXTENSION                     = "xsd";
  public final static String STX_EXTENSION                     = "stx";
  public final static String TMP_EXTENSION                     = "tmp";
  public final static String[] XML_EXTENSIONS                  = new String[] {"xml", "xslt", "xsl", "xsd", "stx"};
  
  public static Set<String> xmlExtensions = new HashSet<String>();
  static {
    for (int i=0; i<XML_EXTENSIONS.length; i++) {
      xmlExtensions.add(XML_EXTENSIONS[i]);
    }
  }
  
  public final static String NAMESPACEURI_XML                   = "http://www.w3.org/XML/1998/namespace";
  public final static String NAMESPACEURI_XMLNS                 = "http://www.w3.org/2000/xmlns/";
  public final static String NAMESPACEURI_XSLT                  = "http://www.w3.org/1999/XSL/Transform";
  public final static String NAMESPACEURI_XMLSCHEMA_INSTANCE    = "http://www.w3.org/2001/XMLSchema-instance";
  public final static String NAMESPACEURI_XMLSCHEMA             = "http://www.w3.org/2001/XMLSchema";
  public final static String NAMESPACEURI_XHTML                 = "http://www.w3.org/1999/xhtml";
  public final static String NAMESPACEURI_XLINK                 = "http://www.w3.org/1999/xlink";
  public final static String NAMESPACEURI_XINCLUDE              = "http://www.w3.org/2001/XInclude";
  public final static String NAMESPACEURI_STX                   = "http://stx.sourceforge.net/2002/ns";
 
  public final static String NAMESPACEURI_SAXON_CONFIGURATION   = "urn:net.sf.saxon.Configuration";
  public final static String NAMESPACEURI_XSLWEB                = "http://www.armatiek.com/xslweb";
  public final static String NAMESPACEURI_XSLWEB_WEBAPP         = NAMESPACEURI_XSLWEB + "/webapp";
  public final static String NAMESPACEURI_XSLWEB_CONFIGURATION  = NAMESPACEURI_XSLWEB + "/configuration";
  public final static String NAMESPACEURI_XSLWEB_FUNCTIONS      = NAMESPACEURI_XSLWEB + "/functions";
  public final static String NAMESPACEURI_XSLWEB_EVENT          = NAMESPACEURI_XSLWEB + "/event";
  public final static String NAMESPACEURI_XSLWEB_FX_BASE64      = NAMESPACEURI_XSLWEB_FUNCTIONS + "/base64";
  public final static String NAMESPACEURI_XSLWEB_FX_CONTEXT     = NAMESPACEURI_XSLWEB_FUNCTIONS + "/context";
  public final static String NAMESPACEURI_XSLWEB_FX_IO          = NAMESPACEURI_XSLWEB_FUNCTIONS + "/io";
  public final static String NAMESPACEURI_XSLWEB_FX_IMAGE       = NAMESPACEURI_XSLWEB_FUNCTIONS + "/image";
  public final static String NAMESPACEURI_XSLWEB_FX_LOG         = NAMESPACEURI_XSLWEB_FUNCTIONS + "/log";
  public final static String NAMESPACEURI_XSLWEB_FX_EMAIL       = NAMESPACEURI_XSLWEB_FUNCTIONS + "/email";  
  public final static String NAMESPACEURI_XSLWEB_FX_SERIALIZE   = NAMESPACEURI_XSLWEB_FUNCTIONS + "/serialize";
  public final static String NAMESPACEURI_XSLWEB_FX_SECURITY    = NAMESPACEURI_XSLWEB_FUNCTIONS + "/security";
  public final static String NAMESPACEURI_XSLWEB_FX_JSON        = NAMESPACEURI_XSLWEB_FUNCTIONS + "/json";
  public final static String NAMESPACEURI_XSLWEB_FX_UUID        = NAMESPACEURI_XSLWEB_FUNCTIONS + "/uuid";
  public final static String NAMESPACEURI_XSLWEB_FX_WEBAPP      = NAMESPACEURI_XSLWEB_FUNCTIONS + "/webapp";
  public final static String NAMESPACEURI_XSLWEB_FX_CACHE       = NAMESPACEURI_XSLWEB_FUNCTIONS + "/cache";
  public final static String NAMESPACEURI_XSLWEB_FX_SCRIPT      = NAMESPACEURI_XSLWEB_FUNCTIONS + "/script";
  public final static String NAMESPACEURI_XSLWEB_FX_DYNFUNC     = NAMESPACEURI_XSLWEB_FUNCTIONS + "/dynfunc";
  public final static String NAMESPACEURI_XSLWEB_FX_XMLINDEX    = NAMESPACEURI_XSLWEB_FUNCTIONS + "/xmlindex";
  public final static String NAMESPACEURI_XSLWEB_FX_SQL         = NAMESPACEURI_XSLWEB_FUNCTIONS + "/sql";
  public final static String NAMESPACEURI_XSLWEB_FX_UTIL        = NAMESPACEURI_XSLWEB_FUNCTIONS + "/util";
  public final static String NAMESPACEURI_XSLWEB_FX_ZIP         = NAMESPACEURI_XSLWEB_FUNCTIONS + "/zip";
  public final static String NAMESPACEURI_XSLWEB_FX_EXEC        = NAMESPACEURI_XSLWEB_FUNCTIONS + "/exec";
  public final static String NAMESPACEURI_XSLWEB_FX_XQUERY      = NAMESPACEURI_XSLWEB_FUNCTIONS + "/xquery";
  public final static String NAMESPACEURI_XSLWEB_FX_QUEUE       = NAMESPACEURI_XSLWEB_FUNCTIONS + "/queue";
  public final static String NAMESPACEURI_XSLWEB_REQUEST        = NAMESPACEURI_XSLWEB + "/request";
  public final static String NAMESPACEURI_XSLWEB_RESPONSE       = NAMESPACEURI_XSLWEB + "/response";
  public final static String NAMESPACEURI_XSLWEB_FX_SESSION     = NAMESPACEURI_XSLWEB + "/session";
  public final static String NAMESPACEURI_XSLWEB_PIPELINE       = NAMESPACEURI_XSLWEB + "/pipeline";
  public final static String NAMESPACEURI_XSLWEB_ZIP_SERIALIZER = NAMESPACEURI_XSLWEB + "/zip-serializer";
  public final static String NAMESPACEURI_XSLWEB_FOP_SERIALIZER = NAMESPACEURI_XSLWEB + "/fop-serializer";
  public final static String NAMESPACEURI_XSLWEB_VALIDATION     = NAMESPACEURI_XSLWEB + "/validation";
  
  public final static String NAMESPACEURI_EXPATH_FILE           = "http://expath.org/ns/file";
  
  public final static String MIMETYPE_XML                      = "text/xml";
  public final static String MIMETYPE_HTML                     = "text/html";
  public final static String MIMETYPE_MSWORD                   = "application/msword";
  public final static String MIMETYPE_MSEXCEL                  = "application/vnd.ms-excel";
  public final static String MIMETYPE_MSPOWERPOINT             = "application/vnd.ms-powerpoint";
  public final static String MIMETYPE_PDF                      = "application/pdf";
  public final static String MIMETYPE_ZIP                      = "application/zip";
  public final static String MIMETYPE_OO_TEXT                  = "application/vnd.oasis.opendocument.text";
  public final static String MIMETYPE_OO_TEXTTEMPLATE          = "application/vnd.oasis.opendocument.text-template";
  public final static String MIMETYPE_OO_TEXTWEB               = "application/vnd.oasis.opendocument.text-web";
  public final static String MIMETYPE_OO_TEXTMASTER            = "application/vnd.oasis.opendocument.text-master";
  public final static String MIMETYPE_OO_SPREADSHEET           = "application/vnd.oasis.opendocument.spreadsheet";
  public final static String MIMETYPE_OO_SPREADSHEETTEMPLATE   = "application/vnd.oasis.opendocument.spreadsheet-template";
  public final static String MIMETYPE_OO_PRESENTATION          = "application/vnd.oasis.opendocument.presentation";
  public final static String MIMETYPE_OO_PRESENTATIONTEMPLATE  = "application/vnd.oasis.opendocument.presentation-template";
  public final static String MIMETYPE_BINARY                   = "application/octet-stream";
  public final static String MIMETYPE_JPEG                     = "image/jpeg";
  public final static String MIMETYPE_GIF                      = "image/gif";
  public final static String MIMETYPE_PNG                      = "image/png";
  public final static String MIMETYPE_TEXTPLAIN                = "text/plain";  
  
  public final static String ATTRNAME_WEBAPP                   = "xslweb.webapp";
  public final static String ATTRNAME_REQUESTXML               = "xslweb.requestxml";
  public final static String ATTRNAME_PIPELINEHANDLER          = "xslweb.pipelinehandler";
  public final static String ATTRNAME_TEMPFILES                = "xslweb.tempfiles";
  
  public final static String PARAMNAME_TRACE_BASIC             = "xslweb.trace.basic";
  public final static String PARAMNAME_TRACE_TIME              = "xslweb.trace.time";
  
  public final static String CACHENAME_RESPONSECACHINGFILTER   = "XSLWebResponseCachingFilter";
  
  public final static String SCHEME_XSLWEB                     = "xslweb";
  
  public final static String PROPERTYNAME_TRUST_ALL_CERTS      = "xslweb.trustallcerts";
  public final static String PROPERTYNAME_PARSER_HARDENING     = "xslweb.parserhardening";
  public final static String PROPERTYNAME_WABDAV_ENABLE        = "xslweb.webdav.enable";
  public final static String PROPERTYNAME_WEBDAV_ROOT          = "xslweb.webdav.root";
  
  public final static QName EVENTNAME_WEBAPPOPEN              = new QName("event", NAMESPACEURI_XSLWEB_EVENT, "webapp-open");
  public final static QName EVENTNAME_WEBAPPCLOSE             = new QName("event", NAMESPACEURI_XSLWEB_EVENT, "webapp-close");
  public final static QName EVENTNAME_WEBAPPRELOAD            = new QName("event", NAMESPACEURI_XSLWEB_EVENT, "webapp-reload");
  
  public final static String CLASSPATH_SEPARATOR              = StringUtils.defaultString(System.getProperty("path.separator"), SystemUtils.IS_OS_WINDOWS ? ";" : ":");
  
}