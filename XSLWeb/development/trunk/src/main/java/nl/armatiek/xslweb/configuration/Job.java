package nl.armatiek.xslweb.configuration;

import nl.armatiek.xslweb.utils.XMLUtils;

import org.w3c.dom.Element;

public class Job {
  
  private String name; 
  private String uri;
  private String cron;
  
  public Job(Element jobElem) {
    this.name = XMLUtils.getValueOfChildElementByLocalName(jobElem, "name");
    this.uri = XMLUtils.getValueOfChildElementByLocalName(jobElem, "uri");
    this.cron = XMLUtils.getValueOfChildElementByLocalName(jobElem, "cron");
  }
  
  public String getName() {
    return name;
  }

  public String getUri() {
    return uri;
  }

  public String getCron() {
    return cron;
  }
  
}