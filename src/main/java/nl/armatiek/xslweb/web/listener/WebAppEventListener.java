package nl.armatiek.xslweb.web.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import nl.armatiek.xslweb.configuration.Config;
import nl.armatiek.xslweb.quartz.JobScheduler;

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
    /*
    try {
      JobScheduler.getInstance().startScheduler();
    } catch (Exception e) {
      logger.error("Could not start job scheduler ", e);
    }
    */
    Config.getInstance();
  }

  public void contextDestroyed(ServletContextEvent sce) {
    /*
    try { 
      JobScheduler.getInstance().stopScheduler();
    } catch (Exception e) {
      logger.error("Could not stop job scheduler: ", e);
    }
    */
  }
}