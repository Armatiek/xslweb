package nl.armatiek.xslweb.quartz;

import java.net.InetAddress;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class XSLWebJob implements Job {

  public XSLWebJob() {
    super();
  }
  
  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  @SuppressWarnings("rawtypes")
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      JobDataMap dataMap = context.getMergedJobDataMap();      
      String path = dataMap.getString("path");
      
      String localhost = InetAddress.getLocalHost().getHostName();
      
      
    } catch (Exception e) {
      throw new JobExecutionException(e);
    }
  }
}