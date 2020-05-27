package nl.armatiek.xslweb.configuration;

import net.sf.saxon.om.NamePool;
import nl.armatiek.xslweb.saxon.functions.httpclient.Types;

public class Fingerprints {
  
  public int HTTPCLIENT_RESPONSE;
  public int HTTPCLIENT_HEADER;
  public int HTTPCLIENT_BODY;
  public int STATUS;
  public int MESSAGE;
  public int NAME;
  public int VALUE;
  public int MEDIATYPE;
  public int METHOD;
  public int HTTPCLIENT_REQUEST;
  public int HREF;
  public int STATUSONLY;
  public int OVERRIDEMEDIATYPE;
  public int FUNCTION_DIAGNOSTICS;
  public int FUNCTION_DIAGNOSTIC;
  public int CODE;
  public int LINE;
  public int COLUMN;
  public int START;
  public int END;
  public int KIND;
  public int POSITION;
  
  public Fingerprints(NamePool namePool) {
    HTTPCLIENT_RESPONSE = namePool.allocateFingerprint(Types.EXT_NAMESPACEURI, "response");
    HTTPCLIENT_REQUEST = namePool.allocateFingerprint(Types.EXT_NAMESPACEURI, "request"); 
    HTTPCLIENT_HEADER = namePool.allocateFingerprint(Types.EXT_NAMESPACEURI, "header");
    HTTPCLIENT_BODY = namePool.allocateFingerprint(Types.EXT_NAMESPACEURI, "body");
    STATUS = namePool.allocateFingerprint("", "status");
    MESSAGE = namePool.allocateFingerprint("", "message");
    NAME = namePool.allocateFingerprint("", "name");
    VALUE = namePool.allocateFingerprint("", "value");
    MEDIATYPE = namePool.allocateFingerprint("", "media-type");
    METHOD = namePool.allocateFingerprint("", "method");
    HREF = namePool.allocateFingerprint("", "href");
    STATUSONLY = namePool.allocateFingerprint("", "status-only");
    OVERRIDEMEDIATYPE = namePool.allocateFingerprint("", "override-media-type");
    FUNCTION_DIAGNOSTICS = namePool.allocateFingerprint(Definitions.NAMESPACEURI_XSLWEB_FX_DYNFUNC, "diagnostics");
    FUNCTION_DIAGNOSTIC = namePool.allocateFingerprint(Definitions.NAMESPACEURI_XSLWEB_FX_DYNFUNC, "diagnostic");
    CODE = namePool.allocateFingerprint("", "code"); 
    LINE = namePool.allocateFingerprint("", "line");
    COLUMN = namePool.allocateFingerprint("", "column");
    START = namePool.allocateFingerprint("", "start");
    END = namePool.allocateFingerprint("", "end");
    KIND = namePool.allocateFingerprint("", "kind");
    POSITION = namePool.allocateFingerprint("", "position");
  }

}