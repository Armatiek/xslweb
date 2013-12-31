package nl.armatiek.xslweb.configuration;

import nl.armatiek.xslweb.utils.XMLUtils;

public class Attribute {
  
  private String name;
  private Object value;
  private boolean isSerialized;
  
  public Attribute(String name, Object value, boolean isSerialized) {
    this.name = name;
    this.value = value;
    this.isSerialized = isSerialized;
  }
  
  public String getName() {
    return name;
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
  
  public boolean isSerialized() {
    return this.isSerialized;
  }
}