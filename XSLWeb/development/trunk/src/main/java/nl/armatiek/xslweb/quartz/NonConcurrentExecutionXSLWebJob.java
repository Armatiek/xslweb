package nl.armatiek.xslweb.quartz;

import org.quartz.DisallowConcurrentExecution;

@DisallowConcurrentExecution
public class NonConcurrentExecutionXSLWebJob extends XSLWebJob {
	
	public NonConcurrentExecutionXSLWebJob() {
    super();
  }

}