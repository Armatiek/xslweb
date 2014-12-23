package nl.armatiek.xslweb.saxon.functions.log;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;

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
    return 2;
  }

  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.OPTIONAL_STRING };
  }

  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.SINGLE_BOOLEAN;
  }

  public ExtensionFunctionCall makeCallExpression() {
    return new LogCall();
  }
  
  private static class LogCall extends ExtensionFunctionCall {

    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
      try {
        String level = ((StringValue) arguments[0].head()).getStringValue();
        String message = "";
        Sequence seq = arguments[1];
        SequenceIterator iter = seq.iterate();           
        Item item;
        if ((item = iter.next()) != null) {
          message = ((StringValue) item).getStringValue();
        }        
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