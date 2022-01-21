/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.armatiek.xslweb.saxon.functions.exec;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.output.TeeOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
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
    return 8;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    /* $command-line as xs:string, $args as xs:string*, $time-out as xs:integer?, $async as xs:boolean?, $work-dir as xs:string?, 
     * $handle-quoting as xs:boolean?, $log-stdout-and-stderr as xs:boolean?, $environment as map(*) 
     */
    return new SequenceType[] { 
        SequenceType.SINGLE_STRING,
        SequenceType.makeSequenceType(BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_MORE),
        SequenceType.OPTIONAL_INTEGER,
        SequenceType.OPTIONAL_BOOLEAN,
        SequenceType.OPTIONAL_STRING,
        SequenceType.OPTIONAL_BOOLEAN,
        SequenceType.OPTIONAL_BOOLEAN,
        MapType.OPTIONAL_MAP_ITEM};
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
    
    private Map<String, String> trieMapToMap(MapItem mapItem) throws XPathException {
      if (mapItem == null) {
        return null;
      }
      HashMap<String, String> params = new HashMap<String, String>();
      Iterator<KeyValuePair> iter =  mapItem.keyValuePairs().iterator();
      while (iter.hasNext()) {
        KeyValuePair pair = iter.next();
        params.put(pair.key.getStringValue(), (pair.value != null) ? pair.value.getStringValue() : "");
      }
      return params;
    }
    
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {                                  
      final CommandLine cmdLine = new CommandLine(((StringValue) arguments[0].head()).getStringValue());      
      
      if (arguments.length > 1) {
        boolean handleQuoting = true;
        if (arguments.length > 5 && arguments[5].head() != null) {
          handleQuoting = ((BooleanValue) arguments[5].head()).getBooleanValue();
        }
        SequenceIterator args = arguments[1].iterate();      
        Item arg;
        while ((arg = args.next()) != null) {
          cmdLine.addArgument(((StringValue) arg).getStringValue(), handleQuoting);
        }
      }
      
      Executor executor = new DefaultExecutor() {  
        @Override
        public boolean isFailure(int exitValue) {
          return false;
        }
      };
      
      // Log stdin and stdout:
      if (arguments.length > 6 && arguments[6].head() != null) {
        boolean logOutAndErr = ((BooleanValue) arguments[6].head()).getBooleanValue();
        if (logOutAndErr) {
          PumpStreamHandler streamHandler = new PumpStreamHandler(
              new TeeOutputStream(
                  System.out, 
                  new Slf4JOutputStream(logger, Level.INFO, "  [exec-external] - ")), 
              new TeeOutputStream(
                  System.err,
                  new Slf4JOutputStream(logger, Level.ERROR, "  [exec-external] - ")));
          executor.setStreamHandler(streamHandler);    
        }
      }
      
      Item timeout;
      if (arguments.length > 2 && (timeout = arguments[2].head()) != null) {
        ExecuteWatchdog watchdog = new ExecuteWatchdog(((Int64Value) timeout).longValue());
        executor.setWatchdog(watchdog);                        
      }
      
      Item workDir;
      if (arguments.length > 4 && (workDir = arguments[4].head()) != null) {
        executor.setWorkingDirectory(new File(((StringValue) workDir).getStringValue()));                        
      }
      
      Map<String, String> environment = null;
      if (arguments.length > 7 && arguments[7].head() != null) {
        environment = trieMapToMap((MapItem) arguments[7].head());  
      }
      
      logger.info(String.format("Executing external process %s ...", cmdLine.toString()));
      try {
        Item async;
        if (arguments.length > 3 && (async = arguments[3].head()) != null && ((BooleanValue) async).getBooleanValue()) {                    
          executor.execute(cmdLine, environment, new ExecuteResultHandler() {
            @Override
            public void onProcessComplete(int exitValue) {
              logger.info(String.format("External process %s completed with exit value %d", cmdLine.toString(), exitValue));
            }

            @Override
            public void onProcessFailed(ExecuteException e) {
              logger.error(String.format("External process %s completed with exit value %d", cmdLine.toString(), e.getExitValue()));
            }           
          });                
          return EmptySequence.getInstance();          
        } else {
          try {
            executor.execute(cmdLine, environment);
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