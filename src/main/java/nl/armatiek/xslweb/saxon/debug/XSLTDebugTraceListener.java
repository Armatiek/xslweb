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
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.instruct.FixedAttribute;
import net.sf.saxon.expr.instruct.FixedElement;
import net.sf.saxon.expr.instruct.GlobalParam;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.NamedTemplate;
import net.sf.saxon.expr.instruct.TemplateRule;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.functions.Trace;
import net.sf.saxon.trace.Traceable;
import nl.armatiek.xslweb.configuration.WebApp;

public class XSLTDebugTraceListener extends DebugTraceListener {

  public XSLTDebugTraceListener(WebApp webApp, DebugClient client) {
    super(webApp, client);
  }

  @Override
  public String getInstructionLabel(Traceable info) {
    if (info instanceof Expression) {
      Expression expr = (Expression) info;
      if (expr instanceof FixedElement) {
        return "Fixed element";
      } else if (expr instanceof FixedAttribute) {
        return "Fixed attribute";
      } else if (expr instanceof LetExpression) {
        return "xsl:variable";
      } else if (expr.isCallOn(Trace.class)) {
        return "fn:trace";
      } else {
        return expr.getExpressionName();
      }
    } else if (info instanceof UserFunction) {
      return "xsl:function";
    } else if (info instanceof TemplateRule) {
      return "xsl:template";
    } else if (info instanceof NamedTemplate) {
      return "xsl:template";
    } else if (info instanceof GlobalParam) {
      return "xsl:param";
    } else if (info instanceof GlobalVariable) {
      return "xsl:variable";
    } else if (info instanceof Trace) {
      return "fn:trace";
    } else {
      return "misc";
    }
  }

}