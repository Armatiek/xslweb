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
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.lib.StandardURIResolver;
import net.sf.saxon.trans.XPathException;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.web.servlet.InternalRequest;

public class XSLWebURIResolver extends StandardURIResolver {
  
  private Map<String, List<String>> splitQuery(URI uri) throws UnsupportedEncodingException {
    String query = uri.getRawQuery();
    if (query == null)
      return null;
    final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
    final String[] pairs = query.split("&");
    for (String pair : pairs) {
      final int idx = pair.indexOf("=");
      final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
      if (!query_pairs.containsKey(key)) {
        query_pairs.put(key, new LinkedList<String>());
      }
      final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
      query_pairs.get(key).add(value);
    }
    return query_pairs;
  }

  @Override
  public Source resolve(String href, String base) throws XPathException {
    try {
      URI uri = new URI(href);
      Map<String, List<String>> params = null;
      if (uri.getQuery() != null) {
        params = splitQuery(uri);
      }            
      List<String> proxyHost = null;
      List<String> proxyPort = null;
      if (params != null) {
        proxyHost = params.get("proxyHost");
        proxyPort = params.get("proxyPort");
      }
      if (uri.isAbsolute() && uri.getScheme().equals(Definitions.SCHEME_XSLWEB)) {      
        InternalRequest request = new InternalRequest();
        ByteArrayOutputStream boas = new ByteArrayOutputStream();        
        String path = uri.getPath();
        String query = uri.getRawQuery();        
        if (query != null) {
          path = path + "?" + query;
        }        
        request.execute(path, boas);
        return new StreamSource(new ByteArrayInputStream(boas.toByteArray()), href);                
      } else if (uri.isAbsolute() && uri.getScheme().startsWith("http") && proxyHost != null && proxyPort != null) {               
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost.get(0), Integer.parseInt(proxyPort.get(0))));                       
        URLConnection connection = new URL(href).openConnection(proxy);                                        
        return new StreamSource(connection.getInputStream(), href);  
      }
      return super.resolve(href, base);                  
    } catch (Exception e) {
      throw new XPathException(e);
    }
  }

}