package nl.armatiek.xslweb.servlet;

import java.io.StringWriter;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import nl.armatiek.xslweb.configuration.Definitions;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.megginson.sax.DataWriter;

public class RequestSerializer {
  
  @SuppressWarnings("rawtypes")
  public static String serializeToXML(HttpServletRequest req) throws SAXException {    
    StringWriter sw = new StringWriter();      
    DataWriter dw = new DataWriter(sw);
    dw.setIndentStep(2);
    
    String uri = Definitions.NAMESPACEURI_XSLWEB_REQUEST;
    
    dw.setPrefix(uri, "");
    // dw.forceNSDecl(uri, "");
    dw.startDocument();
    dw.startElement(uri, "request");
    dataElement(dw, uri, "auth-type", req.getAuthType());
    dataElement(dw, uri, "character-encoding", req.getCharacterEncoding());
    dataElement(dw, uri, "content-length", Integer.toString(req.getContentLength()));
    dataElement(dw, uri, "context-path", req.getContextPath());
    dataElement(dw, uri, "content-type", req.getContentType());
    dataElement(dw, uri, "local-addr", req.getLocalAddr());
    // locale
    dataElement(dw, uri, "local-name", req.getLocalName());
    dataElement(dw, uri, "local-port", Integer.toString(req.getLocalPort()));
    dataElement(dw, uri, "method", req.getMethod());
    dataElement(dw, uri, "path", safeString(req.getServletPath()) + safeString(req.getPathInfo()));
    dataElement(dw, uri, "path-info", req.getPathInfo());
    dataElement(dw, uri, "path-translated", req.getPathTranslated());    
    dataElement(dw, uri, "protocol", req.getProtocol());
    dataElement(dw, uri, "query-string", req.getQueryString());      
    dataElement(dw, uri, "remote-addr", req.getRemoteAddr());
    dataElement(dw, uri, "remote-host", req.getRemoteHost());
    dataElement(dw, uri, "remote-port", Integer.toString(req.getRemotePort()));
    dataElement(dw, uri, "remote-user", req.getRemoteUser());
    dataElement(dw, uri, "requested-session-id", req.getRequestedSessionId());
    dataElement(dw, uri, "request-uri", req.getRequestURI());
    dataElement(dw, uri, "request-url", req.getRequestURL().toString());
    dataElement(dw, uri, "scheme", req.getScheme());
    dataElement(dw, uri, "server-name", req.getServerName());
    dataElement(dw, uri, "server-port", Integer.toString(req.getServerPort()));
    dataElement(dw, uri, "servlet-path", req.getServletPath());
    // userPrincipal
    dataElement(dw, uri, "is-secure", Boolean.toString(req.isSecure()));
    dataElement(dw, uri, "is-requested-session-id-from-cookie", Boolean.toString(req.isRequestedSessionIdFromCookie()));
    dataElement(dw, uri, "is-requested-session-id-from-url", Boolean.toString(req.isRequestedSessionIdFromURL()));
    dataElement(dw, uri, "is-requested-session-id-valid", Boolean.toString(req.isRequestedSessionIdValid()));
    // isUserIOnRole
    // login
    // logout
    
    // Headers:
    Enumeration headerNames = req.getHeaderNames();
    if (headerNames.hasMoreElements()) {
      dw.startElement(uri, "headers");                                   
      while (headerNames.hasMoreElements()) {
        String headerName = (String) headerNames.nextElement();                  
        dw.startElement(uri, "header", "", createAtts("name", headerName));
        dw.characters(req.getHeader(headerName));
        dw.endElement(uri, "header");                               
      }
      dw.endElement(uri, "headers");
    }
    
    // Parameters:
    Enumeration paramNames = req.getParameterNames();
    if (paramNames.hasMoreElements()) {      
      dw.startElement(uri, "parameters");        
      while (paramNames.hasMoreElements()) {
        String paramName = (String) paramNames.nextElement();        
        dw.startElement(uri, "parameter", "", createAtts("name", paramName));                    
        String[] values = req.getParameterValues(paramName);
        for (String value : values) {
          dataElement(dw, uri, "value", value);                        
        }
        dw.endElement(uri, "parameter");
      }
      dw.endElement(uri, "parameters");
    }
    
    // Attributes:
    Enumeration attrNames = req.getAttributeNames();
    if (attrNames.hasMoreElements()) {
      dw.startElement(uri, "attributes");                                   
      while (attrNames.hasMoreElements()) {
        String attrName = (String) attrNames.nextElement();                  
        dw.startElement(uri, "atribute", "", createAtts("name", attrName));
        dw.characters((String) req.getAttribute(attrName));
        dw.endElement(uri, "attribute");                               
      }
      dw.endElement(uri, "attributes");
    }
    
    // FileUpload:
    /*
    boolean isMultipart = ServletFileUpload.isMultipartContent(req);
    if (isMultipart) {      
      ServletFileUpload upload = new ServletFileUpload();
      FileItemIterator iter = upload.getItemIterator(req);
      while (iter.hasNext()) {
          FileItemStream item = iter.next();
          String name = item.getFieldName();
          InputStream stream = item.openStream();
          if (item.isFormField()) {
              System.out.println("Form field " + name + " with value "
                  + Streams.asString(stream) + " detected.");
          } else {
              System.out.println("File field " + name + " with file name "
                  + item.getName() + " detected.");
              // Process the input stream
              ...
          }
      }
    }
    */
    
    // Session :
    // HttpSession session = req.getSession();
    
    // Cookies:
    /*
    Cookie[] cookies = req.getCookies();
    if (cookies == null || cookies.length > 0) {
      Element cookiesElem = (Element) rootElem.appendChild(doc.createElementNS("", "cookies"));
      for (Cookie cookie : cookies) {
        cookiesElem.appendChild(doc.createElementNS("", "comment")).appendChild(doc.createTextNode(cookie.getComment()));
        cookiesElem.appendChild(doc.createElementNS("", "domain")).appendChild(doc.createTextNode(cookie.getDomain()));
        cookiesElem.appendChild(doc.createElementNS("", "maxAge")).appendChild(doc.createTextNode(Integer.toString(cookie.getMaxAge())));
        cookiesElem.appendChild(doc.createElementNS("", "name")).appendChild(doc.createTextNode(cookie.getName()));
        cookiesElem.appendChild(doc.createElementNS("", "path")).appendChild(doc.createTextNode(cookie.getPath()));
        cookiesElem.appendChild(doc.createElementNS("", "isSecure")).appendChild(doc.createTextNode(Boolean.toString(cookie.getSecure())));
        cookiesElem.appendChild(doc.createElementNS("", "value")).appendChild(doc.createTextNode(cookie.getValue()));
        cookiesElem.appendChild(doc.createElementNS("", "version")).appendChild(doc.createTextNode(Integer.toString(cookie.getVersion())));                                                           
      }
    }
    */
    
    dw.endElement(uri, "request");
    dw.endDocument();
    
    return sw.toString();
  }
  
  public static void dataElement(DataWriter dw, String uri, String localName, String text) throws SAXException {
    if (StringUtils.isBlank(text)) {
      return;
    }    
    dw.dataElement(uri, localName, text);    
  }
  
  public static Attributes createAtts(String name, String value) {
    AttributesImpl atts = new AttributesImpl();
    atts.addAttribute("", "name", "name", "CDATA", value);
    return atts;
  }
  
  public static String safeString(String str) {
    return (str == null) ? "" : str;
  }

}