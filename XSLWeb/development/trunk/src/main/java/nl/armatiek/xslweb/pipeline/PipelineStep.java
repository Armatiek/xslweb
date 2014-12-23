package nl.armatiek.xslweb.pipeline;

public abstract class PipelineStep {
  
  private String name;
  private boolean log;
  
  public PipelineStep(String name, boolean log) {  
    this.name = name;
    this.log = log;
  }
  
  public String getName() {
    return this.name;
  }
  
  public boolean getLog() {
    return this.log;
  }

}
