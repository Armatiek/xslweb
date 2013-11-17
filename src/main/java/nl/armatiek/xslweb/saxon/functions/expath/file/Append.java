package nl.armatiek.xslweb.saxon.functions.expath.file;

import java.io.File;
import java.io.OutputStream;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0003Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0004Exception;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Append extends ExtensionFunctionDefinition {

  private static final long serialVersionUID = 1L;
  
  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_EXPATH_FILE, "append");

  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 2;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 3;
  }

  @Override
  public SequenceType[] getArgumentTypes() {    
    return new SequenceType[] { 
        SequenceType.SINGLE_STRING, 
        SequenceType.makeSequenceType(AnyItemType.getInstance(), StaticProperty.ALLOWS_ZERO_OR_MORE),
        SequenceType.SINGLE_NODE };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {    
    return SequenceType.EMPTY_SEQUENCE;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new AppendCall();
  }
  
  private static class AppendCall extends FileExtensionFunctionCall {
        
    private static final long serialVersionUID = 1L;
    
    protected void serialize(NodeInfo nodeInfo, OutputStream os, Properties outputProperties) throws Exception {
      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer transformer = factory.newTransformer();
      if (outputProperties != null) {
        transformer.setOutputProperties(outputProperties);
      }
      transformer.transform(nodeInfo, new StreamResult(os));      
    }
    
    @SuppressWarnings("rawtypes")
    public SequenceIterator<Item> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {      
      try {                        
        File file = getFile(((StringValue) arguments[0].next()).getStringValue());
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
          throw new FILE0003Exception(parentFile);
        }     
        if (file.isDirectory()) {
          throw new FILE0004Exception(file);
        }                
        Properties ouputProperties = new Properties();
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
            ouputProperties.put(child.getLocalName(), ((Element)child).getAttribute("value"));                                     
          }                    
        }
        
          /*
        <output:serialization-parameters xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization">
        <output:omit-xml-declaration value="yes"/>
      </output:serialization-parameters>
      */
        
        
        // Element rootElem = nodeOverNodeInfo.getOwnerDocument().getDocumentElement();
        
        
        
        OutputStream os = FileUtils.openOutputStream(parentFile, true);
        try {
          SequenceIterator itemsArg = arguments[1];
          Item item;                
          while ((item = itemsArg.next()) != null) {            
            if (item instanceof NodeInfo) {
              serialize((NodeInfo) item, os, null);
            } else {
              IOUtils.write(item.toString(), os);                            
            }                                        
          }
        } finally {
          os.close();
        }                       
        return EmptyIterator.emptyIterator();
      } catch (Exception e) {
        throw new XPathException(e);
      }
    } 
  }
}