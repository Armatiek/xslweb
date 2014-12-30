package nl.armatiek.xslweb.saxon.functions.log;

import java.io.StringWriter;
import java.util.Properties;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class Log extends ExtensionFunctionDefinition {

  private static final Logger log = LoggerFactory.getLogger(Log.class);
  
  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_LOG, "log");

  public StructuredQName getFunctionQName() {
    return qName;
  }

  public int getMinimumNumberOfArguments() {
    return 2;
  }

  public int getMaximumNumberOfArguments() {
    return 3;
  }

  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { 
        SequenceType.SINGLE_STRING, 
        SequenceType.makeSequenceType(AnyItemType.getInstance(), StaticProperty.ALLOWS_ZERO_OR_MORE),
        SequenceType.OPTIONAL_NODE };
  }

  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.SINGLE_BOOLEAN;
  }
  
  public boolean hasSideEffects() {
    return true;
  }

  public ExtensionFunctionCall makeCallExpression() {
    return new LogCall();
  }
  
  private static class LogCall extends ExtensionFunctionCall {

    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
      try {
        String level = ((StringValue) arguments[0].head()).getStringValue();
        Properties props = null;
        if (arguments.length == 3) {
          props = getOutputProperties((NodeInfo) arguments[2].head());
        }
        StringWriter sw = new StringWriter();
        serialize(arguments[1], sw, props);  
        String message = sw.toString();
        if (level.equals("ERROR")) {
          log.error(message);
        } else if (level.equals("WARN")) {
          log.warn(message);
        } else if (level.equals("INFO")) {
          log.info(message);
        } else if (level.equals("DEBUG")) {
          log.debug(message);          
        } else {
          throw new XPathException(String.format("Level %s not supported", level));          
        }          
        return BooleanValue.TRUE;
      } catch (Exception e) {
        throw new XPathException("Could not log message", e);
      }
    }
  }
}