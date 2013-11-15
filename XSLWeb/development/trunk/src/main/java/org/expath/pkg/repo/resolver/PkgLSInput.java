/****************************************************************************/
/*  File:       PkgLSInput.java                                             */
/*  Author:     F. Georges                                                  */
/*  Company:    H2O Consulting                                              */
/*  Date:       2009-10-19                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo.resolver;

import java.io.InputStream;
import java.io.Reader;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;

/**
 * Implementation of {@link LSInput} wrapping an {@link InputSource}.
 *
 * Package-private class used by {@link PkgLSResourceResolver}.
 *
 * @author Florent Georges
 * @date   2009-10-19
 */
class PkgLSInput
        implements LSInput
{
    public PkgLSInput(InputSource src) {
        mySrc = src;
    }

    @Override
    public Reader getCharacterStream() {
        return mySrc.getCharacterStream();
    }

    @Override
    public InputStream getByteStream() {
        return mySrc.getByteStream();
    }

    @Override
    public String getStringData() {
        return null;
    }

    @Override
    public String getSystemId() {
        return mySrc.getSystemId();
    }

    @Override
    public String getPublicId() {
        return mySrc.getPublicId();
    }

    @Override
    public String getBaseURI() {
        // FIXME: TODO: Is it correct?
        return mySrc.getSystemId();
    }

    @Override
    public String getEncoding() {
        return mySrc.getEncoding();
    }

    @Override
    public boolean getCertifiedText() {
        return false;
    }

    // *** Setters are not supported ***

    @Override
    public void setCharacterStream(Reader r) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setByteStream(InputStream s) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setStringData(String d) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setSystemId(String s) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setPublicId(String p) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setBaseURI(String b) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setEncoding(String e) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setCertifiedText(boolean c) {
        throw new UnsupportedOperationException("Not supported.");
    }

    private InputSource mySrc;
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
