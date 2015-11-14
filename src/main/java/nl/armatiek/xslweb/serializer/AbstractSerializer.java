package nl.armatiek.xslweb.serializer;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xml.sax.helpers.DefaultHandler;

import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.utils.Closeable;
import nl.armatiek.xslweb.utils.XSLWebUtils;

public abstract class AbstractSerializer extends DefaultHandler implements Closeable {
  
  protected HttpServletRequest req;
  protected HttpServletResponse resp;  
  protected OutputStream os;
  protected WebApp webApp;
  
  public AbstractSerializer(WebApp webApp, HttpServletRequest req, HttpServletResponse resp, OutputStream os) {        
    this.webApp = webApp;
    this.req = req;
    this.resp = resp;
    this.os = os;
    if (req != null) {
      XSLWebUtils.addCloseable(req, this);
    }
  }
  
  public AbstractSerializer(WebApp webApp) {
    this(webApp, null, null, null);
  }
  
}