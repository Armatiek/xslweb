package nl.armatiek.xslweb.quartz;

import java.io.ByteArrayOutputStream;

import nl.armatiek.xslweb.web.servlet.InternalRequest;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XSLWebJob implements Job {
  
  private static final Logger logger = LoggerFactory.getLogger(XSLWebJob.class);

  public XSLWebJob() {
    super();
  }
  
  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */  
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    logger.info(String.format("Executing job \"%s\"", context.getJobDetail().getKey().getName()));
    try {      
      JobDataMap dataMap = context.getMergedJobDataMap();
      String webAppPath = dataMap.getString("webapp-path");
      String path = dataMap.getString("uri");      
      InternalRequest request = new InternalRequest();
      ByteArrayOutputStream boas = new ByteArrayOutputStream();
      request.execute(webAppPath + "/" + path, boas);
      logger.info(new String(boas.toByteArray()));            
    } catch (Exception e) {
      logger.error(String.format("Error executing job \"%s\"", context.getJobDetail().getKey().getName()), e);
      throw new JobExecutionException(e);
    }
  }
}