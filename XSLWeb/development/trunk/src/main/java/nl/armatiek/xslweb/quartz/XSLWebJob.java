package nl.armatiek.xslweb.quartz;

import nl.armatiek.xslweb.configuration.Config;
import nl.armatiek.xslweb.utils.XSLWebUtils;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
      Config config = Config.getInstance();
      JobDataMap dataMap = context.getMergedJobDataMap();
      String uri = XSLWebUtils.resolveProperties(dataMap.getString("uri"), config.getProperties());           
      CloseableHttpClient httpClient = HttpClients.createDefault();
      try {
        HttpGet httpget = new HttpGet(uri);
        CloseableHttpResponse response = httpClient.execute(httpget);
        try {
          String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
          logger.info(responseBody);
        } finally {
          response.close();        
        }
      } finally {
        httpClient.close();
      }
    } catch (Exception e) {
      logger.error(String.format("Error executing job \"%s\"", context.getJobDetail().getKey().getName()), e);
      throw new JobExecutionException(e);
    }
  }
}