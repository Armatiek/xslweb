package nl.armatiek.xslweb.quartz;

import java.net.URI;

import nl.armatiek.xslweb.configuration.Context;

import org.apache.commons.lang3.StringUtils;
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
      Context ctx = Context.getInstance();
      JobDataMap dataMap = context.getMergedJobDataMap();
      String webAppPath = dataMap.getString("webapp-path");
      String path = dataMap.getString("uri");      
      CloseableHttpClient httpClient = HttpClients.createDefault();
      try {                       
        URI uri = new URI("http", null, ctx.getLocalHost(), ctx.getPort(), ctx.getContextPath() + webAppPath + "/" + StringUtils.stripStart(path, "/"), null, null);
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