package nl.armatiek.xslweb.saxon.uriresolver;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.lib.StandardURIResolver;
import net.sf.saxon.trans.XPathException;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.utils.XSLWebUtils;
import nl.armatiek.xslweb.web.servlet.InternalRequest;

public class XSLWebURIResolver extends StandardURIResolver {
  
  public enum DefaultBehaviour { SAXON, STREAM } 
  
  private DefaultBehaviour defaultBehaviour;
  private HttpServletRequest request = null;
  
  public XSLWebURIResolver() {
    this.defaultBehaviour = DefaultBehaviour.SAXON;
  }
  
  public XSLWebURIResolver(DefaultBehaviour defaultBehaviour) {
    this.defaultBehaviour = defaultBehaviour;
  }
  
  public XSLWebURIResolver(DefaultBehaviour defaultBehaviour, HttpServletRequest request) {
    this.defaultBehaviour = defaultBehaviour;
    this.request = request;
  }
  
  @Override
  public Source resolve(String href, String base) throws XPathException {
    try {
      URI uri = new URI(href);
      Map<String, List<String>> params = null;
      if (uri.getQuery() != null) {
        params = XSLWebUtils.splitQuery(uri);
      }            
      List<String> proxyHost = null;
      List<String> proxyPort = null;
      if (params != null) {
        proxyHost = params.get("proxyHost");
        proxyPort = params.get("proxyPort");
      }
      if (uri.isAbsolute() && uri.getScheme().equals(Definitions.SCHEME_XSLWEB)) {      
        InternalRequest internalRequest = new InternalRequest();
        ByteArrayOutputStream boas = new ByteArrayOutputStream();        
        String path = uri.getPath();
        String query = uri.getRawQuery();        
        if (query != null) {
          path = path + "?" + query;
        }        
        internalRequest.execute(path, boas, false, request);
        return new StreamSource(new ByteArrayInputStream(boas.toByteArray()), href);                
      } else if (uri.isAbsolute() && uri.getScheme().startsWith("http") && proxyHost != null && proxyPort != null) {               
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost.get(0), Integer.parseInt(proxyPort.get(0))));                       
        URLConnection connection = new URL(href).openConnection(proxy);                                        
        return new StreamSource(connection.getInputStream(), href);  
      }
      if (defaultBehaviour == DefaultBehaviour.SAXON)
        return super.resolve(href, base);
      else {
        URI absoluteUri;
        if (uri.isAbsolute()) 
          absoluteUri = uri;
        else {
          absoluteUri = ResolveURI.makeAbsolute(href, base);
        }
        return new StreamSource(absoluteUri.toString());
      }
    } catch (Exception e) {
      throw new XPathException(e);
    }
  }

}