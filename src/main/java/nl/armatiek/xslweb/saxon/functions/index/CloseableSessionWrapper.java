package nl.armatiek.xslweb.saxon.functions.index;

import nl.armatiek.xmlindex.Session;
import nl.armatiek.xslweb.utils.Closeable;

public class CloseableSessionWrapper implements Closeable {
  
  private Session session;
  
  public CloseableSessionWrapper(Session session) {
    this.session = session; 
  }

  @Override
  public void close() throws Exception {
    session.close();    
  }

}