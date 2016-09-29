package nl.armatiek.xslweb.saxon.functions.sql;

import nl.armatiek.xslweb.utils.Closeable;

public class CloseableAutoCloseableWrapper implements Closeable {
  
  private AutoCloseable autoCloseable;
  private boolean isClosed;
  
  public CloseableAutoCloseableWrapper(AutoCloseable autoCloseable) {
    this.autoCloseable = autoCloseable; 
  }

  @Override
  public void close() throws Exception {
    if (!isClosed) {
      autoCloseable.close();
      isClosed = true;
    }    
  }

}