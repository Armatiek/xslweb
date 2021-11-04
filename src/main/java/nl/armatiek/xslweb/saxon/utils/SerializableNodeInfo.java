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
package nl.armatiek.xslweb.saxon.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.function.Predicate;

import javax.xml.stream.XMLStreamReader;

import org.ehcache.sizeof.annotations.IgnoreSizeOf;

import com.sun.xml.fastinfoset.stax.StAXDocumentParser;
import com.sun.xml.fastinfoset.stax.StAXDocumentSerializer;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Genre;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.pull.PullSource;
import net.sf.saxon.pull.StaxBridge;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.stax.XMLStreamWriterDestination;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.SchemaType;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.WebApp;

public class SerializableNodeInfo implements NodeInfo, Serializable {
  
  private static final long serialVersionUID = 3233046816831468756L;
  
  private transient NodeInfo nodeInfo;
  
  @IgnoreSizeOf
  private transient WebApp webApp;
  
  public SerializableNodeInfo(NodeInfo nodeInfo, WebApp webApp) {
    this.nodeInfo = nodeInfo;
    this.webApp = webApp;
  }
  
  private void writeObject(ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject();
    try {
      oos.writeObject(webApp.getPath());
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      StAXDocumentSerializer serializer = new StAXDocumentSerializer(baos);
      Processor processor = webApp.getProcessor();
      processor.writeXdmValue(new XdmNode(nodeInfo), new XMLStreamWriterDestination(serializer));
      oos.writeObject(baos.toByteArray());
      oos.flush();
    } catch (SaxonApiException e) {
      throw new IOException("Error serializing NodeInfo", e);
    }
  }

  private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
    try {
      String webAppPath = (String) ois.readObject();
      if (webApp == null) {
        webApp =  Context.getInstance().getWebApp(webAppPath);
      }
      byte[] buffer = (byte[]) ois.readObject();
      ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
      XMLStreamReader streamReader = new StAXDocumentParser(bais);
      StaxBridge staxBridge = new StaxBridge();
      staxBridge.setXMLStreamReader(streamReader);
      PullSource source = new PullSource(staxBridge);
      Configuration config = webApp.getConfiguration();
      ParseOptions parseOptions = new ParseOptions(config.getParseOptions());
      parseOptions.setXIncludeAware(false);
      nodeInfo = config.buildDocumentTree(source, parseOptions).getRootNode();
    } catch (XPathException e) {
      throw new IOException("Error deserializing NodeInfo", e);
    }
  }

  @Override
  public void setSystemId(String systemId) {
    nodeInfo.setSystemId(systemId);
  }

  @Override
  public Item head() {
    return nodeInfo.head();
  }
  
  @Override
  public Location saveLocation() {
    return nodeInfo.saveLocation();
  }

  @Override
  public CharSequence getStringValueCS() {
    return nodeInfo.getStringValueCS();
  }

  @Override
  public TreeInfo getTreeInfo() {
    return nodeInfo.getTreeInfo();
  }

  @Override  
  public Configuration getConfiguration() {
    return nodeInfo.getConfiguration();
  }

  @Override
  public int getNodeKind() {
    return nodeInfo.getNodeKind();
  }

  @Override
  public boolean isSameNodeInfo(NodeInfo other) {
    return nodeInfo.isSameNodeInfo(other);
  }

  @Override
  public boolean equals(Object other) {
    return nodeInfo.equals(other);
  }

  @Override
  public int hashCode() {
    return nodeInfo.hashCode();
  }

  @Override
  public String getSystemId() {
    return nodeInfo.getSystemId();
  }

  @Override
  public String getPublicId() {
    return nodeInfo.getPublicId();
  }

  @Override
  public String getBaseURI() {
    return nodeInfo.getBaseURI();
  }

  @Override
  public int getLineNumber() {
    return nodeInfo.getLineNumber();
  }

  @Override
  public int getColumnNumber() {
    return nodeInfo.getColumnNumber();
  }

  @Override
  public int compareOrder(NodeInfo other) {
    return nodeInfo.compareOrder(other);
  }

  @Override
  public String getStringValue() {
    return nodeInfo.getStringValue();
  }

  @Override
  public boolean hasFingerprint() {
    return nodeInfo.hasFingerprint();
  }

  @Override
  public int getFingerprint() {
    return nodeInfo.getFingerprint();
  }

  @Override
  public String getLocalPart() {
    return nodeInfo.getLocalPart();
  }

  @Override
  public String getURI() {
    return nodeInfo.getURI();
  }

  @Override
  public String getDisplayName() {
    return nodeInfo.getDisplayName();
  }

  @Override
  public String getPrefix() {
    return nodeInfo.getPrefix();
  }

  @Override
  public SchemaType getSchemaType() {
    return nodeInfo.getSchemaType();
  }

  @Override
  public AtomicSequence atomize() throws XPathException {
    return nodeInfo.atomize();
  }

  @Override
  public NodeInfo getParent() {
    return nodeInfo.getParent();
  }

  @Override
  public AxisIterator iterateAxis(int axisNumber, Predicate<? super NodeInfo> nodeTest) {
    return nodeInfo.iterateAxis(axisNumber, nodeTest);
  }

  @Override
  public String getAttributeValue(String uri, String local) {
    return nodeInfo.getAttributeValue(uri, local);
  }

  @Override
  public NodeInfo getRoot() {
    return nodeInfo.getRoot();
  }

  @Override
  public boolean hasChildNodes() {
    return nodeInfo.hasChildNodes();
  }

  @Override
  public void generateId(FastStringBuffer buffer) {
    nodeInfo.generateId(buffer);
  }

  @Override
  public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
    nodeInfo.copy(out, copyOptions, locationId);
  }

  @Override
  public NamespaceBinding[] getDeclaredNamespaces(NamespaceBinding[] buffer) {
    return nodeInfo.getDeclaredNamespaces(buffer);
  }

  @Override
  public boolean isId() {
    return nodeInfo.isId();
  }

  @Override
  public boolean isIdref() {
    return nodeInfo.isIdref();
  }

  @Override
  public boolean isNilled() {
    return nodeInfo.isNilled();
  }

  @Override
  public boolean isStreamed() {
    return nodeInfo.isStreamed();
  }

  @Override
  public String toShortString() {
    return nodeInfo.toShortString();
  }

  @Override
  public Genre getGenre() {
    return nodeInfo.getGenre();
  }

  @Override
  public NamespaceMap getAllNamespaces() {
    return nodeInfo.getAllNamespaces();
  }
  
}