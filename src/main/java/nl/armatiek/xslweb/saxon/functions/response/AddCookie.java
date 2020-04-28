package nl.armatiek.xslweb.saxon.functions.response;

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

import javax.servlet.http.Cookie;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;
import nl.armatiek.xslweb.saxon.utils.NodeInfoUtils;

public class AddCookie extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_RESPONSE, "add-cookie");

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
    return 1;
  }

  @Override
  public SequenceType[] getArgumentTypes() {    
    return new SequenceType[] { SequenceType.SINGLE_NODE };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {    
    return SequenceType.OPTIONAL_BOOLEAN;
  }
  
  @Override
  public boolean hasSideEffects() {    
    return true;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new ResponseAddCookieCall();
  }
  
  private static class ResponseAddCookieCall extends ExtensionFunctionCall {
        
    @Override
    public Sequence<?> call(XPathContext context, Sequence[] arguments) throws XPathException {                                  
      NodeInfo cookieElem =  unwrapNodeInfo((NodeInfo) arguments[0].head());                                          
      String comment = NodeInfoUtils.getValueOfChildElementByLocalName(cookieElem, "comment", context);
      String domain = NodeInfoUtils.getValueOfChildElementByLocalName(cookieElem, "domain", context);
      String maxAge = NodeInfoUtils.getValueOfChildElementByLocalName(cookieElem, "max-age", context);
      String name = NodeInfoUtils.getValueOfChildElementByLocalName(cookieElem, "name", context);
      String path = NodeInfoUtils.getValueOfChildElementByLocalName(cookieElem, "path", context);
      String isSecure = NodeInfoUtils.getValueOfChildElementByLocalName(cookieElem, "is-secure", context);
      String value = NodeInfoUtils.getValueOfChildElementByLocalName(cookieElem, "value", context);
      String version = NodeInfoUtils.getValueOfChildElementByLocalName(cookieElem, "version", context);
      Cookie cookie = new Cookie(name, value);
      if (comment != null) cookie.setComment(comment);
      if (domain != null) cookie.setDomain(domain);
      if (maxAge != null) cookie.setMaxAge(Integer.parseInt(maxAge));
      if (path != null) cookie.setPath(path);
      if (isSecure != null) cookie.setSecure(Boolean.parseBoolean(isSecure));
      if (version != null) cookie.setVersion(Integer.parseInt(version));                
      getResponse(context).addCookie(cookie);                                                      
      return EmptySequence.getInstance();              
    }
    
  }

}