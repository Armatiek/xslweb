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
package nl.armatiek.xslweb.saxon.errrorlistener;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import net.sf.saxon.s9api.MessageListener;
import net.sf.saxon.s9api.XdmNode;

public class ErrorListenerMessageListener implements MessageListener {
  
  private ErrorListener errorListener;
  
  public ErrorListenerMessageListener(ErrorListener errorListener) {
    this.errorListener = errorListener; 
  }

  @Override
  public void message(XdmNode content, boolean terminate, SourceLocator locator) {
    try {
      TransformerException te = new TransformerException(content.toString(), locator);
      if (terminate) {
        errorListener.fatalError(te);
      } else {
        errorListener.warning(te);
      }
    } catch (TransformerException e) {
      // ignore
    }
  }
  
}