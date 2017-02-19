package nl.armatiek.xslweb.saxon.functions.diff.hddiff;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DeltaInfo {
  
  private List<DeleteInfo> deletes;
  private Element attrsElem;
  private Element textGroupElem;
  
  public void addDeletedNode(Node deletedNode) {
    if (deletes == null) 
      deletes = new ArrayList<DeleteInfo>();
    deletes.add(0, new DeleteInfo(deletedNode));
  }
  
  public void addAttributesElem(Element attrsElem) {
    this.attrsElem = attrsElem;
  }
  
  public void addTextGroupElem(Element textGroupElem) {
    this.textGroupElem = textGroupElem;
  }
  
  public Iterator<DeleteInfo> getDeletedNodes() {
    if (deletes == null)
      return null;
    return deletes.iterator();
  }
  
  public Element getAttrsElem() {
    return this.attrsElem;
  }
  
  public Element getTextGroupElem() {
    return this.textGroupElem;
  }
  
  public static final class DeleteInfo {
    
    public final Node deletedNode;
    public final Node nextSiblingNode;
    public final Node prevSiblingNode;
    public final int position;

    public DeleteInfo(Node deletedNode) {
      this.deletedNode = deletedNode;
      this.nextSiblingNode = this.deletedNode.getNextSibling();
      this.prevSiblingNode = this.deletedNode.getPreviousSibling();
      int index = 0;
      Node tmp = this.deletedNode;
      while (true) {
        tmp = tmp.getPreviousSibling();
        if (tmp == null)
          break;
        ++index;
      }
      this.position = index;
    }
    
  }

}