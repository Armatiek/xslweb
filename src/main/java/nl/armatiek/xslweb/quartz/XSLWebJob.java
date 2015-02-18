package nl.armatiek.xslweb.quartz;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.ByteArrayOutputStream;

import nl.armatiek.xslweb.web.servlet.InternalRequest;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quartz Job class that executes a internal XSLWeb pipeline request.
 * 
 * @author Maarten
 */
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