/****************************************************************************/
/*  File:       TreeBuilder.java                                            */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-02-21                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.zip;

/**
 * An abstract tree builder, to be implemented for each specific processor.
 *
 * TODO: Review the javadoc in case this interface does not allow pushing text
 * in element content (as this is not needed in this project).
 *
 * @author Florent Georges
 * @date   2011-02-21
 */
public interface TreeBuilder
{
    /**
     * Start a new element, in the zip namespace (see {@link ZipConstants}).
     */
    public void startElement(String local_name)
            throws ZipException;

    /**
     * End the currently opened element.
     */
    public void endElement()
            throws ZipException;

    /**
     * Start element content.
     *
     * After a call to {@link #startElement(String)}, you can push attributes.
     * Before pushing some text or child elements, you have to call this method
     * (in order to close the opening tag and start pushing content).  So the
     * last call on this object must have been {@link #startElement(String)},
     * optionally followed by some attributes.
     */
    public void startContent()
            throws ZipException;

    /**
     * Create a new attribute on the current element, in no namespace.
     *
     * A call to this method must follow a call to {@link #startElement(String)}
     * or another call to this method.  After all attributes have been added to
     * the element, the start tag must be closed by calling the method {@link
     * #startContent()}, in order to push text, other element or to close the
     * current element.
     */
    public void attribute(String name, String value)
            throws ZipException;
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
