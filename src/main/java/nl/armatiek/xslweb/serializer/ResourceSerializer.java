package nl.armatiek.xslweb.serializer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.web.servlet.FileServlet;

/**
 * 
 * @author Maarten Kroon
 */
public class ResourceSerializer extends AbstractSerializer {
  
  protected static final Logger logger = LoggerFactory.getLogger(ResourceSerializer.class);
  
  private static MimeType unknownMimeType = new MimeType(Definitions.MIMETYPE_BINARY);
  
  public ResourceSerializer(WebApp webApp, HttpServletRequest req, HttpServletResponse resp, OutputStream os) {    
    super(webApp, req, resp, os);      
  }
  
  public ResourceSerializer(WebApp webApp) {
    this(webApp, null, null, null);    
  }
  
  @Override
  public void close() throws IOException { }
  
  private void processResourceSerializer(String uri, String localName, String qName, Attributes attributes) throws Exception {    
    String method = req.getMethod().toUpperCase();
    
    if (!StringUtils.equalsAny(method, "GET", "POST", "HEAD")) {
      resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method \"" + method + "\" not supported");
      return;
    }
    
    final String contentDispositionFilename = attributes.getValue("", "content-disposition-filename");
    
    final String expireTimeSeconds = attributes.getValue("", "expire-time");
    
    FileServlet fileServlet = new FileServlet() {

      private static final long serialVersionUID = 1L;

      @Override
      protected File getFile(HttpServletRequest request) {
        String path = attributes.getValue("", "path");
        if (StringUtils.isEmpty(path)) {
          return null;
        }
        File file = new File(path);
        if (!file.isAbsolute()) {
          file = new File(Context.getInstance().getHomeDir(), path);
        }
        return file;
      }
      
      @Override
      protected String getContentType(HttpServletRequest request, File file) {
        String contentType = attributes.getValue("", "content-type");
        if (StringUtils.isEmpty(contentType)) {
          contentType = MimeUtil.getMostSpecificMimeType(MimeUtil.getMimeTypes(file, unknownMimeType)).toString();
        }
        return contentType;
      }
      
      @Override
      protected boolean isAttachment(HttpServletRequest request, String contentType) {
        return StringUtils.isNotEmpty(contentDispositionFilename); 
      }
      
      @Override
      protected String getAttachmentName(HttpServletRequest request, File file) {
        return (StringUtils.isEmpty(contentDispositionFilename)) ? super.getAttachmentName(request, file) : contentDispositionFilename;
      }

      @Override
      protected long getExpireTime(HttpServletRequest request, File file) {
        if (expireTimeSeconds != null) {
          return Long.parseLong(expireTimeSeconds);
        } 
        return super.getExpireTime(request, file);
      }
      
    };
    
    if (method.equals("GET") || method.equals("POST")) {
      fileServlet.doGet(req, resp);
    } else if (method.equals("HEAD")) {
      fileServlet.doHead(req, resp);
    } else {
      resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method \"" + method + "\" not supported");
    }
    
  }
  
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {       
    try {
      if (StringUtils.equals(localName, "resource-serializer")) {
        processResourceSerializer(uri, localName, qName, attributes);
      }
    } catch (Exception e) {
      throw new SAXException(e);
    }
  }
  
  @Override
  public void warning(SAXParseException e) throws SAXException {
    logger.warn(e.getMessage(), e);
  }
  
  @Override
  public void error(SAXParseException e) throws SAXException {
    logger.error(e.getMessage(), e);
  }
  
  @Override
  public void fatalError(SAXParseException e) throws SAXException {
    logger.error(e.getMessage(), e);
  }
  
}