package nl.armatiek.xslweb.saxon.functions.expath.file;

import java.io.File;
import java.io.OutputStream;
import java.util.Properties;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.ExpectedFileException;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0003Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0004Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE9999Exception;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class WriteCall extends FileExtensionFunctionCall {

  private static final long serialVersionUID = 1L;
  
  private boolean append;
  
  public WriteCall(boolean append) {
    this.append = append;
  }

  protected void serialize(NodeInfo nodeInfo, OutputStream os, Properties outputProperties) throws Exception {
    TransformerFactory factory = TransformerFactory.newInstance();
    Transformer transformer = factory.newTransformer();
    if (outputProperties != null) {
      transformer.setOutputProperties(outputProperties);
    }
    transformer.transform(nodeInfo, new StreamResult(os));
  }

  @SuppressWarnings("rawtypes")
  public SequenceIterator<BooleanValue> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {
    try {
      File file = getFile(((StringValue) arguments[0].next()).getStringValue());
      File parentFile = file.getParentFile();
      if (!parentFile.exists()) {
        throw new FILE0003Exception(parentFile);
      }
      if (file.isDirectory()) {
        throw new FILE0004Exception(file);
      }
      Properties outputProperties = new Properties();
      Element serParamElem;
      if (arguments.length > 2) {
        NodeInfo nodeInfo = (NodeInfo) arguments[2].next();
        NodeOverNodeInfo nodeOverNodeInfo = NodeOverNodeInfo.wrap(nodeInfo);
        serParamElem = nodeOverNodeInfo.getOwnerDocument().getDocumentElement();
        Node child = serParamElem.getFirstChild();
        while ((child = child.getNextSibling()) != null) {
          if (child.getNodeType() != Node.ELEMENT_NODE) {
            continue;
          }
          outputProperties.put(child.getLocalName(), ((Element) child).getAttribute("value"));
        }
      }

      /*
       * <output:serialization-parameters
       * xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization">
       * <output:omit-xml-declaration value="yes"/>
       * </output:serialization-parameters>
       */

      OutputStream os = FileUtils.openOutputStream(parentFile, append);
      try {
        SequenceIterator itemsArg = arguments[1];
        Item item;
        while ((item = itemsArg.next()) != null) {
          if (item instanceof NodeInfo) {
            serialize((NodeInfo) item, os, outputProperties);
          } else {
            IOUtils.write(item.toString(), os);
          }
        }
      } finally {
        os.close();
      }
      return SingletonIterator.makeIterator(BooleanValue.TRUE);
    } catch (ExpectedFileException e) {
      throw e;
    } catch (Exception e) {
      throw new FILE9999Exception(e);
    }
  }
}