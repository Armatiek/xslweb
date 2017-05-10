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

package nl.armatiek.xslweb.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer.Builder;
import org.apache.lucene.util.Version;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import nl.armatiek.xmlindex.conf.Config;
import nl.armatiek.xslweb.error.XSLWebException;
import nl.armatiek.xslweb.utils.XMLUtils;

/**
 * Index definition
 * 
 * @author Maarten Kroon
 */
public class Index {
  
  private String name; 
  private String path;
  private Config config;
  
  public Index(XPath xpath, Element indexElem, File homeDir) throws IOException, XPathExpressionException, 
      ClassNotFoundException, InstantiationException, IllegalAccessException, SaxonApiException {
    this.name = XMLUtils.getValueOfChildElementByLocalName(indexElem, "name");
    this.path = XMLUtils.getValueOfChildElementByLocalName(indexElem, "path");   
    this.config = new Config();
    
    NodeList tdefs = (NodeList) xpath.evaluate("webapp:value-type-defs/webapp:value-type-def", indexElem, XPathConstants.NODESET);
    for (int i=0; i<tdefs.getLength(); i++) {
      Element defElem = (Element) tdefs.item(i);
      
      int nodeType = 0;  
      String nodeTypeVal = XMLUtils.getValueOfChildElementByLocalName(defElem, "node-type");
      switch (nodeTypeVal) {
        case "element()":
          nodeType = 1;
          break;
        case "attribute()":
          nodeType = 2;
          break;
        default:
          // Not possible because of XML schema validation of configuration file
      }
      
      Element nameElem = XMLUtils.getChildElementByLocalName(defElem, "name");
      QName name = getQName(nameElem.getTextContent(), nameElem);
      
      String itemTypeVal = XMLUtils.getValueOfChildElementByLocalName(defElem, "item-type");
      ItemType itemType = Type.getBuiltInItemType(Definitions.NAMESPACEURI_XMLSCHEMA, StringUtils.substringAfter(itemTypeVal, ":"));      
      config.addTypedValueDef(nodeType, name, itemType);
    }
    
    NodeList adefs = (NodeList) xpath.evaluate("webapp:virtual-attribute-defs/webapp:virtual-attribute-def", indexElem, XPathConstants.NODESET);
    if (adefs.getLength() > 0) {
      String xqueryPath = (String) xpath.evaluate("webapp:virtual-attribute-defs/webapp:xquery-path/text()", indexElem, XPathConstants.STRING);
      File file = new File(xqueryPath);
      if (!file.isAbsolute())
        file = new File(homeDir, xqueryPath);
      if (!file.isFile())
        throw new FileNotFoundException("Virtual attributes XQuery file \"" + file.getAbsolutePath() + "\" not found");
      FileInputStream fis = new FileInputStream(file);
      try {
        config.loadVirtualAttrsXQuery(fis, file.toURI());
      } finally {
        fis.close();
      }
    }
      
    for (int i=0; i<adefs.getLength(); i++) {
      Element defElem = (Element) adefs.item(i);
      Element elemNameElem = XMLUtils.getChildElementByLocalName(defElem, "elem-name");
      QName elemName = getQName(elemNameElem.getTextContent(), elemNameElem);
      String virtualAttrName = XMLUtils.getValueOfChildElementByLocalName(defElem, "virtual-attribute-name");
      Element functionNameElem = XMLUtils.getChildElementByLocalName(defElem, "function-name");
      QName functionName = getQName(functionNameElem.getTextContent(), functionNameElem);
      String itemTypeVal = XMLUtils.getValueOfChildElementByLocalName(defElem, "item-type");
      ItemType itemType = Type.getBuiltInItemType(Definitions.NAMESPACEURI_XMLSCHEMA, StringUtils.substringAfter(itemTypeVal, ":"));
      
      Analyzer indexAnalyzer = null;
      Analyzer queryAnalyzer = null;
      NodeList analyzers = defElem.getElementsByTagNameNS(Definitions.NAMESPACEURI_XSLWEB_WEBAPP, "analyzer");
      for (int j=0; j<analyzers.getLength(); j++) {
        Analyzer analyzer;
        Element analyzerElem = (Element) analyzers.item(j);
        String analyzerClass = analyzerElem.getAttribute("class");
        String analyzerType = analyzerElem.getAttribute("type");
        if (StringUtils.isNotEmpty(analyzerClass)) {
          Class<?> clazz = Class.forName(analyzerClass);
          analyzer = (Analyzer) clazz.newInstance();
        } else {
          Builder builder = CustomAnalyzer.builder(homeDir.toPath());
          Element childElem = XMLUtils.getFirstChildElement(analyzerElem);
          while (childElem != null) {
            String localName = childElem.getLocalName();
            switch (localName) {
            case "tokenizer":
              builder = builder.withTokenizer(childElem.getAttribute("class"), attrToParams(childElem));
              break;
            case "filter":
              builder = builder.addTokenFilter(childElem.getAttribute("class"), attrToParams(childElem));
              break;
            case "charFilter":
              builder = builder.addCharFilter(childElem.getAttribute("class"), attrToParams(childElem));
              break;
            }
            childElem = XMLUtils.getNextSiblingElement(childElem);
          }
          analyzer = builder.build();
        }
        if (StringUtils.isEmpty(analyzerType)) {
          indexAnalyzer = analyzer;
          queryAnalyzer = analyzer;
        } else if (analyzerType.equals("index")) {
          indexAnalyzer = analyzer;
        } else if (analyzerType.equals("query")) {
          queryAnalyzer = analyzer;
        } 
      }
      if ((indexAnalyzer != null && queryAnalyzer == null) || (queryAnalyzer != null && indexAnalyzer == null))
        throw new XSLWebException("Both index and query analyzer must be coonfigured for virtual attribute \"" + name + "\"");
      config.addVirtualAttributeDef(elemName, virtualAttrName, functionName, itemType, indexAnalyzer, queryAnalyzer);
    }
  }
  
  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }
    
  public Config getConfig() {
    return config;
  }
  
  private Map<String, String> attrToParams(Element elem) {
    Map<String, String> params = new HashMap<String, String>();
    NamedNodeMap attrs = elem.getAttributes();
    for (int i=0; i<attrs.getLength(); i++) {
      Attr attr = (Attr) attrs.item(i);
      String localName = attr.getNamespaceURI() == null ? attr.getName() : attr.getLocalName();
      if (localName.equals("class"))
        continue;
      params.put(localName, attr.getValue());
    }
    params.put("luceneMatchVersion", Version.LATEST.toString());
    return params;
  }
  
  private QName getQName(String qName, Element elem) {
    if (qName.indexOf(':') == -1)
      return new QName(qName);
    String prefix = StringUtils.substringBefore(qName, ":");
    String localName = StringUtils.substringAfter(qName, ":");
    String namespace = XMLUtils.getNamespace(elem, prefix);
    return new QName(prefix, namespace, localName);
    
  }
  
}