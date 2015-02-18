/****************************************************************************/
/*  File:       LoggerHelper.java                                           */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-08-04                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */

package org.expath.httpclient.impl;

import org.apache.commons.logging.Log;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.cookie.Cookie;

/**
 * TODO<doc>: ...
 * 
 * @author Florent Georges
 * @date 2009-08-04
 */
public class LoggerHelper {
  
  public static void logCookies(Log log, String prompt, Iterable<Cookie> cookies) {
    if (log.isDebugEnabled()) {
      if (cookies == null) {
        log.debug(prompt + ": null");
        return;
      }
      for (Cookie c : cookies) {
        log.debug(prompt + ": " + c.getName() + ": " + c.getValue());
      }
    }
  }

  public static void logHeaders(Log log, String prompt, Header[] headers) {
    if (log.isDebugEnabled()) {
      if (headers == null) {
        log.debug(prompt + ": null");
        return;
      }
      for (Header h : headers) {
        log.debug(prompt + ": " + h.getName() + ": " + h.getValue());
      }
    }
  }

  public static void logHeaderDetails(Log log, String prompt, Iterable<Header> headers) {
    if (log.isDebugEnabled()) {
      if (headers == null) {
        log.debug(prompt + ": null");
        return;
      }
      for (Header h : headers) {
        log.debug(prompt + " - HEADER: " + h.getName() + ": " + h.getValue());
        for (HeaderElement e : h.getElements()) {
          log.debug(prompt + " -   ELEM: " + e.getName() + ": " + e.getValue());
          for (NameValuePair p : e.getParameters()) {
            log.debug(prompt + " -     P: " + p.getName() + ": " + p.getValue());
          }
        }
      }
    }
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
