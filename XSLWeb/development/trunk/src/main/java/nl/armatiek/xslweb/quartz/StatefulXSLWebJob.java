package nl.armatiek.xslweb.quartz;

import org.quartz.DisallowConcurrentExecution;

@DisallowConcurrentExecution
public class StatefulXSLWebJob extends XSLWebJob {
	
	public StatefulXSLWebJob() {
    super();
  }

}