package nl.armatiek.xslweb.quartz;

import java.io.File;
import java.io.FileNotFoundException;

import nl.armatiek.xslweb.configuration.Config;
import nl.armatiek.xslweb.configuration.Definitions;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton wrapper for a Quartz Scheduler instance
 *
 * @author Maarten Kroon
 */
public class JobScheduler {
  
  private static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);
  
  private static JobScheduler _instance;
  
  private Scheduler quartzScheduler;
  
  private JobScheduler() throws Exception {            
    File quartzFile = getQuartzPropertiesFile();
    logger.info("Initializing Quartz scheduler using properties file \"" + quartzFile.getAbsolutePath() + "\" ...");
    SchedulerFactory sf = new StdSchedulerFactory(quartzFile.getAbsolutePath());
    quartzScheduler = sf.getScheduler();
  }
  
  /**
   * Returns the JobScheduler singleton instance
   * 
   * @throws Exception
   */
  public static synchronized JobScheduler getInstance() throws Exception {
    if (_instance == null) {
      _instance = new JobScheduler();
    }
    return _instance;
  }
  
  private File getQuartzPropertiesFile() throws FileNotFoundException {
    File file = new File(Config.getInstance().getHomeDir(), "config" + File.separatorChar + Definitions.FILENAME_QUARTZ);
    if (!file.isFile()) {
      throw new FileNotFoundException("Could not find quartz properties file \"" + file.getAbsolutePath() + "\"");
    }
    return file;
  }
  
  /**
   * Returns the Quartz Scheduler instance.
   */
  public Scheduler getScheduler() {
    return quartzScheduler;
  }
  
  /**
   * Starts the Quartz Scheduler.
   * 
   * @throws SchedulerException
   */
  public void startScheduler() throws SchedulerException {
    logger.info("Starting Quartz scheduler ...");
    quartzScheduler.start();
    logger.info("Started Quartz scheduler.");
  }
  
  /**
   * Stops the Quartz Scheduler.
   * 
   * @throws SchedulerException
   */
  public void stopScheduler() throws SchedulerException {
    logger.info("Shutting down Quartz scheduler ...");
    quartzScheduler.shutdown(!Config.getInstance().isDevelopmentMode());
    logger.info("Shutdown Quartz scheduler complete.");
  }
 
}