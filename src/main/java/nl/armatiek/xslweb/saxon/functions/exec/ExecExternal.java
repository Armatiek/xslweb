package nl.armatiek.xslweb.saxon.functions.exec;

import java.io.File;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class ExecExternal extends ExtensionFunctionDefinition {
  
  private static final Logger logger = LoggerFactory.getLogger(ExecExternal.class);

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_EXEC, "exec-external");

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
    return 5;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    // commandline, (args), watchdog, async
    return new SequenceType[] { 
        SequenceType.SINGLE_STRING,
        SequenceType.makeSequenceType(BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_MORE),
        SequenceType.OPTIONAL_INTEGER,
        SequenceType.OPTIONAL_BOOLEAN,
        SequenceType.OPTIONAL_STRING};
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.OPTIONAL_INTEGER;
  }
  
  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new ExecExternalCall();
  }

  private static class ExecExternalCall extends ExtensionFunctionCall {
    
    @Override
    public Sequence<?> call(XPathContext context, Sequence[] arguments) throws XPathException {                                  
      final CommandLine cmdLine = new CommandLine(((StringValue) arguments[0].head()).getStringValue());      
      if (arguments.length > 1) {
        SequenceIterator args = arguments[1].iterate();      
        Item arg;
        while ((arg = args.next()) != null) {
          cmdLine.addArgument(((StringValue) arg).getStringValue());
        }
      }
      Executor executor = new DefaultExecutor();
      executor.setExitValue(0);
      Item timeout;
      if (arguments.length > 2 && (timeout = arguments[2].head()) != null) {
        ExecuteWatchdog watchdog = new ExecuteWatchdog(((Int64Value) timeout).longValue());
        executor.setWatchdog(watchdog);                        
      }
      Item workDir;
      if (arguments.length > 4 && (workDir = arguments[4].head()) != null) {
        executor.setWorkingDirectory(new File(((StringValue) workDir).getStringValue()));                        
      }
      try {
        Item async;
        if (arguments.length > 3 && (async = arguments[3].head()) != null && ((BooleanValue) async).getBooleanValue()) {                    
          executor.execute(cmdLine, new ExecuteResultHandler() {
            @Override
            public void onProcessComplete(int exitValue) {
              logger.debug("External process \"" + cmdLine.toString() + "\" completed with exit value \"" + exitValue + "\"");
            }

            @Override
            public void onProcessFailed(ExecuteException e) {
              logger.error("External process \"" + cmdLine.toString() + "\" failed with exit value \"" + e.getExitValue() + "\"", e);
            }           
          });                
          return EmptySequence.getInstance();          
        } else {
          try {
            executor.execute(cmdLine);
            return Int64Value.makeIntegerValue(0);
          } catch (ExecuteException e) {
            return Int64Value.makeIntegerValue(e.getExitValue());
          }
        }
      } catch (Exception e) {
        throw new XPathException("Error executing external process", e);
      }
    }
  }
  
}