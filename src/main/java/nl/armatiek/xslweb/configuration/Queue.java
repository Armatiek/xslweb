package nl.armatiek.xslweb.configuration;

import org.w3c.dom.Element;

import nl.armatiek.xslweb.utils.XMLUtils;

/**
 * Queue definition
 * 
 * @author Maarten
 */
public class Queue {
  
  private String name; 
  private int nThreads;
  
  public Queue(Element queueElem) {
    this.name = XMLUtils.getValueOfChildElementByLocalName(queueElem, "name");
    this.nThreads = XMLUtils.getIntegerValue(XMLUtils.getValueOfChildElementByLocalName(queueElem, "number-of-threads"), 3);
  }
  
  public String getName() {
    return name;
  }

  public int getNumberOfThreads() {
    return nThreads;
  }

}