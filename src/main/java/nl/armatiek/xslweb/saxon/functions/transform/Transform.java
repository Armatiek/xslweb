package nl.armatiek.xslweb.saxon.functions.transform;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.ErrorListener;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ErrorReporterToListener;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.saxon.debug.DebugUtils;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;
import nl.armatiek.xslweb.saxon.utils.SaxonUtils;
import nl.armatiek.xslweb.utils.XSLWebUtils;

/**
 * XPath extension function class for
 * 
 * @author Maarten Kroon
 */
public class Transform extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_TRANSFORM, "transform");

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
    return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.NODE_SEQUENCE, SequenceType.makeSequenceType(MapType.ANY_MAP_TYPE, StaticProperty.ALLOWS_ZERO_OR_ONE) };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.makeSequenceType(NodeKindTest.DOCUMENT, StaticProperty.EXACTLY_ONE);    
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new TransformCall();
  }
  
  private static class TransformCall extends ExtensionFunctionCall {
    
    private void checkSequenceIsUntyped(Sequence value) throws XPathException {
      SequenceIterator iter = value.iterate();
      Item item;
      while ((item = iter.next()) != null) {
        if (item instanceof NodeInfo && ((NodeInfo) item).getTreeInfo().isTyped()) {
          throw new XPathException("Schema-validated nodes cannot be passed to fn:transform() when it runs under a different Saxon Configuration", "FOXT0002");
        }
      }
    }
    
    private void processParams(MapItem suppliedParams, Map<QName, XdmValue> checkedParams, boolean allowTypedNodes) throws XPathException {
      AtomicIterator paramIterator = suppliedParams.keys();
      while (true) {
        AtomicValue param = paramIterator.next();
        if (param != null) {
          if (!(param instanceof QNameValue)) {
            throw new XPathException("The names of parameters must be supplied as QNames", "FOXT0002");
          }
          QName paramName = new QName(((QNameValue) param).getStructuredQName());
          Sequence value = suppliedParams.get(param);
          if (!allowTypedNodes) {
            checkSequenceIsUntyped(value);
          }
          XdmValue paramVal = XdmValue.wrap(value);
          checkedParams.put(paramName, paramVal);
        } else {
          break;
        }
      }
    }
    
    @Override
    public NodeInfo call(XPathContext context, Sequence[] arguments) throws XPathException {
      try {
        String xslPath = ((StringValue) arguments[0].head()).getStringValue(); 
        WebApp webApp = this.getWebApp(context);
        ErrorListener errorListener = ((ErrorReporterToListener) context.getErrorReporter()).getErrorListener();
        xslPath = new File(webApp.getHomeDir(), "xsl/" + xslPath).getAbsolutePath();
        XsltExecutable exec = webApp.tryXsltExecutableCache(xslPath, errorListener);
        Xslt30Transformer transformer = exec.load30();
        SaxonUtils.setMessageEmitter(transformer.getUnderlyingController(), webApp.getConfiguration(), errorListener);
        transformer.setErrorListener(errorListener);
        DebugUtils.setDebugTraceListener(webApp, this.getRequest(context), transformer);
        HttpServletRequest req = getRequest(context);
        HttpServletResponse resp = getResponse(context);
        Map<QName, XdmValue> stylesheetParams = XSLWebUtils.getStylesheetParameters(webApp, req, resp, webApp.getHomeDir());
        MapItem params;
        if (arguments.length > 2 && (params = (MapItem) arguments[2].head()) != null) {
          MapItem stylesheetParamsMap = (MapItem) params.get(new StringValue("stylesheet-params"));
          if (stylesheetParamsMap != null) {
            processParams(stylesheetParamsMap, stylesheetParams, true);
            transformer.setStylesheetParameters(stylesheetParams);
          }
        }
        XdmDestination dest = new XdmDestination();
        ArrayList<XdmNode> nodeList = new ArrayList<XdmNode>();
        SequenceIterator nodeIter = arguments[1].iterate();
        Item item;
        boolean first = true;
        while ((item = nodeIter.next()) != null) {
          XdmNode node = new XdmNode((NodeInfo) item);
          if (first) {
            transformer.setGlobalContextItem(node);
            first = false;
          }
          nodeList.add(node);
        }
        transformer.applyTemplates(new XdmValue(nodeList), dest);
        return dest.getXdmNode().getUnderlyingNode();
      } catch (XPathException xpe) {
        throw xpe;
      } catch (Exception e) {
        throw new XPathException(e.getMessage(), e);
      }
    }
  }
}