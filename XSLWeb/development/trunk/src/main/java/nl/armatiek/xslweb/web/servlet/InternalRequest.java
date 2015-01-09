package nl.armatiek.xslweb.web.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.web.filter.CachingFilter;
import nl.armatiek.xslweb.web.filter.PipelineGeneratorFilter;
import nl.armatiek.xslweb.web.filter.RequestSerializerFilter;
import nl.armatiek.xslweb.web.filter.SetCharacterEncodingFilter;
import nl.armatiek.xslweb.web.filter.WebAppFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalRequest {
  
  private static final Logger logger = LoggerFactory.getLogger(InternalRequest.class);
  
  public void execute(String path, OutputStream os) throws ServletException, IOException {
    try {
      ArrayList<Filter> filters = new ArrayList<Filter>();
      XSLWebFilterConfig emptyConfig = new XSLWebFilterConfig();
      
      Filter filter;
      filter = new SetCharacterEncodingFilter();
      XSLWebFilterConfig config = new XSLWebFilterConfig();
      config.addInitParameter("encoding", "UTF-8");
      filter.init(config);
      filters.add(filter);
      
      filter = new WebAppFilter();
      filter.init(emptyConfig);
      filters.add(filter);
      
      filter = new RequestSerializerFilter();
      filter.init(emptyConfig);
      filters.add(filter);
      
      filter = new PipelineGeneratorFilter();
      filter.init(emptyConfig);
      filters.add(filter);
      
      filter = new CachingFilter();
      filter.init(emptyConfig);
      filters.add(filter);
      
      HttpServlet servlet = new XSLWebServlet();
      servlet.init();
            
      XSLWebFilterChain filterChain = new XSLWebFilterChain(servlet, filters.toArray(new Filter[filters.size()]));
      
      ServletContext servletContext = Context.getInstance().getServletContext();
      
      ServletRequest request = new XSLWebHttpServletRequest(servletContext, path);
      ServletResponse response = new XSLWebHttpServletResponse(os);
      
      filterChain.doFilter(request, response);
      
    } catch (Exception e) {
      logger.error("Error executing internal servlet request to \"" + path + "\"", e);
      throw e;
    }            
  }

}