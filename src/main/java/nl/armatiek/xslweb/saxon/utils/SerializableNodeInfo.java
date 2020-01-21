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
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
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
  
  private transient NodeInfo nodeInfo;
  
  public SerializableNodeInfo(NodeInfo nodeInfo) {
    this.nodeInfo = nodeInfo;
  }
  
  private void writeObject(ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject();
    try {
      Serializer serializer = processor.newSerializer();
      serializer.setOutputProperty(Serializer.Property.ENCODING, "UTF-8");
      String xml = serializer.serializeNodeToString(new XdmNode(this));
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

  public void setSystemId(String systemId) {
    nodeInfo.setSystemId(systemId);
  }

  public Item head() {
    return nodeInfo.head();
  }

  public SequenceIterator iterate() throws XPathException {
    return nodeInfo.iterate();
  }

  public Location saveLocation() {
    return nodeInfo.saveLocation();
  }

  public CharSequence getStringValueCS() {
    return nodeInfo.getStringValueCS();
  }

  public TreeInfo getTreeInfo() {
    return nodeInfo.getTreeInfo();
  }

  public Configuration getConfiguration() {
    return nodeInfo.getConfiguration();
  }

  public int getNodeKind() {
    return nodeInfo.getNodeKind();
  }

  public boolean isSameNodeInfo(NodeInfo other) {
    return nodeInfo.isSameNodeInfo(other);
  }

  public boolean equals(Object other) {
    return nodeInfo.equals(other);
  }

  public int hashCode() {
    return nodeInfo.hashCode();
  }

  public String getSystemId() {
    return nodeInfo.getSystemId();
  }

  public String getPublicId() {
    return nodeInfo.getPublicId();
  }

  public String getBaseURI() {
    return nodeInfo.getBaseURI();
  }

  public int getLineNumber() {
    return nodeInfo.getLineNumber();
  }

  public int getColumnNumber() {
    return nodeInfo.getColumnNumber();
  }

  public int compareOrder(NodeInfo other) {
    return nodeInfo.compareOrder(other);
  }

  public int comparePosition(NodeInfo other) {
    return nodeInfo.comparePosition(other);
  }

  public String getStringValue() {
    return nodeInfo.getStringValue();
  }

  public boolean hasFingerprint() {
    return nodeInfo.hasFingerprint();
  }

  public int getFingerprint() {
    return nodeInfo.getFingerprint();
  }

  public String getLocalPart() {
    return nodeInfo.getLocalPart();
  }

  public String getURI() {
    return nodeInfo.getURI();
  }

  public String getDisplayName() {
    return nodeInfo.getDisplayName();
  }

  public String getPrefix() {
    return nodeInfo.getPrefix();
  }

  public SchemaType getSchemaType() {
    return nodeInfo.getSchemaType();
  }

  public AtomicSequence atomize() throws XPathException {
    return nodeInfo.atomize();
  }

  public NodeInfo getParent() {
    return nodeInfo.getParent();
  }

  public AxisIterator iterateAxis(byte axisNumber) {
    return nodeInfo.iterateAxis(axisNumber);
  }

  public AxisIterator iterateAxis(byte axisNumber, NodeTest nodeTest) {
    return nodeInfo.iterateAxis(axisNumber, nodeTest);
  }

  public String getAttributeValue(String uri, String local) {
    return nodeInfo.getAttributeValue(uri, local);
  }

  public NodeInfo getRoot() {
    return nodeInfo.getRoot();
  }

  public boolean hasChildNodes() {
    return nodeInfo.hasChildNodes();
  }

  public void generateId(FastStringBuffer buffer) {
    nodeInfo.generateId(buffer);
  }

  public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
    nodeInfo.copy(out, copyOptions, locationId);
  }

  public NamespaceBinding[] getDeclaredNamespaces(NamespaceBinding[] buffer) {
    return nodeInfo.getDeclaredNamespaces(buffer);
  }

  public boolean isId() {
    return nodeInfo.isId();
  }

  public boolean isIdref() {
    return nodeInfo.isIdref();
  }

  public boolean isNilled() {
    return nodeInfo.isNilled();
  }

  public boolean isStreamed() {
    return nodeInfo.isStreamed();
  }

}