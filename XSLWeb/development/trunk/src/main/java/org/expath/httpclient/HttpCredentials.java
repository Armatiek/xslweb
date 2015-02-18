/****************************************************************************/
/*  File:       HttpCredentials.java                                        */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-22                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */

package org.expath.httpclient;

/**
 * TODO<doc>: ...
 * 
 * @author Florent Georges
 * @date 2009-02-22
 */
public class HttpCredentials {
  
  public HttpCredentials(String user, String pwd, String method) {
    myUser = user;
    myPwd = pwd;
    myMethod = method;
  }

  public String getUser() {
    return myUser;
  }

  public String getPwd() {
    return myPwd;
  }

  public String getMethod() {
    return myMethod;
  }

  private String myUser;
  private String myPwd;
  private String myMethod;
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
