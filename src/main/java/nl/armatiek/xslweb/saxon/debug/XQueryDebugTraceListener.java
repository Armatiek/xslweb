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

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.flwor.ClauseInfo;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.functions.Trace;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trace.Traceable;
import net.sf.saxon.trace.TraceableComponent;
import nl.armatiek.xslweb.configuration.WebApp;

public class XQueryDebugTraceListener extends DebugTraceListener {

  public XQueryDebugTraceListener(WebApp webApp) {
    super(webApp);
  }

  @Override
  public String getInstructionLabel(Traceable info) {
    if (info instanceof TraceableComponent) {
      if (info instanceof GlobalVariable) {
        return "variable";
      } else if (info instanceof UserFunction) {
        return "function";
      } else if (info instanceof XQueryExpression) {
        return "query";
      } else {
        return "misc";
      }
    } else if (info instanceof Trace) {
      return "fn:trace";
    } else if (info instanceof ClauseInfo) {
      return ((ClauseInfo) info).getClause().getClauseKey().toString();
    } else if (info instanceof Expression) {
      String s = ((Expression) info).getExpressionName();
      if (s.startsWith("xsl:")) {
        s = s.substring(4);
      }
      switch (s) {
      case "value-of":
        return "text";
      case "LRE":
        return "element";
      case "ATTR":
        return "attribute";
      default:
        return s;
      }
    } else {
      return null;
    }
  }

}