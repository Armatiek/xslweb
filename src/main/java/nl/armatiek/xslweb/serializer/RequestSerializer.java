package nl.armatiek.xslweb.serializer;

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

import java.io.File;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import com.sun.xml.ws.util.xml.ContentHandlerToXMLStreamWriter;

import javanet.staxutils.IndentingXMLStreamWriter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.StreamWriterToReceiver;
import net.sf.saxon.expr.parser.ExplicitLocation;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.CopyOptions;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.stax.ReceiverToXMLStreamWriter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.DateTimeValue;
import nl.armatiek.xslweb.configuration.Attribute;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.error.XSLWebException;
import nl.armatiek.xslweb.utils.XSLWebUtils;
import nl.armatiek.xslweb.xml.BodyFilter;

public class RequestSerializer {
  
  private static final String URI = Definitions.NAMESPACEURI_XSLWEB_REQUEST;
  
  protected static final Logger logger = LoggerFactory.getLogger(RequestSerializer.class);
  
  private HttpServletRequest req;
  private WebApp webApp;
  private boolean developmentMode;
  private XMLStreamWriter xsw;
  private XMLReader xmlReader;  
  private File reposDir; 
  
  private static final Object SECURITY_MANAGER;
  
  static {
    Object securityManager = null;
    try {
      Class<?> securityManagerClass = Class.forName("org.apache.xerces.util.SecurityManager");
      securityManager = securityManagerClass.newInstance();
      Method setEntityExpansionLimit = securityManagerClass.getMethod("setEntityExpansionLimit", int.class);
      setEntityExpansionLimit.invoke(securityManager, 5000);
    } catch (ClassNotFoundException ex) {
      logger.info("Unable to set expansion limit; not using Xerces");
    } catch (Exception ex) {
      logger.info("Unable to set expansion limit for Xerces, using default settings", ex);
      securityManager = null;
    }
    SECURITY_MANAGER = securityManager;
  }
    
  public RequestSerializer(HttpServletRequest req, WebApp webApp) {
    this.req = req;         
    this.webApp = webApp;
    this.developmentMode = webApp.getDevelopmentMode();    
  }
  
  public void serializeToXMLStreamWriter(XMLStreamWriter xsw) throws Exception {  
    this.xsw = xsw;
    
    List<FileItem> fileItems = getMultipartContentItems();
 
    xsw.writeStartDocument();                  
    xsw.setPrefix("req", URI);
    xsw.writeStartElement(URI, "request");
    xsw.writeNamespace("req", URI);
    
    serializeProperties();
    serializeHeaders();
    serializeParameters(fileItems);
    serializeBody(fileItems);
    serializeAttributes();
    serializeFileUploads(fileItems);
    serializeSession();    
    serializeCookies();
    
    xsw.writeEndElement();
    xsw.writeEndDocument();
  }
  
  public String serializeToXML() throws Exception {
    StringWriter sw = new StringWriter();
    XMLOutputFactory output = XMLOutputFactory.newInstance();    
    XMLStreamWriter xsw = output.createXMLStreamWriter(sw);
    if (developmentMode) {
      xsw = new IndentingXMLStreamWriter(xsw);
    }
    serializeToXMLStreamWriter(xsw);
    return sw.toString();
  }
  
  public NodeInfo serializeToNodeInfo() throws Exception {
    TinyBuilder builder = new TinyBuilder(webApp.getConfiguration().makePipelineConfiguration());
    XMLStreamWriter xsw = new StreamWriterToReceiver(builder);
    if (developmentMode) {
      xsw = new IndentingXMLStreamWriter(xsw);
    }
    serializeToXMLStreamWriter(xsw);
    return builder.getCurrentRoot();
  }
  
  public void close() throws IOException {
    if (reposDir != null && reposDir.exists()) {
      FileUtils.deleteDirectory(reposDir);
    }
  }
  
  private List<FileItem> getMultipartContentItems() throws IOException, FileUploadException {
    List<FileItem> items = null;
    boolean isMultipart = ServletFileUpload.isMultipartContent(req);    
    if (isMultipart) {
      DiskFileItemFactory factory = new DiskFileItemFactory();
      factory.setSizeThreshold(0);
      reposDir = new File(FileUtils.getTempDirectory(), File.separatorChar + UUID.randomUUID().toString());
      if (!reposDir.mkdirs()) {
        throw new XSLWebException(String.format("Could not create DiskFileItemFactory repository directory (%s)", reposDir.getAbsolutePath()));
      }
      factory.setRepository(reposDir);
      ServletFileUpload upload = new ServletFileUpload(factory);
      upload.setSizeMax(1024 * 1024 * webApp.getMaxUploadSize());
      items = upload.parseRequest(req);      
    }
    return items;
  }
  
  private boolean hasItems(List<FileItem> fileItems, boolean formField) {
    boolean result = false;
    if (fileItems == null) {
      return result;
    }
    Iterator<FileItem> iter = fileItems.iterator();    
    while (iter.hasNext()) {
      FileItem item = iter.next();
      if (item.isFormField() && formField) {
        return true;        
      } else if (!item.isFormField() && !formField) {
        return true;
      }
    }   
    return result;
  }
  
  private void serializeProperties() throws Exception {
    dataElement(xsw, URI, "auth-type", req.getAuthType());
    dataElement(xsw, URI, "character-encoding", req.getCharacterEncoding());
    dataElement(xsw, URI, "content-length", Integer.toString(req.getContentLength()));
    dataElement(xsw, URI, "context-path", req.getContextPath());
    dataElement(xsw, URI, "content-type", req.getContentType());
    dataElement(xsw, URI, "local-addr", req.getLocalAddr());
    // locale
    dataElement(xsw, URI, "local-name", req.getLocalName());
    dataElement(xsw, URI, "local-port", Integer.toString(req.getLocalPort()));
    dataElement(xsw, URI, "method", req.getMethod());
    String path = StringUtils.substringAfter(safeString(req.getServletPath()) + safeString(req.getPathInfo()), webApp.getPath());
    if (StringUtils.isBlank(path)) {
      path = "/";
    }    
    dataElement(xsw, URI, "path", path);
    dataElement(xsw, URI, "path-info", req.getPathInfo());
    dataElement(xsw, URI, "path-translated", req.getPathTranslated());    
    dataElement(xsw, URI, "protocol", req.getProtocol());
    dataElement(xsw, URI, "query-string", req.getQueryString());      
    dataElement(xsw, URI, "remote-addr", req.getRemoteAddr());
    dataElement(xsw, URI, "remote-host", req.getRemoteHost());
    dataElement(xsw, URI, "remote-port", Integer.toString(req.getRemotePort()));
    dataElement(xsw, URI, "remote-user", req.getRemoteUser());
    dataElement(xsw, URI, "requested-session-id", req.getRequestedSessionId());
    dataElement(xsw, URI, "request-URI", req.getRequestURI());
    dataElement(xsw, URI, "request-url", req.getRequestURL().toString());
    dataElement(xsw, URI, "scheme", req.getScheme());
    dataElement(xsw, URI, "server-name", req.getServerName());
    dataElement(xsw, URI, "server-port", Integer.toString(req.getServerPort()));
    dataElement(xsw, URI, "servlet-path", req.getServletPath());
    dataElement(xsw, URI, "webapp-path", webApp.getPath());
    // userPrincipal
    dataElement(xsw, URI, "is-secure", Boolean.toString(req.isSecure()));
    dataElement(xsw, URI, "is-requested-session-id-from-cookie", Boolean.toString(req.isRequestedSessionIdFromCookie()));
    dataElement(xsw, URI, "is-requested-session-id-from-url", Boolean.toString(req.isRequestedSessionIdFromURL()));
    dataElement(xsw, URI, "is-requested-session-id-valid", Boolean.toString(req.isRequestedSessionIdValid()));
    // isUserInRole
    // login
    // logout
  }
  
  
  @SuppressWarnings("rawtypes")
  private void serializeHeaders() throws Exception {
    Enumeration headerNames = req.getHeaderNames();
    if (headerNames.hasMoreElements()) {
      xsw.writeStartElement(URI, "headers");                                   
      while (headerNames.hasMoreElements()) {
        String headerName = (String) headerNames.nextElement();                  
        xsw.writeStartElement(URI, "header");
        xsw.writeAttribute("name", headerName);
        xsw.writeCharacters(req.getHeader(headerName));
        xsw.writeEndElement();                               
      }
      xsw.writeEndElement();
    }
  }
  
  @SuppressWarnings("rawtypes")
  private void serializeParameters(List<FileItem> fileItems) throws Exception {
    if (fileItems != null) {
      /* Get any querystring parameters: */
      Map<String, List<String>> queryStringParams = null;
      String rawQuery = req.getQueryString();
      if (rawQuery != null)
        queryStringParams = XSLWebUtils.splitQuery(rawQuery);
      if (hasItems(fileItems, true) || queryStringParams != null) {
        xsw.writeStartElement(URI, "parameters");
        /* Serialize form fields: */
        Iterator<FileItem> iter = fileItems.iterator();
        while (iter.hasNext()) {
          FileItem item = iter.next();
          if (item.isFormField()) {
            String paramName = item.getFieldName();
            String value = item.getString();
            xsw.writeStartElement(URI, "parameter");
            xsw.writeAttribute("name", paramName);
            dataElement(xsw, URI, "value", value);            
            xsw.writeEndElement();
          }
        }
        /* Serialize querystring parameters: */
        if (queryStringParams != null) {      
          for (Entry<String, List<String>> param : queryStringParams.entrySet()) {
            xsw.writeStartElement(URI, "parameter");
            xsw.writeAttribute("name", param.getKey());
            for (String value : param.getValue()) {
              dataElement(xsw, URI, "value", value);                        
            }
            xsw.writeEndElement();
          }
        }
        xsw.writeEndElement();
      }
    } else {
      Enumeration paramNames = req.getParameterNames();
      if (paramNames.hasMoreElements()) {      
        xsw.writeStartElement(URI, "parameters");        
        while (paramNames.hasMoreElements()) {
          String paramName = (String) paramNames.nextElement();        
          xsw.writeStartElement(URI, "parameter");
          xsw.writeAttribute("name", paramName);
          String[] values = req.getParameterValues(paramName);
          for (String value : values) {
            dataElement(xsw, URI, "value", value);                        
          }
          xsw.writeEndElement();
        }
        xsw.writeEndElement();
      }
    }
  }
  
  private void serializeBody(List<FileItem> fileItems) throws Exception {
    String method = req.getMethod().toUpperCase();
    if (!(method.equals("POST") || method.equals("PUT")) || fileItems != null) {
      return;
    }    
    PushbackReader pushbackReader = new PushbackReader(req.getReader());    
    int b = pushbackReader.read();
    if (b == -1) {
      return;
    }
    pushbackReader.unread(b);                
    xsw.writeStartElement(URI, "body");
    String contentType = req.getContentType();
    if (contentType != null && contentType.contains(";")) {
      contentType = contentType.split(";")[0].trim();
    }
    if ((contentType != null) && 
        (contentType.startsWith("text/xml") || contentType.startsWith("application/xml") ||
        contentType.endsWith("+xml"))) {
      getFilteredXMLReader().parse(new InputSource(pushbackReader));
    } else if ((contentType != null) && contentType.startsWith("text/plain")) {      
      xsw.writeCharacters(IOUtils.toString(pushbackReader));      
    } else {
      xsw.writeCData(Base64.getEncoder().encodeToString(IOUtils.toByteArray(pushbackReader, "UTF-8")));
    }
    xsw.writeEndElement();
  }

  @SuppressWarnings("rawtypes")
  private void serializeAttributes() throws Exception {
    Enumeration attrNames = req.getAttributeNames();
    if (attrNames.hasMoreElements()) {
      xsw.writeStartElement(URI, "attributes");                                   
      while (attrNames.hasMoreElements()) {
        String attrName = (String) attrNames.nextElement();                  
        xsw.writeStartElement(URI, "atribute");
        xsw.writeAttribute("name", attrName);
        xsw.writeCharacters(req.getAttribute(attrName).toString());        
        xsw.writeEndElement();                               
      }
      xsw.writeEndElement();
    }
  }
  
  private void serializeFileUploads(List<FileItem> fileItems) throws Exception {
    if (!hasItems(fileItems, false)) {
      return;
    }        
    Iterator<FileItem> iter = fileItems.iterator();
    if (iter.hasNext()) {        
      xsw.writeStartElement(URI, "file-uploads");
      while (iter.hasNext()) {
        FileItem item = iter.next();
        if (!item.isFormField() && StringUtils.isNotBlank(item.getName())) {
          String fileName = item.getName();
          File file = new File(reposDir, fileName);                      
          xsw.writeStartElement(URI, "file-upload");
          dataElement(xsw, URI, "file-path", file.getAbsolutePath());
          dataElement(xsw, URI, "field-name", item.getFieldName());
          dataElement(xsw, URI, "file-name", item.getName());
          dataElement(xsw, URI, "content-type", item.getContentType());
          dataElement(xsw, URI, "size", Long.toString(item.getSize()));
          xsw.writeEndElement();
          item.write(file);
        }
      }
      xsw.writeEndElement();      
    }
  }
  
  @SuppressWarnings("rawtypes")
  private void serializeSession() throws Exception {
    HttpSession session = req.getSession(false);
    if (session == null) {
      return;
    }
    xsw.writeStartElement(URI, "session");
    dataElement(xsw, URI, "creation-time", getXsDateTimeString(new Date(session.getCreationTime())));
    dataElement(xsw, URI, "id", session.getId());
    dataElement(xsw, URI, "last-accessed-time", getXsDateTimeString(new Date(session.getLastAccessedTime())));
    dataElement(xsw, URI, "max-inactive-interval", Integer.toString(session.getMaxInactiveInterval()));
    dataElement(xsw, URI, "is-new", Boolean.toString(session.isNew()));    
    Enumeration attrNames = session.getAttributeNames();
    if (attrNames.hasMoreElements()) {
      xsw.writeStartElement(URI, "attributes");                                   
      while (attrNames.hasMoreElements()) {
        String attrName = (String) attrNames.nextElement();                  
        xsw.writeStartElement(URI, "attribute");
        xsw.writeAttribute("name", attrName);                
        Object attr = session.getAttribute(attrName);
        if (attr instanceof Collection) {
          @SuppressWarnings("unchecked")
          Collection<Attribute> attrs = (Collection<Attribute>) attr;
          for (Attribute a: attrs) {
            xsw.writeStartElement(URI, "item");
            if (a.getValue() instanceof NodeInfo) {
              NodeInfo node = unwrapNodeInfo((NodeInfo) a.getValue());
              Receiver receiver = new ReceiverToXMLStreamWriter(xsw);
              PipelineConfiguration config = node.getConfiguration().makePipelineConfiguration();
              receiver.setPipelineConfiguration(config);
              node.copy(receiver, CopyOptions.ALL_NAMESPACES | CopyOptions.TYPE_ANNOTATIONS, ExplicitLocation.UNKNOWN_LOCATION);            
            } else {              
              xsw.writeAttribute("type", a.getType());
              xsw.writeCharacters(a.getValue().toString());
            }
            xsw.writeEndElement();
          }                    
        } else {
          xsw.writeCharacters(attr.toString());
        }        
        xsw.writeEndElement();                               
      }
      xsw.writeEndElement();
    }    
    xsw.writeEndElement();
  }
  
  private void serializeCookies() throws Exception {
    Cookie[] cookies = req.getCookies();
    if (cookies != null && cookies.length > 0) {
      xsw.writeStartElement(URI, "cookies");      
      for (Cookie cookie : cookies) {
        xsw.writeStartElement(URI, "cookie");
        dataElement(xsw, URI, "comment", cookie.getComment());
        dataElement(xsw, URI, "domain", cookie.getDomain());
        dataElement(xsw, URI, "max-age", Integer.toString(cookie.getMaxAge()));
        dataElement(xsw, URI, "name", cookie.getName());
        dataElement(xsw, URI, "path", cookie.getPath());
        dataElement(xsw, URI, "is-secure", Boolean.toString(cookie.getSecure()));
        dataElement(xsw, URI, "value", cookie.getValue());
        dataElement(xsw, URI, "version", Integer.toString(cookie.getVersion()));        
        xsw.writeEndElement();
      }      
      xsw.writeEndElement();                
    }
  }
  
  private void dataElement(XMLStreamWriter xsw, String uri, String localName, String text) throws XMLStreamException {
    if (text == null) {
      return;
    }           
    if (text.equals("")) {
      xsw.writeEmptyElement(uri, localName);
    } else {
      xsw.writeStartElement(uri, localName);
      xsw.writeCharacters(text);
      xsw.writeEndElement();
    }
  }
  
  private XMLReader getFilteredXMLReader() throws SAXException {
    if (this.xmlReader == null) {
      XMLFilterImpl filter = new BodyFilter();
      filter.setContentHandler(new ContentHandlerToXMLStreamWriter(xsw));           
      this.xmlReader = XMLReaderFactory.createXMLReader();              
      this.xmlReader.setFeature("http://xml.org/sax/features/validation", false);
      this.xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
      this.xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", false); 
      if (Context.getInstance().getParserHardening()) {
        setXMLReaderFeature(this.xmlReader, "http://xml.org/sax/features/external-general-entities", false);
        setXMLReaderFeature(this.xmlReader, "http://xml.org/sax/features/external-parameter-entities", false);
        setXMLReaderFeature(this.xmlReader, "http://apache.org/xml/features/disallow-doctype-decl", true);
        setXMLReaderFeature(this.xmlReader, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        this.xmlReader.setEntityResolver(new EntityResolver() {
          @Override
          public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {            
            return new InputSource(new StringReader(""));
          }         
        });
        if (SECURITY_MANAGER != null) {
          setXMLReaderProperty(this.xmlReader, "http://apache.org/xml/properties/security-manager", SECURITY_MANAGER);
        }
      }            
      this.xmlReader.setContentHandler(filter);      
    }
    return this.xmlReader;
  }
  
  private void setXMLReaderFeature(XMLReader reader, String feature, boolean value) {
    try {
      reader.setFeature(feature, value);
    } catch (Exception e) {
      logger.error("Could not set XMLReader feature \"" + feature + "\" with value + \"" + value + "\"", e);
    }
  }
  
  private void setXMLReaderProperty(XMLReader reader, String feature, Object value) {
    try {
      reader.setProperty(feature, value);
    } catch (Exception e) {
      logger.error("Could not set XMLReader property \"" + feature + "\" with value + \"" + value.toString() + "\"", e);
    }
  }
  
  private String safeString(String str) {
    return (str == null) ? "" : str;
  }
  
  private String getXsDateTimeString(Date date) throws XPathException { 
    DateTimeValue dtv = (date == null) ? DateTimeValue.fromJavaTime(0) : DateTimeValue.fromJavaDate(date); 
    return dtv.getStringValue();          
  }
  
  private NodeInfo unwrapNodeInfo(NodeInfo nodeInfo) {
    if (nodeInfo != null && nodeInfo.getNodeKind() == Type.DOCUMENT) {
      nodeInfo = nodeInfo.iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT).next();
    }
    return nodeInfo;
  }

}