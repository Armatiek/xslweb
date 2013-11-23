package nl.armatiek.xslweb.configuration;

import java.util.HashSet;
import java.util.Set;

/**
 * Class containing all global string identifiers as static final String fields
 * 
 * @author Maarten Kroon
 */
public class Definitions {
  
  public final static String PROJECT_NAME                      = "xslweb";
  public final static String FILENAME_CONTROLLER_XSL           = "xslweb-controller.xsl";
  public final static String FILENAME_PROPERTIES               = "xslweb.properties";
  
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
    
  public final static String NAMESPACEURI_XML                  = "http://www.w3.org/XML/1998/namespace"; 
  public final static String NAMESPACEURI_XSLT                 = "http://www.w3.org/1999/XSL/Transform";
  public final static String NAMESPACEURI_XMLSCHEMA_INSTANCE   = "http://www.w3.org/2001/XMLSchema-instance";
  public final static String NAMESPACEURI_XMLSCHEMA            = "http://www.w3.org/2001/XMLSchema";
  public final static String NAMESPACEURI_XHTML                = "http://www.w3.org/1999/xhtml";
  public final static String NAMESPACEURI_XLINK                = "http://www.w3.org/1999/xlink";
  public final static String NAMESPACEURI_XINCLUDE             = "http://www.w3.org/2001/XInclude";
  public final static String NAMESPACEURI_STX                  = "http://stx.sourceforge.net/2002/ns";
 
  public final static String NAMESPACEURI_SAXON_CONFIGURATION  = "urn:net.sf.saxon.Configuration";
  public final static String NAMESPACEURI_XSLWEB_CONFIGURATION = "http://www.armatiek.com/xslweb/configuration";
  public final static String NAMESPACEURI_XSLWEB_FUNCTIONS     = "http://www.armatiek.com/xslweb/functions";
  public final static String NAMESPACEURI_XSLWEB_REQUEST       = "http://www.armatiek.com/xslweb/request";
  public final static String NAMESPACEURI_XSLWEB_RESPONSE      = "http://www.armatiek.com/xslweb/response";
  public final static String NAMESPACEURI_XSLWEB_PIPELINE      = "http://www.armatiek.com/xslweb/pipeline";  
  public final static String NAMESPACEURI_EXPATH_FILE          = "http://expath.org/ns/file";
    
  public final static String MIMETYPE_XML                      = "text/xml";
  public final static String MIMETYPE_HTML                     = "text/html";
  public final static String MIMETYPE_MSWORD                   = "application/msword";
  public final static String MIMETYPE_MSEXCEL                  = "application/vnd.ms-excel";
  public final static String MIMETYPE_MSPOWERPOINT             = "application/vnd.ms-powerpoint";
  public final static String MIMETYPE_PDF                      = "application/pdf";
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
  
  public final static String PROPERTYNAME_DEVELOPMENTMODE      = "xslweb.developmentmode";
  public final static String PROPERTYNAME_STATICCONTENTPATTERN = "xslweb.staticcontentpattern";
  public final static String PROPERTYNAME_UPLOAD_MAX_SIZE      = "xslweb.uploadmaxsize";
  
}