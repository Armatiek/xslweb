package nl.armatiek.xslweb.pipeline;

public class TransformerStep extends PipelineStep {
  
  private String xslPath;
  
  public TransformerStep(String xslPath) {
    this.xslPath = xslPath;
  }
  
  public String getXslPath() {
    return this.xslPath;
  }
  
}