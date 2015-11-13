package nl.armatiek.xslweb.serializer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.xml.ws.util.xml.ContentHandlerToXMLStreamWriter;

import net.sf.saxon.event.StreamWriterToReceiver;
import net.sf.saxon.lib.SerializerFactory;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;

/**
 * 
 * @author Maarten Kroon
 */
public class ZipSerializer extends DefaultHandler implements Closeable {
  
  protected static final Logger logger = LoggerFactory.getLogger(ZipSerializer.class);
  
  private ZipOutputStream zos;
  private HttpServletResponse resp;
  private OutputStream os;
  private SerializerFactory serializerFactory;  
  private StreamWriterToReceiver xsw;  
  private ContentHandler serializingHandler;
  
  public ZipSerializer(WebApp webApp) {           
    this.serializerFactory = new SerializerFactory(webApp.getConfiguration());        
  }
  
  public ZipSerializer(WebApp webApp, HttpServletResponse resp, OutputStream os) {    
    this(webApp);
    this.resp = resp;
    this.os = os;                
  }
  
  @Override
  public void close() throws IOException {
    if (zos != null) {
      zos.close();
    }    
  }
  
  private void processArchive(String uri, String localName, String qName, Attributes attributes) throws Exception {
    String name = attributes.getValue("", "name");
    if (name == null) {
      throw new SAXException("No attribute \"name\" specified on archive element");
    }
    String dir = attributes.getValue("", "dir");            
    if (dir == null) {
      if (resp == null) {
        throw new SAXException("No attribute \"dir\" specified on archive element");
      }                
      this.zos = new ZipOutputStream(os);
      resp.setHeader("Content-Disposition","attachment; filename=" + name);        
    } else {
      File parentDir = new File(dir);
      if (!parentDir.isDirectory()) {
        if (!parentDir.mkdirs()) {
          throw new SAXException("Could not create directory \"" + parentDir.getAbsolutePath() + "\"");
        }
      }
      File outputFile = new File(parentDir, name);
      if (outputFile.exists()) {
        if (outputFile.isDirectory()) {
          throw new SAXException("File \"" + parentDir.getAbsolutePath() + "\" already exists as directory");
        } else if (!outputFile.delete()) {
          throw new SAXException("Could not delete output file \"" + outputFile.getAbsolutePath() + "\"");
        }
      }                
      BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile));
      this.zos = new ZipOutputStream(bos);
    }
  }
  
  private void processFileEntry(String uri, String localName, String qName, Attributes attributes) throws Exception {
    String name = attributes.getValue("", "name");
    if (name == null) {
      throw new SAXException("No attribute \"name\" specified on file-entry element");
    }
    String src = attributes.getValue("", "src");
    if (src == null) {
      throw new SAXException("No attribute \"src\" specified on file-entry element");
    }     
    InputStream in;
    if (src.startsWith("http")) {
      in = new URL(src).openStream();
    } else {
      File file = new File(src);
      if (!file.isFile()) {
        throw new SAXException("File \"" + file.getAbsolutePath() + "\" not found");
      }        
      in = new BufferedInputStream(new FileInputStream(file));
    }        
    try {
      ZipEntry entry = new ZipEntry(name);
      zos.putNextEntry(entry);          
      IOUtils.copy(in, zos); 
    } finally {
      IOUtils.closeQuietly(in);
    }
  }
  
  private void processInlineEntry(String uri, String localName, String qName, Attributes attributes) throws Exception {
    String name = attributes.getValue("", "name");
    if (name == null) {
      throw new SAXException("No attribute \"name\" specified on inline-entry element");
    }    
    Properties props = new Properties();
    for (int i=0; i<attributes.getLength(); i++) {
      String n = attributes.getLocalName(i);
      if (!n.equals("name")) {
        props.put(n, attributes.getValue(i));
      }
    }
    ZipEntry entry = new ZipEntry(name);
    zos.putNextEntry(entry);                     
    this.xsw = serializerFactory.getXMLStreamWriter(new StreamResult(this.zos), props);    
    this.serializingHandler = new ContentHandlerToXMLStreamWriter(xsw);
  }
  
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if (serializingHandler != null) {
      serializingHandler.startElement(uri, localName, qName, attributes);
      return;
    }
    if (!StringUtils.equals(uri, Definitions.NAMESPACEURI_XSLWEB_ZIP_SERIALIZER)) {
      return;      
    }
    try {
      if (StringUtils.equals(localName, "archive")) {
        processArchive(uri, localName, qName, attributes);
      } else if (StringUtils.equals(localName, "file-entry")) {
        processFileEntry(uri, localName, qName, attributes);
      } else if (StringUtils.equals(localName, "inline-entry")) {
        processInlineEntry(uri, localName, qName, attributes);
      }
    } catch (Exception e) {
      throw new SAXException(e);
    }
  }
  
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {        
    try {
      boolean isInlineEntry = StringUtils.equals(uri, Definitions.NAMESPACEURI_XSLWEB_ZIP_SERIALIZER) && 
          StringUtils.equals(localName, "inline-entry");    
      if (isInlineEntry) {
        if (this.xsw != null) {
          this.xsw.close();
        }
        this.xsw = null;
        this.serializingHandler = null;        
      } else if (serializingHandler != null) {
        serializingHandler.endElement(uri, localName, qName);      
      }
    } catch (Exception e) {
      throw new SAXException(e);
    }        
  }
  
  @Override
  public void endDocument() throws SAXException {
    try {
      close();
    } catch (IOException ioe) {
      throw new SAXException("Could not close ZipOutputStream", ioe);
    }
  }
  
  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (serializingHandler != null) {
      serializingHandler.characters(ch, start, length);
      return;
    }     
  }
  
  @Override
  public void endPrefixMapping(String prefix) throws SAXException {
    if (serializingHandler != null) {
      serializingHandler.endPrefixMapping(prefix);
    }
  }
  
  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {        
    if (serializingHandler != null) {
      serializingHandler.ignorableWhitespace(ch, start, length);
    }
  }
  
  @Override
  public void processingInstruction(String target, String data) throws SAXException {
    if (serializingHandler != null) {
      serializingHandler.processingInstruction(target, data);
    }
  }
  
  @Override
  public void setDocumentLocator(Locator locator) { 
    if (serializingHandler != null) {
      serializingHandler.setDocumentLocator(locator);
    }    
  }
  
  @Override
  public void skippedEntity(String name) throws SAXException {
    if (serializingHandler != null) {
      serializingHandler.skippedEntity(name);
    }      
  }
  
  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    if (serializingHandler != null) {
      serializingHandler.startPrefixMapping(prefix, uri);
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