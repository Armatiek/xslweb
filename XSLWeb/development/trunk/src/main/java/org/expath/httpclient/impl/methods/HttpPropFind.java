package org.expath.httpclient.impl.methods;

import java.net.URI;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * Implements the HTTP extension PROPFIND method, defined by WebDAV.
 * 
 * TODO: Gneralize into a "whatever method, given at runtime", so it will be
 * possible to support any HTTP extension method, without requiring to have a
 * class explicitely supporting it (the name in {@code http:request/@method}
 * will be enough).
 * 
 * The above point will maybe require to have an empty
 * {@code http:request/http:body} on requests with a method allowing body, but
 * with an empty body. So at runtime if we do not know the method, we can at
 * least choose between the base classes {@code HttpRequestBase} and
 * {@code HttpEntityEnclosingRequestBase}.
 * 
 * @author Florent Georges
 * @date 2009-11-18
 */
public class HttpPropFind extends HttpEntityEnclosingRequestBase {
  
  public HttpPropFind() {
    super();
  }

  public HttpPropFind(URI uri) {
    super();
    setURI(uri);
  }

  public HttpPropFind(String uri) {
    super();
    setURI(URI.create(uri));
  }

  @Override
  public String getMethod() {
    return METHOD_NAME;
  }

  public final static String METHOD_NAME = "PROPFIND";
}