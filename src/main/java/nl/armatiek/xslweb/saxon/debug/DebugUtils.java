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
package nl.armatiek.xslweb.saxon.debug;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;

public class DebugUtils {
  
  private static XsltExecutable serializeSequenceXsltExecutable;
  
  public static void setDebugTraceListener(WebApp webApp, HttpServletRequest req, Object controller) {
    if (Context.getInstance().getDebugEnable() && webApp.getDebugMode()) {
      HttpSession session = req.getSession(false);
      DebugClient debugClient = (session != null) ? (DebugClient) session.getAttribute(Definitions.ATTRNAME_DEBUGCLIENT) : null;
      if (debugClient != null) {
        if (controller instanceof Xslt30Transformer) {
          TraceListener traceListener = new XSLTDebugTraceListener(webApp, debugClient);
          // TraceListener traceListener = new XSLTTraceListener();
          ((Xslt30Transformer) controller).setTraceListener(traceListener);
        } else if (controller instanceof XQueryEvaluator) {
          TraceListener traceListener = new XQueryDebugTraceListener(webApp, debugClient); 
          ((XQueryEvaluator) controller).setTraceListener(traceListener);
        }
      }
    }
  }
  
  public static String getDisplayText(WebApp webApp, Sequence seq, String displayMode) throws SaxonApiException {
    if (seq == null) {
      return "";
    }
    if (serializeSequenceXsltExecutable == null) {
      StreamSource source = new StreamSource(webApp.getClass().getClassLoader().getResourceAsStream("serialize-sequence.xsl"));
      XsltCompiler comp = webApp.getProcessor().newXsltCompiler();
      serializeSequenceXsltExecutable = comp.compile(source);
    }
    Xslt30Transformer trans = serializeSequenceXsltExecutable.load30();
    Map<QName, XdmValue> params = new HashMap<QName, XdmValue>();
    params.put(new QName("sequence"), XdmValue.wrap(seq));
    params.put(new QName("display-mode"), new XdmAtomicValue(displayMode));
    trans.setStylesheetParameters(params);
    XdmValue text = trans.callTemplate(new QName("serialize-sequence"));
    return text.toString();
  }

}