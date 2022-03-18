package nl.armatiek.xslweb.pipeline;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import nl.armatiek.xslweb.configuration.Attribute;

public class WhenCondition extends Condition {
  
  private String attrName;
  private String attrValue;
  
  public WhenCondition(String attrName, String attrValue) {
    this.attrName = attrName;
    this.attrValue = attrValue;
  }
  
  @Override
  public boolean evaluate(HttpServletRequest request) {
    @SuppressWarnings("unchecked")
    List<Attribute> attr = (ArrayList<Attribute>) request.getAttribute(attrName);
    if (attr == null || attr.isEmpty()) {
      return false;
    }
    return StringUtils.equals(attr.get(0).getValue().toString(), attrValue);
  }
  
}
