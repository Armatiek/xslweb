package nl.armatiek.xslweb.saxon.functions.diff;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.fau.cs.osr.hddiff.HDDiff;
import de.fau.cs.osr.hddiff.HDDiffOptions;
import de.fau.cs.osr.hddiff.editscript.EditOp;
import de.fau.cs.osr.hddiff.editscript.EditScriptOptimizer;
import de.fau.cs.osr.hddiff.tree.DiffNode;
import de.fau.cs.osr.hddiff.tree.NodeEligibilityTesterInterface;
import de.fau.cs.osr.hddiff.utils.WordSubstringJudge;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;
import nl.armatiek.xslweb.saxon.functions.diff.hddiff.DiffUtils;
import nl.armatiek.xslweb.saxon.functions.diff.hddiff.EditScriptManager;
import nl.armatiek.xslweb.saxon.functions.diff.hddiff.NodeMetrics;
import nl.armatiek.xslweb.saxon.functions.diff.hddiff.NodeToDiffNodeConverter;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class DiffXML extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_DIFF, "diff-xml");

  private static final String WHITESPACE_STRIPPING_POLICY = "whitespace-stripping-policy";
  private static final String MIN_SUBTREE_WEIGHT          = "min-subtree-weight";
  private static final String ENABLE_TNSM                 = "enable-tnsm";
  private static final String RECORD_SPLIT_OPS            = "record-split-ops";
  private static final String MIN_STRING_LENGTH           = "min-string-length";
  private static final String MIN_WORD_COUNT              = "min-word-count";
  private static final String ONLY_SPLIT_NODES            = "only-split-nodes";
  private static final String ADD_SPLIT_IDS               = "add-split-ids";
  
  public enum WhitespaceStrippingPolicy {
    ALL,
    IGNORABLE,
    NONE 
  }
  
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
    return new SequenceType[] { SequenceType.SINGLE_NODE, SequenceType.SINGLE_NODE, SequenceType.SINGLE_NODE };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.SINGLE_NODE;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new DiffCall();
  }

  private static class DiffCall extends ExtensionFunctionCall {
    
    private void setDeltaXMLAttr(Element elem, String name, String value) {
      elem.setAttributeNS(Definitions.NAMESPACEURI_DELTAXML, Definitions.PREFIX_DELTAXML + ":" + name, value);
    }
    
    @Override
    public ZeroOrOne<NodeInfo> call(XPathContext context, Sequence[] arguments) throws XPathException {
      try {
        NodeInfo nodeInfo1 = (NodeInfo) arguments[0].head();
        NodeInfo nodeInfo2 = (NodeInfo) arguments[1].head();
        
        String whitespaceStrippingPolicy = "all"; // "ignorable", "none"
        int minSubtreeWeight = 12;
        boolean enableTnsm = true;
        boolean recordSplitOps = false;
        int minStrLength = 8;
        int minWordCount = 3;
        boolean onlySplitNodes = false;
        boolean addSplitIds = false;
        
        if (arguments.length > 2) {
          NodeInfo optionsElem = unwrapNodeInfo((NodeInfo) arguments[2].head());
          AxisIterator iter = optionsElem.iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT);
          NodeInfo optionElem;
          while ((optionElem = iter.next()) != null) {
            String name = optionElem.getLocalPart();
            String value = optionElem.getAttributeValue("", "value");
            switch (name) {
            case WHITESPACE_STRIPPING_POLICY:
              whitespaceStrippingPolicy = value;
              break;
            case MIN_SUBTREE_WEIGHT:
              minSubtreeWeight = Integer.parseInt(value);
              break;         
            case ENABLE_TNSM:
              enableTnsm = value.equalsIgnoreCase("yes");
              break;                
            case RECORD_SPLIT_OPS:
              recordSplitOps = value.equalsIgnoreCase("yes");
              break;           
            case MIN_STRING_LENGTH:
              minStrLength = Integer.parseInt(value);
              break;          
            case MIN_WORD_COUNT:
              minWordCount = Integer.parseInt(value);
              break;             
            case ONLY_SPLIT_NODES:
              onlySplitNodes = value.equalsIgnoreCase("yes");
              break;           
            case ADD_SPLIT_IDS:
              addSplitIds = value.equalsIgnoreCase("yes");
              break;
            }
          }
        }
        
        HDDiffOptions options = new HDDiffOptions();
        options.setNodeMetrics(new NodeMetrics());
        options.setMinSubtreeWeight(minSubtreeWeight);
        options.setEnableTnsm(enableTnsm);
        options.setAddSplitIds(addSplitIds);
        options.setOnlySplitNodes(onlySplitNodes);
        options.setRecordSplitOps(recordSplitOps);
        options.setTnsmSubstringJudge(new WordSubstringJudge(minStrLength, minWordCount));
        options.setTnsmEligibilityTester(new NodeEligibilityTesterInterface() {
          @Override
          public boolean isEligible(DiffNode paramDiffNode) {
            return paramDiffNode.isTextLeaf();
          }
        });
        
        if (!whitespaceStrippingPolicy.equals("all") && !whitespaceStrippingPolicy.equals("ignorable") && !whitespaceStrippingPolicy.equals("none"))
          throw new XPathException("Whitespace stripping policy \"" + whitespaceStrippingPolicy + "\" not supported");
      
        WhitespaceStrippingPolicy ws = WhitespaceStrippingPolicy.valueOf(whitespaceStrippingPolicy.toUpperCase());
        
        Document doc1 = DiffUtils.createDocumentFromNodeInfo(nodeInfo1, ws);
        Document doc2 = DiffUtils.createDocumentFromNodeInfo(nodeInfo2, ws);
        
        DiffNode node1 = NodeToDiffNodeConverter.preprocess(doc1.getDocumentElement());
        DiffNode node2 = NodeToDiffNodeConverter.preprocess(doc2.getDocumentElement());
        
        List<EditOp> ops = HDDiff.editScript(node1, node2, options);
        EditScriptOptimizer.optimize(ops);
        EditScriptManager esm = new EditScriptManager(ops, doc1);
        esm.apply();
        
        Element root = doc1.getDocumentElement();
        setDeltaXMLAttr(root, "version", "2.0");
        setDeltaXMLAttr(root, "content-type", "full-context");
          
        NodeInfo result = new DocumentWrapper(doc1, "", context.getConfiguration()).getRootNode();
        
        return new ZeroOrOne<NodeInfo>(result);
        
      } catch (Exception e) {
        throw new XPathException("Error differencing nodes", e);
      }
    }
  }

}