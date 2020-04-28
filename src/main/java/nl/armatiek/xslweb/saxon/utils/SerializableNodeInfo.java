package nl.armatiek.xslweb.saxon.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.parser.Location;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Genre;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.SchemaType;

public class SerializableNodeInfo implements NodeInfo, Serializable {
  
  private static final long serialVersionUID = 3233046816831468756L;
  
  private static final transient Processor processor = new Processor(false);
  
  // private transient WebApp webApp;
  private transient NodeInfo nodeInfo;
 
  public SerializableNodeInfo(NodeInfo nodeInfo) {
    // this.webApp = webApp;
    this.nodeInfo = nodeInfo;
  }
  
  private void writeObject(ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject();
    try {
      Serializer serializer = processor.newSerializer();
      serializer.setOutputProperty(Serializer.Property.ENCODING, "UTF-8");
      String xml = serializer.serializeNodeToString(new XdmNode(nodeInfo));
      oos.writeObject(xml);
      oos.flush();
    } catch (SaxonApiException e) {
      throw new IOException("Error serializing NodeInfo", e);
    }
  }

  private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
    try {
      String xml = (String) ois.readObject();
      DocumentBuilder builder = processor.newDocumentBuilder();
      XdmNode node = builder.build(new StreamSource(new StringReader(xml)));
      nodeInfo = node.getUnderlyingNode();
    } catch (SaxonApiException e) {
      throw new IOException("Error deserializing NodeInfo", e);
    }
  }

  @Override
  public void setSystemId(String systemId) {
    nodeInfo.setSystemId(systemId);
  }

  @Override
  public NodeInfo head() {
    return nodeInfo.head();
  }
  
  /*
  public SequenceIterator iterate() throws XPathException {
    return nodeInfo.iterate();
  }
  */

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
  public AxisIterator iterateAxis(byte axisNumber) {
    return nodeInfo.iterateAxis(axisNumber);
  }

  @Override
  public AxisIterator iterateAxis(byte axisNumber, NodeTest nodeTest) {
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

}