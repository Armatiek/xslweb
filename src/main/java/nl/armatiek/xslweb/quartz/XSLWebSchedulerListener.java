package nl.armatiek.xslweb.quartz;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XSLWebSchedulerListener implements SchedulerListener {
  
  private static final Logger logger = LoggerFactory.getLogger(XSLWebSchedulerListener.class);

  @Override
  public void jobAdded(JobDetail jobDetail) {
    logger.info("Job added");
  }

  @Override
  public void jobDeleted(JobKey jobKey) {
    logger.info("Job deleted");
  }

  @Override
  public void jobPaused(JobKey jobKey) {
    logger.info("Job paused");

  }

  @Override
  public void jobResumed(JobKey jobKey) {
    logger.info("Job resumed");
  }

  @Override
  public void jobScheduled(Trigger trigger) {
    logger.info("Job scheduled");

  }

  @Override
  public void jobUnscheduled(TriggerKey triggerKey) {
    logger.info("Job unscheduled");
  }

  @Override
  public void jobsPaused(String jobGroup) {
    logger.info("Job paused");
  }

  @Override
  public void jobsResumed(String jobGroup) {
    logger.info("Job resumed");
  }

  @Override
  public void schedulerError(String error, SchedulerException exception) {
    logger.error(error, exception);
  }

  @Override
  public void schedulerInStandbyMode() {
    logger.info("Scheduler standby");
  }

  @Override
  public void schedulerShutdown() {
    logger.info("Scheduler shutdown");
  }

  @Override
  public void schedulerShuttingdown() {
    logger.info("Scheduler shutting down");
  }

  @Override
  public void schedulerStarted() {
    logger.info("Scheduler started");
  }

  @Override
  public void schedulerStarting() {
    logger.info("Scheduler starting");
  }

  @Override
  public void schedulingDataCleared() {
    logger.info("Scheduling data cleared");
  }

  @Override
  public void triggerFinalized(Trigger trigger) {
    logger.info("Trigger finalized");
  }

  @Override
  public void triggerPaused(TriggerKey triggerKey) {
    logger.info("Trigger paused");
  }

  @Override
  public void triggerResumed(TriggerKey triggerKey) {
    logger.info("Trigger resumed");
  }

  @Override
  public void triggersPaused(String triggerGroup) {
    logger.info("Triggers paused");
  }

  @Override
  public void triggersResumed(String triggerGroup) {
    logger.info("Triggers resumed");
  }

}
