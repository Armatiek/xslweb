package nl.armatiek.xslweb.pipeline;

import java.util.Stack;

import nl.armatiek.xslweb.configuration.Parameter;

public class TransformerStep extends PipelineStep {
  
  private Stack<Parameter> params;
  private String xslPath;  
  
  public TransformerStep(String xslPath, String name, boolean log) {
    super(name, log);
    this.xslPath = xslPath;    
  }
  
  public void addParameter(Parameter param) {
    if (params == null) {
      params = new Stack<Parameter>();
    }
    params.add(param);
  }
    
  public Stack<Parameter> getParameters() {    
    return params;
  }
  
  public String getXslPath() {
    return this.xslPath;
  }
  
}