package nl.armatiek.xslweb.saxon.errrorlistener;

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.event.SequenceWriter;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.MessageListener;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

/**
 * This class implements a Receiver that can receive xsl:message output and send
 * it to a user-supplied MessageListener.
 */

public class MessageListenerProxy extends SequenceWriter {

  private MessageListener listener;
  private boolean terminate;
  private Location locationId;
  private String errorCode;

  public MessageListenerProxy(MessageListener listener, PipelineConfiguration pipe) {
    super(pipe);
    // See bug 2104. We use the Linked Tree model because the TinyTree can use
    // excessive memory. This
    // is because the initial size allocation is based on the size of source
    // documents, which might be large;
    // also because we store several messages in a single TinyTree; and because
    // we fail to condense the tree.
    setTreeModel(TreeModel.LINKED_TREE);
    this.listener = listener;
  }

  /**
   * Get the wrapped MessageListener
   *
   * @return the wrapped MessageListener
   */

  public MessageListener getMessageListener() {
    return listener;
  }

  /**
   * Start of a document node.
   * 
   * @param properties
   */

  public void startDocument(int properties) throws XPathException {
    terminate = ReceiverOption.contains(properties, ReceiverOption.TERMINATE);
    locationId = null;
    errorCode = null;
    super.startDocument(properties);
  }

  /**
   * Output an element start tag.
   */

  public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
    if (this.locationId == null) {
      this.locationId = location;
    }
    super.startElement(elemName, type, attributes, namespaces, location, properties);
  }

  @Override
  public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
    if (target.equals("error-code") && errorCode == null) {
      // Suppress the error code, not used in this interface
      errorCode = data.toString();
    } else {
      super.processingInstruction(target, data, locationId, properties);
    }
  }

  /**
   * Produce text content output.
   * 
   * @param s
   *          The String to be output
   * @param locationId
   *          the location of the node in the source, or of the instruction that
   *          created it
   * @param properties
   *          bit-significant flags for extra information, e.g.
   *          disable-output-escaping @throws net.sf.saxon.trans.XPathException
   */

  public void characters(CharSequence s, Location locationId, int properties) throws XPathException {
    if (this.locationId == null) {
      this.locationId = locationId;
    }
    super.characters(s, locationId, properties);
  }

  /**
   * Append an item to the sequence, performing any necessary type-checking and
   * conversion
   */

  public void append(Item item, Location locationId, int copyNamespaces) throws XPathException {
    if (this.locationId == null) {
      this.locationId = locationId;
    }
    super.append(item, locationId, copyNamespaces);
  }

  /**
   * Abstract method to be supplied by subclasses: output one item in the
   * sequence.
   *
   * @param item
   *          the item to be written to the sequence
   */

  public void write(Item item) throws XPathException {
    Location loc;
    if (locationId == null) {
      loc = Loc.NONE;
    } else {
      loc = locationId.saveLocation();
    }
    listener.message(new XdmNode((NodeInfo) item), terminate, loc);
  }
}
