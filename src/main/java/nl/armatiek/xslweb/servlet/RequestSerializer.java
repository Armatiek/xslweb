package nl.armatiek.xslweb.servlet;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import nl.armatiek.xslweb.configuration.Config;
import nl.armatiek.xslweb.configuration.Definitions;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlDateTime;

public class RequestSerializer {
  
  private static final String URI = Definitions.NAMESPACEURI_XSLWEB_REQUEST;
  
  private HttpServletRequest req;
  private XMLStreamWriter xsw;
  private File reposDir; 
    
  public RequestSerializer(HttpServletRequest req) {
    this.req = req;         
  }
    
  public String serializeToXML() throws Exception {
    StringWriter sw = new StringWriter();
    
    XMLOutputFactory output = XMLOutputFactory.newInstance();
    this.xsw = output.createXMLStreamWriter(sw);
    
    xsw.writeStartDocument();       
    xsw.setDefaultNamespace(URI);
    xsw.writeStartElement(URI, "request");
    xsw.writeDefaultNamespace(URI);
    
    serializeProperties();
    serializeHeaders();
    serializeParameters();
    serializeAttributes();
    serializeFileUploads();
    serializeSession();    
    serializeCookies();
    
    xsw.writeEndElement();
    xsw.writeEndDocument();
    
    return sw.toString();
  }
  
  public void close() throws IOException {
    if (this.reposDir != null) {
      FileUtils.deleteDirectory(this.reposDir);
    }
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
    dataElement(xsw, URI, "path", safeString(req.getServletPath()) + safeString(req.getPathInfo()));
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
  private void serializeParameters() throws Exception {
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
  
  private void serializeFileUploads() throws Exception {
    boolean isMultipart = ServletFileUpload.isMultipartContent(req);
    if (isMultipart) {   
      DiskFileItemFactory factory = new DiskFileItemFactory();
      factory.setSizeThreshold(0);
      this.reposDir = new File(FileUtils.getTempDirectory(), "xslweb-uploads/" + UUID.randomUUID().toString());
      factory.setRepository(reposDir);
      ServletFileUpload upload = new ServletFileUpload(factory);
      String maxSize = Config.getInstance().getProperties().getProperty(Definitions.PROPERTYNAME_UPLOAD_MAX_SIZE, "50");
      upload.setSizeMax(1024 * 1024 * Long.parseLong(maxSize));
      List<FileItem> items = upload.parseRequest(req);
      Iterator<FileItem> iter = items.iterator();
      if (iter.hasNext()) {
        xsw.writeStartElement(URI, "file-uploads");
        while (iter.hasNext()) {
          FileItem item = iter.next();
          if (!item.isFormField()) {
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
  }
  
  @SuppressWarnings("rawtypes")
  private void serializeSession() throws Exception {
    HttpSession session = req.getSession();
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
        xsw.writeStartElement(URI, "atribute");
        xsw.writeAttribute("name", attrName);
        xsw.writeCharacters(session.getAttribute(attrName).toString());
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
    if (StringUtils.isBlank(text)) {
      return;
    }    
    xsw.writeStartElement(uri, localName);
    xsw.writeCharacters(text);
    xsw.writeEndElement();        
  }
  
  private String safeString(String str) {
    return (str == null) ? "" : str;
  }
  
  private String getXsDateTimeString(Date date) { 
    Calendar cal = Calendar.getInstance();
    if (date != null) {
      cal.setTime(date);
    } 
    XmlDateTime dt = XmlDateTime.Factory.newInstance();
    dt.setCalendarValue(cal);
    return dt.getStringValue();   
  }

}