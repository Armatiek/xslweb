package nl.armatiek.xslweb.saxon.functions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.xpath.XPathEvaluator;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.servlet.TemplatesCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Transform extends ExtensionFunctionDefinition {

  private static final long serialVersionUID = 1L;
  
  // private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACE_XSLWEB_FUNCTIONS, "transform");

  @Override
  public StructuredQName getFunctionQName() {
    return null;
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
    return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.SINGLE_NODE, SequenceType.NODE_SEQUENCE };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {    
    return SequenceType.OPTIONAL_NODE;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new TransformCall();
  }
  
  private static class TransformCall extends ExtensionFunctionCall implements ErrorListener {
    
    private static final Logger logger = LoggerFactory.getLogger(TransformCall.class);

    private static final long serialVersionUID = 1L;

    public SequenceIterator<NodeInfo> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {
      String xslPath = "";
      try {                
        xslPath = ((StringValue) arguments[0].next()).getStringValue();
        
        NodeInfo nodeInfo = (NodeInfo) arguments[1].next();
        // NodeOverNodeInfo nodeOverNodeInfo = NodeOverNodeInfo.wrap(nodeInfo);
        // Element rootElem = nodeOverNodeInfo.getOwnerDocument().getDocumentElement();
        
        Templates templates = TemplatesCache.tryTemplatesCache(xslPath, this, context.getConfiguration());
        Controller controller = (Controller) templates.newTransformer();
        // controller.setParameter(expandedName, value);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream(32*1024);
        controller.transform(nodeInfo, new StreamResult(baos));
               
        StreamSource source = new StreamSource(new ByteArrayInputStream(baos.toByteArray()));
        XPathEvaluator evaluator = new XPathEvaluator(context.getConfiguration());
        
        return SingletonIterator.makeIterator(evaluator.setSource(source));
        
      } catch (Exception e) {
        throw new XPathException(String.format("Error executing transformation using \"%s\"", xslPath), e);
      }
    }
    
    @Override
    public void error(TransformerException e) throws TransformerException {
      logger.error(e.getMessage(), e);      
      throw e;      
    }

    @Override
    public void fatalError(TransformerException e) throws TransformerException {
      logger.error(e.getMessage(), e);      
      throw e;      
    }

    @Override
    public void warning(TransformerException e) throws TransformerException {
      logger.warn(e.getMessage());      
    }
  }

}