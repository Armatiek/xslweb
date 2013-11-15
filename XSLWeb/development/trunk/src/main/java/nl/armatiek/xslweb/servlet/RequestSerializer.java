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

import nl.armatiek.xslweb.configuration.Config;
import nl.armatiek.xslweb.configuration.Definitions;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlDateTime;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.megginson.sax.DataWriter;

public class RequestSerializer {
  
  private static final String URI = Definitions.NAMESPACEURI_XSLWEB_REQUEST;
  
  private HttpServletRequest req;
  private DataWriter dw;
  private File reposDir; 
    
  public RequestSerializer(HttpServletRequest req) {
    this.req = req;         
  }
    
  public String serializeToXML() throws Exception {
    StringWriter sw = new StringWriter();      
    this.dw = new DataWriter(sw);
    this.dw.setIndentStep(2);
            
    dw.setPrefix(URI, "");    
    dw.startDocument();    
    dw.startElement(URI, "request");
    
    serializeProperties();
    serializeHeaders();
    serializeParameters();
    serializeAttributes();
    serializeFileUploads();
    serializeSession();    
    serializeCookies();
    
    dw.endElement(URI, "request");
    dw.endDocument();
    
    return sw.toString();
  }
  
  public void close() throws IOException {
    if (this.reposDir != null) {
      FileUtils.deleteDirectory(this.reposDir);
    }
  }
  
  private void serializeProperties() throws Exception {
    dataElement(dw, URI, "auth-type", req.getAuthType());
    dataElement(dw, URI, "character-encoding", req.getCharacterEncoding());
    dataElement(dw, URI, "content-length", Integer.toString(req.getContentLength()));
    dataElement(dw, URI, "context-path", req.getContextPath());
    dataElement(dw, URI, "content-type", req.getContentType());
    dataElement(dw, URI, "local-addr", req.getLocalAddr());
    // locale
    dataElement(dw, URI, "local-name", req.getLocalName());
    dataElement(dw, URI, "local-port", Integer.toString(req.getLocalPort()));
    dataElement(dw, URI, "method", req.getMethod());
    dataElement(dw, URI, "path", safeString(req.getServletPath()) + safeString(req.getPathInfo()));
    dataElement(dw, URI, "path-info", req.getPathInfo());
    dataElement(dw, URI, "path-translated", req.getPathTranslated());    
    dataElement(dw, URI, "protocol", req.getProtocol());
    dataElement(dw, URI, "query-string", req.getQueryString());      
    dataElement(dw, URI, "remote-addr", req.getRemoteAddr());
    dataElement(dw, URI, "remote-host", req.getRemoteHost());
    dataElement(dw, URI, "remote-port", Integer.toString(req.getRemotePort()));
    dataElement(dw, URI, "remote-user", req.getRemoteUser());
    dataElement(dw, URI, "requested-session-id", req.getRequestedSessionId());
    dataElement(dw, URI, "request-URI", req.getRequestURI());
    dataElement(dw, URI, "request-url", req.getRequestURL().toString());
    dataElement(dw, URI, "scheme", req.getScheme());
    dataElement(dw, URI, "server-name", req.getServerName());
    dataElement(dw, URI, "server-port", Integer.toString(req.getServerPort()));
    dataElement(dw, URI, "servlet-path", req.getServletPath());
    // userPrincipal
    dataElement(dw, URI, "is-secure", Boolean.toString(req.isSecure()));
    dataElement(dw, URI, "is-requested-session-id-from-cookie", Boolean.toString(req.isRequestedSessionIdFromCookie()));
    dataElement(dw, URI, "is-requested-session-id-from-url", Boolean.toString(req.isRequestedSessionIdFromURL()));
    dataElement(dw, URI, "is-requested-session-id-valid", Boolean.toString(req.isRequestedSessionIdValid()));
    // isUserInRole
    // login
    // logout
  }
  
  
  @SuppressWarnings("rawtypes")
  private void serializeHeaders() throws Exception {
    Enumeration headerNames = req.getHeaderNames();
    if (headerNames.hasMoreElements()) {
      dw.startElement(URI, "headers");                                   
      while (headerNames.hasMoreElements()) {
        String headerName = (String) headerNames.nextElement();                  
        dw.startElement(URI, "header", "", createAtts("name", headerName));
        dw.characters(req.getHeader(headerName));
        dw.endElement(URI, "header");                               
      }
      dw.endElement(URI, "headers");
    }
  }
  
  @SuppressWarnings("rawtypes")
  private void serializeParameters() throws Exception {
    Enumeration paramNames = req.getParameterNames();
    if (paramNames.hasMoreElements()) {      
      dw.startElement(URI, "parameters");        
      while (paramNames.hasMoreElements()) {
        String paramName = (String) paramNames.nextElement();        
        dw.startElement(URI, "parameter", "", createAtts("name", paramName));                    
        String[] values = req.getParameterValues(paramName);
        for (String value : values) {
          dataElement(dw, URI, "value", value);                        
        }
        dw.endElement(URI, "parameter");
      }
      dw.endElement(URI, "parameters");
    }
  }

  @SuppressWarnings("rawtypes")
  private void serializeAttributes() throws Exception {
    Enumeration attrNames = req.getAttributeNames();
    if (attrNames.hasMoreElements()) {
      dw.startElement(URI, "attributes");                                   
      while (attrNames.hasMoreElements()) {
        String attrName = (String) attrNames.nextElement();                  
        dw.startElement(URI, "atribute", "", createAtts("name", attrName));
        dw.characters(req.getAttribute(attrName).toString());
        dw.endElement(URI, "attribute");                               
      }
      dw.endElement(URI, "attributes");
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
        dw.startElement(URI, "file-uploads");
        while (iter.hasNext()) {
          FileItem item = iter.next();
          if (!item.isFormField()) {
            String fileName = item.getName();
            File file = new File(reposDir, fileName);                      
            dw.startElement(URI, "file-upload");
            dw.dataElement(URI, "file-path", file.getAbsolutePath());
            dw.dataElement(URI, "field-name", item.getFieldName());
            dw.dataElement(URI, "file-name", item.getName());
            dw.dataElement(URI, "content-type", item.getContentType());
            dw.dataElement(URI, "size", Long.toString(item.getSize()));
            dw.endElement(URI, "file-upload");
            item.write(file);
          }
        }
        dw.endElement(URI, "file-uploads");
      }
    }
  }
  
  @SuppressWarnings("rawtypes")
  private void serializeSession() throws Exception {
    HttpSession session = req.getSession();
    dw.startElement(URI, "session");
    dw.dataElement(URI, "creation-time", getXsDateTimeString(new Date(session.getCreationTime())));
    dw.dataElement(URI, "id", session.getId());
    dw.dataElement(URI, "last-accessed-time", getXsDateTimeString(new Date(session.getLastAccessedTime())));
    dw.dataElement(URI, "max-inactive-interval", Integer.toString(session.getMaxInactiveInterval()));
    dw.dataElement(URI, "is-new", Boolean.toString(session.isNew()));    
    Enumeration attrNames = session.getAttributeNames();
    if (attrNames.hasMoreElements()) {
      dw.startElement(URI, "attributes");                                   
      while (attrNames.hasMoreElements()) {
        String attrName = (String) attrNames.nextElement();                  
        dw.startElement(URI, "atribute", "", createAtts("name", attrName));
        dw.characters(session.getAttribute(attrName).toString());
        dw.endElement(URI, "attribute");                               
      }
      dw.endElement(URI, "attributes");
    }    
    dw.endElement(URI, "session");
  }
  
  private void serializeCookies() throws Exception {
    Cookie[] cookies = req.getCookies();
    if (cookies != null && cookies.length > 0) {
      dw.startElement(URI, "cookies");      
      for (Cookie cookie : cookies) {
        dw.startElement(URI, "cookie");
        dataElement(dw, URI, "comment", cookie.getComment());
        dataElement(dw, URI, "domain", cookie.getDomain());
        dataElement(dw, URI, "max-age", Integer.toString(cookie.getMaxAge()));
        dataElement(dw, URI, "name", cookie.getName());
        dataElement(dw, URI, "path", cookie.getPath());
        dataElement(dw, URI, "is-secure", Boolean.toString(cookie.getSecure()));
        dataElement(dw, URI, "value", cookie.getValue());
        dataElement(dw, URI, "version", Integer.toString(cookie.getVersion()));        
        dw.endElement(URI, "cookie");
      }      
      dw.endElement(URI, "cookies");                
    }
  }
  
  private void dataElement(DataWriter dw, String URI, String localName, String text) throws SAXException {
    if (StringUtils.isBlank(text)) {
      return;
    }    
    dw.dataElement(URI, localName, text);    
  }
  
  private Attributes createAtts(String name, String value) {
    AttributesImpl atts = new AttributesImpl();
    atts.addAttribute("", "name", "name", "CDATA", value);
    return atts;
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