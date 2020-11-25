package nl.armatiek.xslweb.saxon.functions.util;

import java.io.StringReader;

import javax.xml.transform.sax.SAXSource;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;

import net.sf.saxon.event.Sender;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;

/**
 * XPath extension function class for
 * 
 * @author Maarten Kroon
 */
public class ParseHtml extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_UTIL, "parse-html");

  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 1;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 1;
  }

  @Override
  public SequenceType[] getArgumentTypes() {    
    return new SequenceType[] { SequenceType.SINGLE_STRING };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.makeSequenceType(NodeKindTest.DOCUMENT, StaticProperty.EXACTLY_ONE);    
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new ParseHtmlCall();
  }
  
  private static class ParseHtmlCall extends ExtensionFunctionCall {
    
    @Override
    public NodeInfo call(XPathContext context, Sequence[] arguments) throws XPathException {
      TinyBuilder builder = new TinyBuilder(context.getConfiguration().makePipelineConfiguration());
      builder.setStatistics(context.getConfiguration().getTreeStatistics().SOURCE_DOCUMENT_STATISTICS);
      String html = ((StringValue) arguments[0].head()).getStringValue();
      InputSource inputSource = new InputSource(new StringReader(html));
      SAXSource source = new SAXSource(inputSource);
      ParseOptions parseOptions = new ParseOptions();
      parseOptions.setDTDValidationMode(Validation.STRIP);
      parseOptions.setSchemaValidationMode(Validation.STRIP);
      parseOptions.setSpaceStrippingRule(NoElementsSpaceStrippingRule.getInstance());
      parseOptions.setLineNumbering(false);
      parseOptions.setXMLReader(new Parser());
      Sender.send(source, builder, parseOptions);
      builder.close();
      return builder.getCurrentRoot(); 
    }
    
  }
}