package nl.armatiek.xslweb.saxon.utils;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.LocalNameTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.type.Type;

public class NodeInfoUtils {
  
  public static NodeInfo getFirstChildElement(NodeInfo parentElement) {
    return parentElement.iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT).next();    
  }
  
  public static NodeInfo getNextSiblingElement(NodeInfo parentElement) {
    return parentElement.iterateAxis(AxisInfo.FOLLOWING_SIBLING, NodeKindTest.ELEMENT).next();    
  }
  
  public static String getValueOfChildElementByLocalName(NodeInfo parentElement, String localName, XPathContext context) {
    NodeInfo nodeInfo = parentElement.iterateAxis(AxisInfo.CHILD, new LocalNameTest(context.getNamePool(), Type.ELEMENT, localName)).next();
    if (nodeInfo != null) {
      return nodeInfo.getStringValue();
    }
    return null;
  }

}
