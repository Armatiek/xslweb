package nl.armatiek.xslweb.saxon.utils;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
