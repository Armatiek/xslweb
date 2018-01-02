package nl.armatiek.xslweb.saxon.functions.queue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;
import nl.armatiek.xslweb.web.servlet.InternalRequest;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class AddRequest extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_QUEUE, "add-request");

  private static final Logger logger = LoggerFactory.getLogger(AddRequest.class);
  
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
    return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING, SequenceType.SINGLE_NODE };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.SINGLE_STRING;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new AddRequestToQueueCall();
  }

  private static class AddRequestToQueueCall extends ExtensionFunctionCall {
    
    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
      String queueName = ((StringValue) arguments[0].head()).getStringValue();
      String path = ((StringValue) arguments[1].head()).getStringValue();
      String extraInfo = arguments.length > 2 ? serialize((NodeInfo) arguments[2].head()) : null;
      WebApp webApp = getWebApp(context);
      ExecutorService service = webApp.getExecutorService(queueName);
      String ticket = UUID.randomUUID().toString();
      service.execute(new QueuedRequest(ticket, webApp.getPath() + "/" + path, extraInfo));
      return StringValue.makeStringValue(ticket);
    }
    
  }
  
  private static class QueuedRequest implements Runnable {

    private String ticket;
    private String path;
    private String extraInfo;
    
    public QueuedRequest(String ticket, String path, String extraInfo) {
      this.ticket = ticket;
      this.path = path;
      this.extraInfo = extraInfo;
    }
    
    @Override
    public void run() {
      try {
        File queueDir = Context.getInstance().getQueueDir();
        if (!queueDir.isDirectory() && !queueDir.mkdirs())
          throw new IOException("Could not create queue directory \"" + queueDir.getAbsolutePath() + "\"");
        File lockFile = new File(queueDir, ticket + ".lck");
        FileUtils.touch(lockFile);  
        try {
          if (extraInfo != null)
            FileUtils.write(new File(queueDir, ticket + ".xml"), extraInfo, StandardCharsets.UTF_8);
          OutputStream os = new BufferedOutputStream(new FileOutputStream(new File(queueDir, ticket + ".bin")));
          try {
            new InternalRequest().execute(path, os, false);
          } finally {
            os.close();
          }
        } finally {
          FileUtils.deleteQuietly(lockFile);
        }
      } catch (Exception e) {
        logger.error("Error adding queued request \"" + path + "\"", e);
      }
    }
   
  }
  
}