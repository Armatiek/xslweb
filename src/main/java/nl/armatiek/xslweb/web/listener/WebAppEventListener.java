package nl.armatiek.xslweb.web.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import nl.armatiek.xslweb.configuration.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class WebAppEventListener implements ServletContextListener {

  private final static Logger logger = LoggerFactory.getLogger(WebAppEventListener.class);

  public void contextInitialized(ServletContextEvent sce) {        
    try { 
      Context context = Context.getInstance();
      context.setServletContext(sce.getServletContext());      
      context.open();
    } catch (Exception e) {
      logger.error("Could not open XSLWeb Context", e);
    }           
  }

  public void contextDestroyed(ServletContextEvent sce) {    
    try { 
      Context.getInstance().close();
    } catch (Exception e) {
      logger.error("Could not close XSLWeb Context", e);
    }    
  }
}