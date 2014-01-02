package nl.armatiek.xslweb.configuration;

import java.io.Serializable;

import nl.armatiek.xslweb.utils.XMLUtils;

public class Attribute implements Serializable {
  
  private static final long serialVersionUID = -6139074295723291672L;
  
  private Object value;
  private String type;
  private boolean isSerialized;
  
  public Attribute(Object value, String type, boolean isSerialized) {    
    this.value = value;
    this.type = type;
    this.isSerialized = isSerialized;
  }
  
  public Object getValue() throws Exception {
    if (isSerialized) {
      return XMLUtils.stringToDocument((String) value).getDocumentElement();
    }
    return value;
  }
  
  public String getSerializedValue() {
    if (value instanceof String) {
      return (String) value;
    }
    return null;
  }
  
  public String getType() {
    return this.type;
  }
  
  public boolean isSerialized() {
    return this.isSerialized;
  }
}