package nl.armatiek.xslweb.pipeline;

public class ResponseStep extends PipelineStep {
  
  private String response;

  public ResponseStep(String response, String name) {
    super(name);   
    this.response = response;
  }
  
  public String getResponse() {
    return this.response;
  }

}
