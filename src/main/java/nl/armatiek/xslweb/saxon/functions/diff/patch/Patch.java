package nl.armatiek.xslweb.saxon.functions.diff.patch;

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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import nl.armatiek.xslweb.saxon.functions.diff.node.Document;
import nl.armatiek.xslweb.saxon.functions.diff.node.ElementNode;
import nl.armatiek.xslweb.saxon.functions.diff.node.TextNode;
import nl.armatiek.xslweb.saxon.functions.diff.node.TreeNode;
import nl.armatiek.xslweb.saxon.functions.diff.text.DiffMatchPatch;
import nl.armatiek.xslweb.saxon.functions.diff.text.DiffMatchPatch.Diff;
import nl.armatiek.xslweb.saxon.functions.diff.util.DiffUtils;

public class Patch {
  
  private static final Logger logger = LoggerFactory.getLogger(Patch.class);
  
  public static final String NAMESPACE_DELTAXML = "http://www.deltaxml.com/ns/well-formed-delta-v1";
  public static final String NAMESPACE_DXA      = "http://www.deltaxml.com/ns/non-namespaced-attribute";
  public static final String NAMESPACE_DXX      = "http://www.deltaxml.com/ns/xml-namespaced-attribute";
  public static final String PREFIX_DELTAXML    = "deltaxml";
  public static final String PREFIX_DXA         = "dxa";
  public static final String PREFIX_DXX         = "dxx";
  
  private DiffMatchPatch dmp;
  
  public Patch() {
    this.dmp = new DiffMatchPatch();
  }
  
  public void patch(TreeNode node1, Document delta) {
    TreeNode root = delta.getChild(0);
    TreeNode deltaChild = root.getFirstChild();
    while (deltaChild != null) {
      if (deltaChild.getNodeType() != Node.ELEMENT_NODE)
        continue;
      ElementNode deltaChildElem = (ElementNode) deltaChild;
      String pos = deltaChildElem.getAttribute("pos");
      TreeNode node = null;
      switch (deltaChildElem.getElementName()) {
        case "Deleted":
          node = node1.getDescendantByPos(pos);
          node.getParent().removeChild(node);
          break;
        case "Inserted":
          int lastIndex = StringUtils.lastIndexOf(pos, ":");
          String parentPos = StringUtils.substring(pos, 0, lastIndex);
          String childPos = StringUtils.substring(pos, lastIndex+1);
          node = node1.getDescendantByPos(parentPos);
          node.insertChild(Integer.parseInt(childPos), DiffUtils.cloneTreeNode(deltaChild.getFirstChild()));
          break;
        case "AttributeInserted":
          node = node1.getDescendantByPos(pos);
          node.setAttribute(deltaChildElem.getAttribute("name"), deltaChildElem.getAttribute("value"));
          break;
        case "AttributeDeleted":
          node = node1.getDescendantByPos(pos);
          node.removeAttribute(deltaChildElem.getAttribute("name"));
          break;
        case "AttributeUpdated":
          node = node1.getDescendantByPos(pos);
          // deltaChildElem.getAttribute("ov")
          node.setAttribute(deltaChildElem.getAttribute("name"), deltaChildElem.getAttribute("nv"));
          break;
      }  
      deltaChild = deltaChild.getNextSibling();
    }
  }
  
  public void markParentsChanged(TreeNode node) {
    TreeNode parent = node.getParent();
    while (parent != null) {
      parent.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "A!=B");
      parent = parent.getParent();
    }
  }
  
  private void addRootAttributes(TreeNode node) {
    if (node.getParent() != null && node.getParent().getNodeType() == Node.DOCUMENT_NODE) {
      node.setAttribute("{" + NAMESPACE_DELTAXML + "}version", "2.0");
      node.setAttribute("{" + NAMESPACE_DELTAXML + "}content-type", "full-context");
    }
  }
  
  public void processEqualNode(TreeNode node) {
    if ((node.getNodeType() == Node.ELEMENT_NODE) && StringUtils.isBlank(node.getAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2"))) {
      node.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "A=B");
    }
  }
  
  public boolean processSingleTextChildDiff(TreeNode parentNode) {
    boolean skipChilds = false;
    if (parentNode.getChildren().size() == 2 && 
        (parentNode.getChild(0).isMarkedAsUpdateOld() || parentNode.getChild(0).isMarkedAsUpdateNew()) &&
        (parentNode.getChild(1).isMarkedAsUpdateOld() || parentNode.getChild(1).isMarkedAsUpdateNew())) {
      
      skipChilds = true;
            
      TreeNode oldNode = parentNode.getChild(0).isMarkedAsUpdateOld() ? parentNode.getChild(0) : parentNode.getChild(1);
      TreeNode newNode = parentNode.getChild(0).isMarkedAsUpdateNew() ? parentNode.getChild(0) : parentNode.getChild(1);
     
      String oldText = ((TextNode) oldNode).getContent();
      String newText = ((TextNode) newNode).getContent();
      
      LinkedList<DiffMatchPatch.Diff> diffs = dmp.diffMain(oldText, newText);
      
      parentNode.removeChild(oldNode);
      parentNode.removeChild(newNode);
      
      ElementNode textGroupElem, textElem;
      for (Diff diff : diffs) {
        switch (diff.operation) {
        case INSERT:
          textGroupElem = new ElementNode("{" + NAMESPACE_DELTAXML + "}textGroup");
          textGroupElem.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "B");
          textElem = new ElementNode("{" + NAMESPACE_DELTAXML + "}text");
          textElem.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "B");
          textGroupElem.appendChild(textElem);
          textElem.appendChild(new TextNode(diff.text));
          parentNode.appendChild(textGroupElem);
          break;
        case DELETE:
          textGroupElem = new ElementNode("{" + NAMESPACE_DELTAXML + "}textGroup");
          textGroupElem.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "A");
          textElem = new ElementNode("{" + NAMESPACE_DELTAXML + "}text");
          textElem.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "A");
          textGroupElem.appendChild(textElem);
          textElem.appendChild(new TextNode(diff.text));
          parentNode.appendChild(textGroupElem);
          break;
        case EQUAL:
          parentNode.appendChild(new TextNode(diff.text));
          break;
        } 
      }
    }
    return skipChilds;
  }
  
  private void processAttributeChangesAndDeletedChildren(TreeNode node) {
    int offset = 0;
    ElementNode deltaXmlAttributes = node.getDeltaXmlAttributes();
    if (deltaXmlAttributes != null) {
      node.insertChild(0, deltaXmlAttributes);
      offset++;
    }
    List<TreeNode> deletedChildren = node.getDeletedChildren();
    Collections.sort(deletedChildren, new Comparator<TreeNode>() {
      @Override
      public int compare(TreeNode node1, TreeNode node2) {
        return new Integer(node1.getDeletedFromPos()).compareTo(new Integer(node2.getDeletedFromPos()));
      }
    });
    for (TreeNode deletedChild : deletedChildren) {
      int pos = deletedChild.getDeletedFromPos() + offset;
      deletedChild.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "A");
      node.insertChild(pos, deletedChild);
      offset++;
    }
  }
  
  public void addDeltaVInformation(TreeNode node) {
    if (node.getNodeType() != Node.ELEMENT_NODE && node.getNodeType() != Node.DOCUMENT_NODE) {
      return;
    }
    
    boolean skipChilds = false;
    
    addRootAttributes(node);
    
    processEqualNode(node);
    
    processAttributeChangesAndDeletedChildren(node);
    
    skipChilds = processSingleTextChildDiff(node);
    
    if (!skipChilds) {
      for (TreeNode child : node.getChildren()) {
        addDeltaVInformation(child);
      }
    }
  }
  
  public void patchDeltaV2(TreeNode node1, Document delta) {
    TreeNode root = delta.getChild(0);
    TreeNode deltaChild = root.getFirstChild();
    while (deltaChild != null) {
      if (deltaChild.getNodeType() != Node.ELEMENT_NODE)
        continue;
      ElementNode deltaChildElem = (ElementNode) deltaChild;
      String pos = deltaChildElem.getAttribute("pos");
      TreeNode node = null;
      ElementNode attrsElem;
      ElementNode attrElem;
      ElementNode valElem;
      ElementNode textGroupElem;
      ElementNode textElem;
      switch (deltaChildElem.getElementName()) {
        case "Deleted":
          node = node1.getDescendantByPos(pos);
          
          if (node == null) {
            logger.warn("Node not found: " + deltaChildElem.toString());
            break;
          }
          
          markParentsChanged(node);
          if ((node.getNodeType() == Node.TEXT_NODE) && StringUtils.equals(deltaChildElem.getAttribute("update"), "yes")) {
            node.markAsUpdateOld();
          } else if (node.getNodeType() == Node.ELEMENT_NODE) {
            node.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "A");
            node.getParent().removeChild(node);
          } else if (node.getNodeType() == Node.TEXT_NODE) {
            textGroupElem = new ElementNode("{" + NAMESPACE_DELTAXML + "}textGroup");
            textGroupElem.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "A");
            textElem = new ElementNode("{" + NAMESPACE_DELTAXML + "}text");
            textElem.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "A");
            textGroupElem.appendChild(textElem);
            textElem.appendChild(new TextNode(((TextNode) node).getContent()));
            node.getParent().replaceChild(textGroupElem, node);
            textGroupElem.getParent().removeChild(textGroupElem);
          }
          break;
        case "Inserted":
          int lastIndex = StringUtils.lastIndexOf(pos, ":");
          String parentPos = StringUtils.substring(pos, 0, lastIndex);
          String childPos = StringUtils.substring(pos, lastIndex+1);
          node = node1.getDescendantByPos(parentPos);
          
          if (node == null) {
            logger.warn("Node not found: " + deltaChildElem.toString());
            break;
          }
          
          TreeNode newNode = DiffUtils.cloneTreeNode(deltaChild.getFirstChild());
          if (newNode.getNodeType() == Node.ELEMENT_NODE) {
            newNode.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "B");
            node.insertChild(Integer.parseInt(childPos), newNode);
            markParentsChanged(newNode);
          } else if ((newNode.getNodeType() == Node.TEXT_NODE) && StringUtils.equals(deltaChildElem.getAttribute("update"), "yes")) {
            newNode.markAsUpdateNew();
            node.insertChild(Integer.parseInt(childPos), newNode);
            markParentsChanged(newNode);
          } else if (newNode.getNodeType() == Node.TEXT_NODE) {
            textGroupElem = new ElementNode("{" + NAMESPACE_DELTAXML + "}textGroup");
            textGroupElem.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "B");
            textElem = new ElementNode("{" + NAMESPACE_DELTAXML + "}text");
            textElem.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "B");
            textGroupElem.appendChild(textElem);
            textElem.appendChild(newNode);
            node.insertChild(Integer.parseInt(childPos), textGroupElem);
            markParentsChanged(textGroupElem);
          }
          
          break;
        case "AttributeInserted":
          node = node1.getDescendantByPos(pos);
          
          if (node == null) {
            logger.warn("Node not found: " + deltaChildElem.toString());
            break;
          }
          
          String attrName = deltaChildElem.getAttribute("name");
          
          attrsElem = node.getDeltaXmlAttributes();
          if (attrsElem == null) {
            attrsElem = new ElementNode("{" + NAMESPACE_DELTAXML + "}attributes");
            attrsElem.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "A!=B");
          }
          if (attrName.startsWith("{")) {
            attrElem = new ElementNode(attrName);
          } else {
            attrElem = new ElementNode("{" + NAMESPACE_DXA + "}" + attrName);
          }
          attrElem.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "B");
          attrsElem.appendChild(attrElem);
          valElem = new ElementNode("{" + NAMESPACE_DELTAXML + "}attributeValue");
          valElem.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "B");
          attrElem.appendChild(valElem);
          valElem.appendChild(new TextNode(deltaChildElem.getAttribute("value")));
          node.setDeltaXmlAttributes(attrsElem);
          
          node.removeAttribute(attrName);
          
          node.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "A!=B");
          markParentsChanged(node);
          break;
        case "AttributeDeleted":
          node = node1.getDescendantByPos(pos);
          
          if (node == null) {
            logger.warn("Node not found: " + deltaChildElem.toString());
            break;
          }
          
          attrName = deltaChildElem.getAttribute("name");
          
          attrsElem = node.getDeltaXmlAttributes();
          if (attrsElem == null) {
            attrsElem = new ElementNode("{" + NAMESPACE_DELTAXML + "}attributes");
            attrsElem.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "A!=B");
          }
          if (attrName.startsWith("{")) {
            attrElem = new ElementNode(attrName);
          } else {
            attrElem = new ElementNode("{" + NAMESPACE_DXA + "}" + attrName);
          }
          attrElem.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "A");
          attrsElem.appendChild(attrElem);
          //valElem = new ElementNode("{" + NAMESPACE_DELTAXML + "}attributeValue");
          //valElem.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "A");
          //attrElem.appendChild(valElem);
          //valElem.appendChild(new TextNode(deltaChildElem.getAttribute("value")));
          node.setDeltaXmlAttributes(attrsElem);
          
          node.removeAttribute(attrName);
          
          node.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "A!=B");
          markParentsChanged(node);
          break;
        case "AttributeUpdated":
          node = node1.getDescendantByPos(pos);
          
          if (node == null) {
            logger.warn("Node not found: " + deltaChildElem.toString());
            break;
          }
          
          attrName = deltaChildElem.getAttribute("name");
          
          attrsElem = node.getDeltaXmlAttributes();
          if (attrsElem == null) {
            attrsElem = new ElementNode("{" + NAMESPACE_DELTAXML + "}attributes");
            attrsElem.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "A!=B");
          }
          if (attrName.startsWith("{")) {
            attrElem = new ElementNode(attrName);
          } else {
            attrElem = new ElementNode("{" + NAMESPACE_DXA + "}" + attrName);
          }
          attrElem.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "A!=B");
          attrsElem.appendChild(attrElem);
          valElem = new ElementNode("{" + NAMESPACE_DELTAXML + "}attributeValue");
          valElem.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "A");
          attrElem.appendChild(valElem);
          valElem.appendChild(new TextNode(deltaChildElem.getAttribute("ov")));
          valElem = new ElementNode("{" + NAMESPACE_DELTAXML + "}attributeValue");
          valElem.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "B");
          attrElem.appendChild(valElem);
          valElem.appendChild(new TextNode(deltaChildElem.getAttribute("nv")));
          node.setDeltaXmlAttributes(attrsElem);
          
          node.removeAttribute(attrName);
          
          node.setAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2", "A!=B");
          markParentsChanged(node);
          break;
      }  
      deltaChild = deltaChild.getNextSibling();
    }
    
    addDeltaVInformation(node1);
    
  }
  
  /*
  private boolean isAncestorInserted(TreeNode node) {
    while (node != null) {
      if (StringUtils.equals(node.getAttribute("{" + NAMESPACE_DELTAXML + "}deltaV2"), "B")) {
        return true;
      }
      node = node.getParent();
    }
    return false;
  }
  */
  
}