package nl.armatiek.xslweb.saxon.functions.httpclient;

import java.io.StringReader;
import java.util.ArrayList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.lib.StandardEntityResolver;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.CodedName;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.LargeAttributeMap;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SmallAttributeMap;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.om.ZeroOrMore;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Fingerprints;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.saxon.functions.httpclient.SendRequest.SendRequestCall;
import nl.armatiek.xslweb.saxon.utils.NodeInfoUtils;

public class HttpClientEntityResolver extends StandardEntityResolver {
  
  private final XPathContext context;
  private final NodeInfo requestNode;
  private final WebApp webApp;
  
  public HttpClientEntityResolver(XPathContext context, NodeInfo requestNode, WebApp webApp) {
    super(context.getConfiguration());
    // this.setConfiguration(context.getConfiguration());
    this.context = context;
    this.requestNode = requestNode;
    this.webApp = webApp;
  }

  @Override
  public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
    /* Try to find the entity in XML Catalog: */
    InputSource is = super.resolveEntity(publicId, systemId);
    if (is != null) {
      return is;
    }
    if (systemId.startsWith("http")) {
      /* The entity could not be found in the XML catalog and the request is an HTTP request. 
       * Lets execute this request with the same settings as the "containing" request:  
       */
      try {
        Fingerprints fingerprints = webApp.getFingerprints();
        NamePool namePool = context.getConfiguration().getNamePool();
        
        /* Create a new http:request element: */
        PipelineConfiguration config = context.getConfiguration().makePipelineConfiguration();
        TinyBuilder builder = (TinyBuilder) TreeModel.TINY_TREE.makeBuilder(config);
        builder.setLineNumbering(false);
        builder.open();
        builder.startDocument(0);
        
        ArrayList<AttributeInfo> attrList = new ArrayList<AttributeInfo>();
        
        /* Copy relevant attribute nodes: */
        AxisIterator attrs = requestNode.iterateAxis(AxisInfo.ATTRIBUTE);
        NodeInfo attr;
        while ((attr = attrs.next()) != null) {
          String local = attr.getLocalPart();
          if ("method".equals(local) || "href".equals(local) || "status-only".equals(local) || "override-media-type".equals(local)) {
            continue;
          }
          NodeName name;
          if (attr.hasFingerprint()) {
            name = new CodedName(attr.getFingerprint(), "", context.getConfiguration().getNamePool());
          } else {
            name = new FingerprintedQName("", "", attr.getLocalPart());
          }
          attrList.add(new AttributeInfo(name, BuiltInAtomicType.UNTYPED_ATOMIC, attr.getStringValue(), Loc.NONE, 0));  
        }
        
        /* Create new attribute nodes specific for this request */
        attrList.add(new AttributeInfo(new CodedName(fingerprints.METHOD, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, "GET", Loc.NONE, 0));
        attrList.add(new AttributeInfo(new CodedName(fingerprints.HREF, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, systemId, Loc.NONE, 0));
        attrList.add(new AttributeInfo(new CodedName(fingerprints.STATUSONLY, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, "false", Loc.NONE, 0));
        attrList.add(new AttributeInfo(new CodedName(fingerprints.OVERRIDEMEDIATYPE, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, "text/plain", Loc.NONE, 0));
        
        AttributeMap attrMap = new LargeAttributeMap(attrList);
        
        NamespaceMap nsMap = NamespaceMap.of("http", Types.EXT_NAMESPACEURI);
        builder.startElement(new CodedName(fingerprints.HTTPCLIENT_REQUEST, "http", namePool), Untyped.getInstance(), attrMap, nsMap, Loc.NONE, 0);
        
        // builder.startContent();

        /* Copy all header elements: */
        AxisIterator headers = requestNode.iterateAxis(AxisInfo.CHILD, new NameTest(Type.ELEMENT, Types.EXT_NAMESPACEURI, "header", context.getConfiguration().getNamePool()));
        NodeInfo header;
        while ((header = headers.next()) != null) {
          ArrayList<AttributeInfo> hAttrList = new ArrayList<AttributeInfo>();
          hAttrList.add(new AttributeInfo(new CodedName(fingerprints.NAME, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, header.getAttributeValue("", "name"), Loc.NONE, 0));  
          hAttrList.add(new AttributeInfo(new CodedName(fingerprints.VALUE, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, header.getAttributeValue("", "value"), Loc.NONE, 0));  
          AttributeMap hAttrMap = new SmallAttributeMap(hAttrList);
          builder.startElement(new CodedName(fingerprints.HTTPCLIENT_HEADER, "http", namePool), Untyped.getInstance(), hAttrMap, nsMap, Loc.NONE, 0);
          builder.endElement();
        }    
        builder.endElement();
        builder.endDocument();
        builder.close();
        
        /* Create new request call: */
        SendRequestCall call = new SendRequest.SendRequestCall();
        Sequence[] args = new Sequence[1];
        args[0] = NodeInfoUtils.getFirstChildElement(builder.getCurrentRoot());
        
        /* Execute request call: */
        ZeroOrMore<Item> result = call.call(context, args);
        
        /* Check HTTP status code: */
        NodeInfo responseNode = (NodeInfo) result.itemAt(0);
        String status = responseNode.getAttributeValue("", "status");
        if ("200".equals(status)) {
          InputSource inputSource = new InputSource(new StringReader(((StringValue) result.itemAt(1)).getStringValue()));
          inputSource.setSystemId(systemId);
          return inputSource;
        }
        throw new SAXException("Error executing HTTP request \"" + systemId + "\" (status: " + status + ", message: " + responseNode.getAttributeValue("", "message") + ")");
      } catch (XPathException e) {
        throw new SAXException("Error executing HTTP request \"" + systemId + "\"", e);
      }
    }
    return null;
  }
  
}
