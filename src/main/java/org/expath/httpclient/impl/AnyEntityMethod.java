package org.expath.httpclient.impl;

import java.net.URI;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * Implements any HTTP extension method, without any entity content.
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
public class AnyEntityMethod extends HttpEntityEnclosingRequestBase {
 
  public AnyEntityMethod(String method) {
    super();
    METHOD_NAME = method;
  }

  public AnyEntityMethod(String method, URI uri) {
    super();
    METHOD_NAME = method;
    setURI(uri);
  }

  public AnyEntityMethod(String method, String uri) {
    super();
    METHOD_NAME = method;
    setURI(URI.create(uri));
  }

  @Override
  public String getMethod() {
    return METHOD_NAME;
  }

  public String METHOD_NAME;
}