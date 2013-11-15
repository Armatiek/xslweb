/****************************************************************************/
/*  File:       SerializationParams.java                                    */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2010-01-10                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient;

/**
 * Represent an output definition (i.e. a set of serialization parameters).
 *
 * TODO: Other serialization parameters are defined, but not implemented yet.
 * There are only encoding, indent and omit-xml-declaration here.  Should also
 * have (to double-check...):
 *
 *     - byte-order-mark
 *     - cdata-section-elements
 *     - doctype-public
 *     - doctype-system
 *     - escape-uri-attributes
 *     - normalization-form
 *     - standalone
 *     - suppress-indentation
 *     - undeclare-prfixes
 *     - version
 *
 * @author Florent Georges
 * @date   2010-01-10
 */
public class SerializationParams
{
    // encoding
    public String getEncoding() {
        return myEncoding;
    }
    public void setEncoding(String s) {
        myEncoding = s;
    }
    private String myEncoding;

    // indent
    public Boolean getIndent() {
        return myIndent;
    }
    public void setIndent(Boolean b) {
        myIndent = b;
    }
    private Boolean myIndent;

    // omit-xml-declaration
    public Boolean getOmitXmlDecl() {
        return myOmitXmlDecl;
    }
    public void setOmitXmlDecl(Boolean b) {
        myOmitXmlDecl = b;
    }
    private Boolean myOmitXmlDecl;
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
