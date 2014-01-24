package nl.armatiek.xslweb.configuration;

import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.w3c.dom.Element;

public class Resource {
  
  private static String defaultDuration = "P0DT4H0M0S";
  
  private Pattern pattern; 
  private String mediaType;
  private Duration duration;
  
  public Resource(Element resourceElem) throws DatatypeConfigurationException {
    this.pattern = Pattern.compile(resourceElem.getAttribute("pattern"));
    this.mediaType = resourceElem.getAttribute("media-type");
    String duration;
    if (resourceElem.hasAttribute("duration")) {
      duration = resourceElem.getAttribute("duration");
    } else {
      duration = defaultDuration;
    }
    this.duration = DatatypeFactory.newInstance().newDurationDayTime(duration);
  }
  
  public Pattern getPattern() {
    return pattern;
  }

  public String getMediaType() {
    return mediaType;
  }
  
  public Duration getDuration() {
    return duration;
  }

}