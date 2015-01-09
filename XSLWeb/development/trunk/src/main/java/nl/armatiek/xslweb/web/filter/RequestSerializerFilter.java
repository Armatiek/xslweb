package nl.armatiek.xslweb.web.filter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.serializer.RequestSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestSerializerFilter implements Filter {
  
  private static final Logger logger = LoggerFactory.getLogger(RequestSerializerFilter.class);

  private String lineSeparator;
  
  @Override
  public void init(FilterConfig filterConfig) throws ServletException { 
    lineSeparator = System.lineSeparator();
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
      throws IOException, ServletException {
    WebApp webApp = null;
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;
    try {             
      webApp = (WebApp) request.getAttribute(Definitions.ATTRNAME_WEBAPP);    
      RequestSerializer requestSerializer = new RequestSerializer(req, webApp);
      try {
        String requestXML = requestSerializer.serializeToXML();
        request.setAttribute(Definitions.ATTRNAME_REQUESTXML, requestXML);      
        if (webApp.getDevelopmentMode()) {
          logger.debug("----------\nREQUEST XML:" + lineSeparator + requestXML);                
        }      
        chain.doFilter(request, response);        
      } finally {
        requestSerializer.close();
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      if (webApp != null && webApp.getDevelopmentMode()) {              
        resp.setContentType("text/plain; charset=UTF-8");        
        e.printStackTrace(new PrintStream(resp.getOutputStream()));        
      } else if (!resp.isCommitted()) {
        resp.resetBuffer();
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setContentType("text/html; charset=UTF-8");
        Writer w = new OutputStreamWriter(resp.getOutputStream(), "UTF-8");
        w.write("<html><body><h1>Internal Server Error</h1></body></html>");
      }
    }
  }

  @Override
  public void destroy() { }

}