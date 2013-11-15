package nl.armatiek.xslweb.pipeline;

public abstract class PipelineStep {
  
  private String name;
  
  public PipelineStep(String name) {  
    this.name = name;
  }
  
  public String getName() {
    return this.name;
  }

}
