package nl.armatiek.xslweb.pipeline;

import java.util.ArrayList;
import java.util.List;

import nl.armatiek.xslweb.configuration.Parameter;

public class TransformerStep extends PipelineStep {
  
  private List<Parameter> params;
  private String xslPath;
  
  public TransformerStep(String xslPath, String name) {
    super(name);
    this.xslPath = xslPath;    
  }
  
  public void addParameter(Parameter param) {
    if (params == null) {
      params = new ArrayList<Parameter>();
    }
    params.add(param);
  }
    
  public List<Parameter> getParameters() {    
    return params;
  }
  
  public String getXslPath() {
    return this.xslPath;
  }
  
}