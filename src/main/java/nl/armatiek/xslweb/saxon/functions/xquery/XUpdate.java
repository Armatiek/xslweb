package nl.armatiek.xslweb.saxon.functions.xquery;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import ch.ethz.mxquery.contextConfig.CompilerOptions;
import ch.ethz.mxquery.contextConfig.Context;
import ch.ethz.mxquery.datamodel.types.Type;
import ch.ethz.mxquery.datamodel.xdm.Token;
import ch.ethz.mxquery.exceptions.MXQueryException;
import ch.ethz.mxquery.exceptions.QueryLocation;
import ch.ethz.mxquery.model.CFException;
import ch.ethz.mxquery.model.XDMIterator;
import ch.ethz.mxquery.model.updatePrimitives.UpdateableStore;
import ch.ethz.mxquery.query.PreparedStatement;
import ch.ethz.mxquery.query.XQCompiler;
import ch.ethz.mxquery.query.impl.CompilerImpl;
import ch.ethz.mxquery.xdmio.XDMInputFactory;
import ch.ethz.mxquery.xdmio.XDMSerializer;
import ch.ethz.mxquery.xdmio.XDMSerializerSettings;
import ch.ethz.mxquery.xdmio.XMLSource;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.Sender;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyDocumentImpl;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class XUpdate extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_XQUERY, "xupdate");

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
    return 2;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { SequenceType.SINGLE_NODE, SequenceType.SINGLE_STRING };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.SINGLE_NODE;
  }
  
  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new SerializeCall();
  }

  private static class SerializeCall extends ExtensionFunctionCall {

    @Override
    public NodeInfo call(XPathContext context, Sequence[] arguments) throws XPathException {
      NodeInfo input = (NodeInfo) arguments[0].head();
      String query = ((StringValue) arguments[1].head()).getStringValue();
      
      Context ctx = new Context();
      CompilerOptions co = new CompilerOptions();
      co.setXquery11(true);
      co.setUpdate(true);
      ctx.getStores().setUseUpdateStores(true);
      ctx.getStores().setSerializeStores(false);
      XQCompiler compiler = new CompilerImpl();
      PreparedStatement statement;
      try {
        statement = compiler.compile(ctx, query, co);
        
        Node node = NodeOverNodeInfo.wrap(input);
        XMLSource xmlSource = XDMInputFactory.createDOMInput(ctx, node, QueryLocation.OUTSIDE_QUERY_LOC);
        UpdateableStore store = ctx.getStores().createUpdateableStore("output", xmlSource, true, false);
        statement.setContextItem(store.getIterator(ctx));
        
        XDMIterator it = statement.evaluate();
        Token tok = null;
        try {
          tok = it.next();
        } catch (CFException cfe) {
          if (cfe.isEarlyReturn()) {
            it = cfe.getReturnValue();
            tok = it.next();
          } else {
            throw cfe;
          }
        }
        while (tok.getEventType() != Type.END_SEQUENCE){
          tok = it.next();
        }
        
        statement.applyPUL();
        statement.serializeStores(false);
        
        XDMSerializerSettings ser = new XDMSerializerSettings();
        ser.setOmitXMLDeclaration(true);
        XDMSerializer ip = new XDMSerializer(ser);
        String resultXML = ip.eventsToXML(store.getIterator(ctx));
       
        statement.close();
        
        ctx.getStores().freeRessources();
        Builder b = TreeModel.TINY_TREE.makeBuilder(context.getController().makePipelineConfiguration());
        Receiver s = b;
        ParseOptions options = new ParseOptions();
        options.setStripSpace(Whitespace.NONE);
        options.setErrorListener(context.getConfiguration().getErrorListener());
        s.setPipelineConfiguration(b.getPipelineConfiguration()); 
        StringReader sr = new StringReader(resultXML);
        InputSource is = new InputSource(sr);                        
        Source source = new SAXSource(is);
        Sender.send(source, s, options);
        TinyDocumentImpl rootNode = (TinyDocumentImpl) b.getCurrentRoot();
        rootNode.setSystemId(null);
        return rootNode;
      } catch (MXQueryException err) {
        throw new XPathException("Error executing XUpdate query: " + MXQueryException.getErrorPosition(query, err.getLocation()), err);
      } catch (IOException ioe) {
        throw new XPathException(ioe);
      }
    }
  }
  
}