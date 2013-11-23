package nl.armatiek.xslweb.pipeline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TransformerStep extends PipelineStep {
  
  private List<StylesheetParameter> params;
  private String xslPath;
  
  public TransformerStep(String xslPath, String name) {
    super(name);
    this.xslPath = xslPath;    
  }
  
  public void addParameter(StylesheetParameter param) {
    if (params == null) {
      params = new ArrayList<StylesheetParameter>();
    }
    params.add(param);
  }
    
  public Iterator<StylesheetParameter> getStylesheetParameters() {
    if (params != null) {
      return params.iterator();
    }
    return null;
  }
  
  public String getXslPath() {
    return this.xslPath;
  }
  
}