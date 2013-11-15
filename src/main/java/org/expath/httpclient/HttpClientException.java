/****************************************************************************/
/*  File:       HttpClientException.java                                    */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-03-01                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient;

/**
 * Generic exception for the EXPath HTTP Client implementation in Java.
 *
 * @author Florent Georges
 * @date   2011-03-01
 */
public class HttpClientException
        extends Exception
{
    public HttpClientException(String msg)
    {
        super(msg);
    }

    public HttpClientException(String msg, Throwable cause)
    {
        super(msg, cause);
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
