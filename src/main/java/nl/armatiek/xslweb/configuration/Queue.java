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
  private int numberOfThreads;
  private int maxQueueSize;
  
  public Queue(Element queueElem) {
    this.name = XMLUtils.getValueOfChildElementByLocalName(queueElem, "name");
    this.numberOfThreads = XMLUtils.getIntegerValue(XMLUtils.getValueOfChildElementByLocalName(queueElem, "number-of-threads"), 3);
    this.maxQueueSize = XMLUtils.getIntegerValue(XMLUtils.getValueOfChildElementByLocalName(queueElem, "max-queue-size"), 3);
  }
  
  public String getName() {
    return name;
  }

  public int getNumberOfThreads() {
    return numberOfThreads;
  }
  
  public int getMaxQueueSize() {
    return maxQueueSize;
  }

}