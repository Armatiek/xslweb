package nl.armatiek.xslweb.configuration;

import java.util.regex.Pattern;

import org.w3c.dom.Element;

public class Resource {
  
  private Pattern pattern; 
  private String mediaType;
  
  public Resource(Element resourceElem) {
    this.pattern = Pattern.compile(resourceElem.getAttribute("pattern"));
    this.mediaType = resourceElem.getAttribute("media-type");    
  }
  
  public Pattern getPattern() {
    return pattern;
  }

  public String getMediaType() {
    return mediaType;
  }

}