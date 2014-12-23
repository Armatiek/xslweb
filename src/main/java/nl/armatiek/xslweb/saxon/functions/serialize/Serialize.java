package nl.armatiek.xslweb.saxon.functions.serialize;

import java.io.StringWriter;
import java.util.Properties;

import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class Serialize extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_SERIALIZE, "serialize");

  public StructuredQName getFunctionQName() {
    return qName;
  }

  public int getMinimumNumberOfArguments() {
    return 1;
  }

  public int getMaximumNumberOfArguments() {
    return 2;
  }

  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { SequenceType.ANY_SEQUENCE, SequenceType.OPTIONAL_NODE };
  }

  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.SINGLE_STRING;
  }
  
  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new SerializeCall();
  }

  private static class SerializeCall extends ExtensionFunctionCall {
    
    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {                            
      Properties props = new Properties();
      if (arguments.length == 2) {
        NodeInfo paramsDoc = (NodeInfo) arguments[1].head();
        NodeInfo paramsElem = paramsDoc.iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT).next(); 
        AxisIterator iter = paramsElem.iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT);
        NodeInfo paramElem;
        while ((paramElem = iter.next()) != null) {
          props.put(paramElem.getLocalPart(), paramElem.getAttributeValue("", "value"));
        }  
      }
      Configuration config = context.getConfiguration();
      StringWriter sw = new StringWriter();
      Item item;
      Sequence seq = arguments[0];
      SequenceIterator iter = seq.iterate();
      while ((item = iter.next()) != null) {
        if (item instanceof NodeInfo) {
          NodeInfo nodeInfo = (NodeInfo) item;
          Receiver serializer = config.getSerializerFactory().getReceiver(new StreamResult(sw), 
              config.makePipelineConfiguration(), props);
          nodeInfo.copy(serializer, NodeInfo.ALL_NAMESPACES, 0);
        } else {                             
          sw.append(item.getStringValue());
        }
      }    
      return StringValue.makeStringValue(sw.toString());              
    }
  }
  
}