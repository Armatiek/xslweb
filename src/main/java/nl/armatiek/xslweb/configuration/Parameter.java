package nl.armatiek.xslweb.configuration;

import java.util.ArrayList;
import java.util.List;

import nl.armatiek.xslweb.utils.XMLUtils;

import org.w3c.dom.Element;

public class Parameter {
  
  private String uri;
  private String name;  
  private String type;
  private Object value;
  
  public Parameter(String uri, String name, String type) {
    this.uri = uri;
    this.name = name;
    this.type = type;
  }
  
  public Parameter(Element paramElem) {
    this(paramElem.getAttribute("uri"), paramElem.getAttribute("name"), paramElem.getAttribute("type"));    
    Element valueElem = XMLUtils.getFirstChildElement(paramElem);
    while (valueElem != null) {
      addValue(valueElem.getTextContent());
      valueElem = XMLUtils.getNextSiblingElement(valueElem);
    }                   
  }
  
  @SuppressWarnings("unchecked")
  public void addValue(String value) {
    if (this.value == null) {
      this.value = XMLUtils.getObject(this.type, value);
    } else {
      if (!(this.value instanceof List)) {
        Object obj = this.value;
        this.value = new ArrayList<Object>();
        ((List<Object>) this.value).add(obj);
      }      
      ((List<Object>) this.value).add(XMLUtils.getObject(this.type, value));
    }
  }
  
  public String getURI() {
    return uri;
  }
  
  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public Object getValue() {
    return value;
  }
  
}