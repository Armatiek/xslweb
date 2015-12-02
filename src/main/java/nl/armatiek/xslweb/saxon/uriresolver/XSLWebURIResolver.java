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
import java.net.URI;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.lib.StandardURIResolver;
import net.sf.saxon.trans.XPathException;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.web.servlet.InternalRequest;

public class XSLWebURIResolver extends StandardURIResolver {

  @Override
  public Source resolve(String href, String base) throws XPathException {
    try {
      URI uri = new URI(href);
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
      } 
      return super.resolve(href, base);                  
    } catch (Exception e) {
      throw new XPathException(e);
    }
  }

}