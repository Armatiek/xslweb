/****************************************************************************/
/*  File:       Element.java                                                */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-03-09                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient.model;

import org.expath.httpclient.HttpClientException;

/**
 * An abstract representation of an element (just provide the HTTP Client needs).
 *
 * @author Florent Georges
 * @date   2011-03-09
 */
public interface Element
{
    /**
     * Get the local part of the name of the element.  Cannot be null nor empty.
     */
    public String getLocalName()
            throws HttpClientException;

    /**
     * Return the namespace URI part of the name of the element.
     *
     * Return the empty string if the name is in no namespace (never return
     * {@code null}).
     */
    public String getNamespaceUri();

    /**
     * Get the display name of the element.  Cannot be null nor empty.
     *
     * The display name is the original lexical name (with the original prefix
     * if any).  An implementation is not required to return the exact same
     * name as the original, and can instead make a "best guess".  This is for
     * reporting purpose only.
     */
    public String getDisplayName()
            throws HttpClientException;

    /**
     * Return the value of an attribute.
     *
     * @param name The local name of the attribute to look for.  The attribute
     * is looked for in no namespace.
     *
     * @return The value of the attribute, or null if it does not exist.
     */
    public String getAttribute(String local_name)
            throws HttpClientException;

    /**
     * Iterate through the attributes.
     */
    public Iterable<Attribute> attributes()
            throws HttpClientException;

    /**
     * Return true if this element has at least one child in no namespace.
     */
    public boolean hasNoNsChild()
            throws HttpClientException;

    /**
     * Iterate through the children elements.
     */
    public Iterable<Element> children()
            throws HttpClientException;

    /**
     * Iterate through the children elements in the HTTP Client namespace.
     */
    public Iterable<Element> httpNsChildren()
            throws HttpClientException;

    /**
     * Check the element {@code elem} does not have attributes other than {@code names}.
     *
     * {@code names} contains non-qualified names, for allowed attributes.  The
     * element can have other attributes in other namespace (not in the HTTP
     * Client namespace) but no attributes in no namespace.
     *
     * @param names The non-qualified names of allowed attributes (cannot be
     * null, but can be empty.)
     *
     * @throws HttpClientException If the element contains an attribute in the
     * HTTP Client namespace, or in no namespace and the name of which is not
     * in {@code names}.
     */
    public void noOtherNCNameAttribute(String[] names)
            throws HttpClientException;

    /**
     * Return the content of the element (the content of the child:: axis).
     */
    public Sequence getContent()
            throws HttpClientException;
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
