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

package nl.armatiek.xslweb.saxon.functions.index;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.TransformerException;

import net.sf.saxon.expr.parser.ExplicitLocation;
import net.sf.saxon.expr.parser.Location;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.QualifiedNameValue;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

/**
 * 
 * @author Maarten Kroon
 */
public abstract class TransformOrQueryCall extends ExtensionFunctionCall {

  public Map<QName, XdmValue> mapToParams(HashTrieMap map) {
    if (map == null)
      return null;
    HashMap<QName, XdmValue> params = new HashMap<QName, XdmValue>();
    Iterator<KeyValuePair> iter =  map.iterator();
    while (iter.hasNext()) {
      KeyValuePair pair = iter.next();
      QualifiedNameValue key = (QualifiedNameValue) pair.key;
      QName name = new QName(key.getNamespaceURI(), key.getLocalName());
      XdmValue value = XdmValue.wrap(pair.value);
      params.put(name, value);
    }
    return params;
  }
  
  public XPathException getXPathException(ErrorListener errorListener, SaxonApiException sae) {
    TransformerException e = errorListener.getFirstError();
    if (e == null)
      return null;
    Location loc = (e.getLocator() != null) ? new ExplicitLocation(e.getLocator()) : null;
    XPathException xpe = new XPathException(e.getMessage(), loc, e);
    QName errorCode = sae.getErrorCode();
    if (errorCode != null)
      xpe.setErrorCodeQName(errorCode.getStructuredQName());
    return xpe;
  }
  
  private void writeErrorElem(TinyBuilder builder, String localName, String content) throws XPathException {
    builder.startElement(new FingerprintedQName("err", NamespaceConstant.ERR, localName), 
        Untyped.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0);
    builder.startContent();
    builder.characters(content, ExplicitLocation.UNKNOWN_LOCATION, 0);
    builder.endElement();
  }
  
  public NodeInfo getErrorNode(TinyBuilder builder, Exception e) throws XPathException {
    builder.open();
    builder.startElement(new FingerprintedQName("err", NamespaceConstant.ERR, "error"), 
        Untyped.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0);
    if (e instanceof XPathException) {
      XPathException xpe = (XPathException) e;
      if (xpe.getErrorCodeQName() != null)
        writeErrorElem(builder, "code", xpe.getErrorCodeQName().getDisplayName());
      writeErrorElem(builder, "description", xpe.getMessage());
      if (xpe.getLocator() != null) {
        Location loc = xpe.getLocator();
        if (loc.getSystemId() != null)
          writeErrorElem(builder, "module", loc.getSystemId());
        writeErrorElem(builder, "line-number", Integer.toString(loc.getLineNumber()));
        writeErrorElem(builder, "column-number", Integer.toString(loc.getColumnNumber()));
      }
    } else {
      writeErrorElem(builder, "description", e.getMessage());
    }
    builder.endElement();
    builder.close();
    return builder.getCurrentRoot();
  }
  
}