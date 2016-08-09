/****************************************************************************/
/*  File:       SaxonResult.java                                            */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-03-10                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */

package org.expath.httpclient.saxon;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;

import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.HttpResponse;
import org.expath.httpclient.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ArrayIterator;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.StringValue;

/**
 * Implementation of {@link Item} for Saxon.
 * 
 * @author Florent Georges
 * @date 2011-03-10
 */
public class SaxonResult implements Result {
  
  private static final Logger logger = LoggerFactory.getLogger(SaxonResult.class);
  
  public SaxonResult(XPathContext ctxt) {
    myItems = new ArrayList<Item>();
    myCtxt = ctxt;
  }

  @Override
  public void add(String string) throws HttpClientException {
    Item item = new StringValue(string);
    myItems.add(item);
  }

  @Override
  public void add(byte[] bytes) throws HttpClientException {
    Item item = new Base64BinaryValue(bytes);
    myItems.add(item);
  }

  @Override
  public void add(Source src) throws HttpClientException {
    try {
      Item doc = myCtxt.getConfiguration().buildDocument(src);
      myItems.add(doc);
    } catch (XPathException ex) {
      logger.error("Error building the XML or HTML document", ex);
      throw new HttpClientException("Error building the XML or HTML document", ex);
    }
  }

  @Override
  public void add(HttpResponse response) throws HttpClientException {
    SaxonTreeBuilder builder = new SaxonTreeBuilder(myCtxt);
    response.outputResponseElement(builder);
    Item elem = builder.getCurrentRoot();
    myItems.add(0, elem);
  }

  public SequenceIterator newIterator() throws HttpClientException {
    Item[] array = myItems.toArray(new Item[0]);
    return new ArrayIterator(array);
  }

  private List<Item> myItems;
  private XPathContext myCtxt;
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
