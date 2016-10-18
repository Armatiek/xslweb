package nl.armatiek.xslweb.serializer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
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
    String path = attributes.getValue("", "path");   
    String contentType = attributes.getValue("", "content-type");
    String contentDispositionFilename = attributes.getValue("", "content-disposition-filename");
    File file = null;
    if (path != null) {
      file = new File(path);
      if (!file.isAbsolute()) {
        file = new File(Context.getInstance().getHomeDir(), path);
      }
    }
    if (file == null || !file.isFile()) {
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      resp.flushBuffer();
      return;
    }
    String method = req.getMethod().toUpperCase();
    if (!method.equals("HEAD") && !method.equals("GET")) {
      resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      resp.flushBuffer();
    }
    resp.setContentLength((int) file.length());
    resp.setDateHeader("Last-Modified", file.lastModified());
    if (contentDispositionFilename != null) {
      resp.setHeader("Content-Disposition","attachment; filename=" + contentDispositionFilename);
    }
    if (contentType == null) {
      contentType = MimeUtil.getMostSpecificMimeType(MimeUtil.getMimeTypes(file, unknownMimeType)).toString();
    }
    resp.setContentType(contentType);
    if (method.equals("GET")) {
      FileUtils.copyFile(file, this.os);
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