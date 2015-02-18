/****************************************************************************/
/*  File:       SendRequestCall.java                                        */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-08-08                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */

package org.expath.httpclient.saxon;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

import org.expath.saxon.HttpClient;

/**
 * TODO: Doc...
 * 
 * @author Florent Georges
 * @date 2009-08-08
 */
public class SendRequestCall extends ExtensionFunctionCall {
  @Override
  public Sequence call(XPathContext context, Sequence[] params) throws XPathException {
    NodeInfo request = null;
    String href = null;
    SequenceIterator bodies = null;
    switch (params.length) {
    case 3:
      bodies = params[2].iterate();
    case 2:
      href = getHref(params[1].iterate());
    case 1:
      request = getRequest(params[0].iterate());
      break;
    default:
      throw new XPathException("Incorrect number of params: " + params.length);
    }
    return SequenceTool.toLazySequence(HttpClient.sendRequest(context, request, href, bodies, getWebApp(context).getHttpClient()));
  }

  private NodeInfo getRequest(SequenceIterator param) throws XPathException {
    Item item = param.next();
    if (item == null) {
      throw new XPathException("The request param is an empty sequence");
    }
    if (!(item instanceof NodeInfo)) {
      throw new XPathException("The request param is not a node");
    }
    return (NodeInfo) item;
  }

  private String getHref(SequenceIterator param) throws XPathException {
    Item item = param.next();
    if (item == null) {
      return null;
    }
    if (!(item instanceof StringValue)) {
      throw new XPathException("The href param is not a string");
    }
    return item.getStringValue();
  }
}

/* ------------------------------------------------------------------------ */
/*  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS COMMENT.               */
/*                                                                          */
/*  The contents of this file are subject to the Mozilla Public License     */
/*  Version 1.0 (the "License"); you may not use this file except in        */
/*  compliance with the License. You may obtain a copy of the License at    */
/*  http://www.mozilla.org/MPL/.                                            */
/*                                                                          */
/*  Software distributed under the License is distributed on an "AS IS"     */
/*  basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See    */
/*  the License for the specific language governing rights and limitations  */
/*  under the License.                                                      */
/*                                                                          */
/*  The Original Code is: all this file.                                    */
/*                                                                          */
/*  The Initial Developer of the Original Code is Florent Georges.          */
/*                                                                          */
/*  Contributor(s): none.                                                   */
/* ------------------------------------------------------------------------ */
