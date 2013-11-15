/****************************************************************************/
/*  File:       SaxonSerialization.java                                     */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-06-16                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.zip.saxon;

import net.sf.saxon.lib.SaxonOutputKeys;
import org.expath.zip.Serialization;
import org.expath.zip.ZipException;

/**
 * Support for Saxon-specific serialization parameters.
 * 
 * TODO: Actually, for now, it also has to support standard serialization params
 * which have no constants in JAXP (which supports only the version 1.0 of the
 * serial spec).  For now it even only supports those actually...
 *
 * @author Florent Georges
 * @date   2011-06-16
 */
public class SaxonSerialization
        extends Serialization
{
    @Override
    public void setOutputParam(String name, String value)
            throws ZipException
    {
        if ( "byte-order-mark".equals(name) ) {
            setProperty(SaxonOutputKeys.BYTE_ORDER_MARK, value);
        }
        else if ( "escape-uri-uttributes".equals(name) ) {
            setProperty(SaxonOutputKeys.ESCAPE_URI_ATTRIBUTES, value);
        }
        else if ( "normalization-form".equals(name) ) {
            setProperty(SaxonOutputKeys.NORMALIZATION_FORM, value);
        }
        else if ( "suppress-indentation".equals(name) ) {
            setProperty(SaxonOutputKeys.SUPPRESS_INDENTATION, value);
        }
        else if ( "undeclare-prefixes".equals(name) ) {
            setProperty(SaxonOutputKeys.UNDECLARE_PREFIXES, value);
        }
        else {
            super.setOutputParam(name, value);
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
