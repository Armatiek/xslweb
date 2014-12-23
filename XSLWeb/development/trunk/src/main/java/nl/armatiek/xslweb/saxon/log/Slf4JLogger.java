package nl.armatiek.xslweb.saxon.log;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.lib.Logger;

import org.apache.commons.io.output.ProxyWriter;

public class Slf4JLogger extends Logger {
  
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Slf4JLogger.class);

  @Override
  public void println(String message, int severity) {
    switch(severity) {
    case Logger.INFO:
      logger.info(message);
      break;
    case Logger.WARNING:
      logger.warn(message);
      break;
    case Logger.ERROR:
      logger.error(message);
      break;
    case Logger.DISASTER:
      logger.error(message);
      break;
    }
  }

  @Override
  public StreamResult asStreamResult() {
    Writer w = new ProxyWriter(new StringWriter()) {
      @Override
      public void flush() throws IOException {        
        logger.info(out.toString());
        super.flush();
      }
    };    
    return new StreamResult(w);        
  }
  
}