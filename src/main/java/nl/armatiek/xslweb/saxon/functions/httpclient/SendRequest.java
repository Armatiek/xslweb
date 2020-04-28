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
package nl.armatiek.xslweb.saxon.functions.httpclient;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrMore;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.saxon.utils.NodeInfoUtils;
import okhttp3.Authenticator;
import okhttp3.ConnectionSpec;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

/** 
 * 
 * @author Maarten Kroon
 */
public class SendRequest extends ExtensionFunctionDefinition {
  
  public static final StructuredQName qName = new StructuredQName("", Types.EXT_NAMESPACEURI, "send-request");
  
  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 1;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 3;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { SequenceType.OPTIONAL_NODE, SequenceType.OPTIONAL_STRING, SequenceType.ANY_SEQUENCE };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.makeSequenceType(AnyItemType.getInstance(), StaticProperty.ALLOWS_ONE_OR_MORE);
  }
  
  @Override
  public boolean hasSideEffects() {    
    return true;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new SendRequestCall();
  }
  
  protected static class SendRequestCall extends nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall {
        
    private static final int TIMEOUT_DEFAULT = 30;
    
    private final static OkHttpClient client = new OkHttpClient.Builder()
        .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT))
        .build();
    
    private boolean getBoolean(String value, boolean defaultValue) {
      if (value == null) {
        return defaultValue;
      }
      ConversionResult result = BooleanValue.fromString(value);
      if (result instanceof BooleanValue) {
        return ((BooleanValue) result).getBooleanValue();
      } else {
        return defaultValue; // TODO: throw Exception?
      }  
    }
    
    private int getInteger(String value, int defaultValue) throws XPathException {
      if (value == null) {
        return defaultValue;
      }
      ConversionResult result = IntegerValue.stringToInteger(value);
      if (result instanceof IntegerValue) {
        return (int) ((IntegerValue) result).longValue();
      } else {
        return defaultValue; // TODO: throw Exception?
      }  
    }
    
    @Override
    public ZeroOrMore<Item<?>> call(XPathContext context, Sequence[] arguments) throws XPathException {
      NodeInfo requestElem =  unwrapNodeInfo((NodeInfo) arguments[0].head()); 
      String method = requestElem.getAttributeValue("", "method");
      if (StringUtils.isBlank(method)) {
        throw new XPathException("http:request/@method must be specified", "HC005");
      }
      String href = requestElem.getAttributeValue("", "href");
      boolean statusOnly = getBoolean(requestElem.getAttributeValue("", "status-only"), false);
      String username = requestElem.getAttributeValue("", "username");
      String password = requestElem.getAttributeValue("", "password");
      String authMethod = requestElem.getAttributeValue("", "auth-method");
      if (StringUtils.isNoneBlank(username)) {
        if (StringUtils.isBlank(password)) {
          throw new XPathException("http:request/@password must be specified if http:request/@username is specified", "HC005");
        }
        if (StringUtils.isBlank(authMethod)) {
          throw new XPathException("http:request/@auth-method must be specified if http:request/@username is specified", "HC005");
        }
        if (!authMethod.toLowerCase().equals("basic")) {
          throw new XPathException("Only authentication method \"Basic\" is supported in http:request/@auth-method", "HC005");
        } 
      }
      boolean sendAuthorization = getBoolean(requestElem.getAttributeValue("", "send-authorization"), false);
      String overrideMediaType = requestElem.getAttributeValue("", "override-media-type");
      boolean followRedirect = getBoolean(requestElem.getAttributeValue("", "follow-redirect"), true);
      int timeout = getInteger(requestElem.getAttributeValue("", "timeout"), TIMEOUT_DEFAULT);
      String proxyHost = requestElem.getAttributeValue("", "proxy-host");
      int proxyPort = getInteger(requestElem.getAttributeValue("", "proxy-port"), 8080);
      String proxyUsername = requestElem.getAttributeValue("", "proxy-username");
      String proxyPassword = requestElem.getAttributeValue("", "proxy-password");
      boolean trustAllCerts = getBoolean(requestElem.getAttributeValue("", "trust-all-certs"), false);
      
      if (arguments.length > 1 && arguments[1].head() != null) {
        href = ((StringValue) arguments[1].head()).getStringValue();  
      }
      if (StringUtils.isBlank(href)) {
        throw new XPathException("href is not specified, not in http:request/@href and not as second function argument", "HC005");
      }
      
      Sequence bodies = null;
      if (arguments.length > 2) {
        bodies = arguments[2];  
      }
      
      RequestBody requestBody = null;
      Request.Builder requestBuilder = new Request.Builder().url(href);
      
      if (sendAuthorization) {
        requestBuilder = requestBuilder.header("Authorization", Credentials.basic(username, password));
      }
      
      // Request headers and body:
      NodeInfo childElem = NodeInfoUtils.getFirstChildElement(requestElem);
      while (childElem != null) {
        switch (childElem.getLocalPart()) {
        case "header":
          requestBuilder.addHeader(
              childElem.getAttributeValue("", "name"), 
              childElem.getAttributeValue("", "value"));
          break;
        case "body":
          requestBody = RequestUtils.getRequestBody(childElem, bodies, 0, context);
          break;
        case "multipart":
          requestBody = RequestUtils.getMultipartRequestBody(childElem, bodies, context);
          break;
        }
        childElem = NodeInfoUtils.getNextSiblingElement(childElem);
      }
      requestBuilder.method(method.toUpperCase(), requestBody);
      
      Request request = requestBuilder.build();
      
      OkHttpClient.Builder clientBuilder = client.newBuilder();
      
      // Timeouts:
      if (timeout != TIMEOUT_DEFAULT) {
        clientBuilder
          .connectTimeout(timeout, TimeUnit.SECONDS)
          .writeTimeout(timeout, TimeUnit.SECONDS)
          .readTimeout(timeout, TimeUnit.SECONDS)
          .callTimeout(timeout, TimeUnit.SECONDS);
      }
      
      // Redirects: 
      if (!followRedirect) {
        clientBuilder
          .followRedirects(false)
          .followSslRedirects(false);
      }
      
      // Proxy:
      if (proxyHost != null) {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        clientBuilder.proxy(proxy);
      }
      
      // Proxy authentication
      if (StringUtils.isNoneBlank(proxyUsername) && StringUtils.isNoneBlank(proxyPassword)) {
        // TODO: add digest authentication
        clientBuilder.proxyAuthenticator(new Authenticator() {
          @Override
          public Request authenticate(Route route, Response response) throws IOException {
            if (response.request().header("Proxy-Authorization") != null) {
              return null; // Give up, we've already attempted to authenticate.
            }
            String credential = Credentials.basic(proxyUsername, proxyPassword);
            return response.request().newBuilder().header("Proxy-Authorization", credential).build();
          }
        });  
      }
      
      // Authentication:
      if (StringUtils.isNoneBlank(username)) {
        // TODO: add digest authentication
        clientBuilder.authenticator(new Authenticator() {    
          @Override
          public Request authenticate(Route route, Response response) throws IOException {
            if (response.request().header("Authorization") != null) {
              return null; // Give up, we've already attempted to authenticate.
            }
            String credential = Credentials.basic(username, password);
            return response.request().newBuilder().header("Authorization", credential).build();
          }
        });

      }
      
      // Trust all certificates:
      if (trustAllCerts || Context.getInstance().getTrustAllCerts()) {
        TrustAllCerts.setTrustAllCerts(clientBuilder);
      }
      
      ArrayList<Item> resultList = new ArrayList<Item>();
      
      OkHttpClient customClient = clientBuilder.build();
      
      // Execute the request synchronously:
      try (Response response = customClient.newCall(request).execute()) {
       
        // Build the http:response element:
        resultList.add(ResponseUtils.buildResponseElement(response, context));
        
        if (!statusOnly) {
          // Build the response content:
          resultList.add(ResponseUtils.buildResponseContent(response, context, requestElem, overrideMediaType));
        }
        
      } catch (InterruptedIOException e) {
        if (e instanceof SocketTimeoutException) {
          throw new XPathException("Socket timeout exception", "HC006");  
        } else {
          throw new XPathException("Call timeout exception", "HC006");
        }
      } catch (XPathException e) {
        e.setErrorCode("HC001");
        throw e;
      } catch (Exception e) {
        throw new XPathException("An HTTP error occurred: " + e.getMessage(), "HC001");
      }
      
      return new ZeroOrMore<Item<?>>(resultList.toArray(new Item[resultList.size()]));
      
    }
    
  }
  
}