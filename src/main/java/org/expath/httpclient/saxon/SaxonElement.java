/****************************************************************************/
/*  File:       SaxonElement.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-03-10                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient.saxon;

import java.util.Arrays;
import java.util.Iterator;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NamespaceTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.type.Type;
import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.HttpConstants;
import org.expath.httpclient.model.Attribute;
import org.expath.httpclient.model.Element;
import org.expath.httpclient.model.Sequence;

/**
 * Saxon implementation of {@link Element}, relying on {@link NodeInfo}.
 *
 * @author Florent Georges
 * @date   2011-03-10
 */
public class SaxonElement
        implements Element
{
    public SaxonElement(NodeInfo node, XPathContext ctxt)
            throws HttpClientException
    {
        if ( node == null ) {
            throw new HttpClientException("the node is null");
        }
        if ( node.getNodeKind() != Type.ELEMENT ) {
            throw new HttpClientException("the node is not an element");
        }
        myNode = node;
        myCtxt = ctxt;
    }

    @Override
    public String getLocalName()
            throws HttpClientException
    {
        return myNode.getLocalPart();
    }

    @Override
    public String getNamespaceUri()
    {
        return myNode.getURI();
    }

    @Override
    public String getDisplayName()
            throws HttpClientException
    {
        return myNode.getDisplayName();
    }

    @Override
    public String getAttribute(String local_name)
            throws HttpClientException
    {
        // get the attribute
        NamePool pool = myNode.getNamePool();
        NodeTest pred = new NameTest(Type.ATTRIBUTE, "", local_name, pool);
        AxisIterator attrs = myNode.iterateAxis(Axis.ATTRIBUTE, pred);
        NodeInfo a = (NodeInfo) attrs.next();
        // return its string value, or null if there is no such attribute
        if ( a == null ) {
            return null;
        }
        else {
            return a.getStringValue();
        }
    }

    @Override
    public Iterable<Attribute> attributes()
    {
        AxisIterator it = myNode.iterateAxis(Axis.ATTRIBUTE);
        return new AttributeIterable(it);
    }

    @Override
    public boolean hasNoNsChild()
            throws HttpClientException
    {
        NamePool pool = myNode.getNamePool();
        NodeTest no_ns_pred = new NamespaceTest(pool, Type.ELEMENT, "");
        return myNode.iterateAxis(Axis.CHILD, no_ns_pred).moveNext();
    }

    @Override
    public void noOtherNCNameAttribute(String[] names)
            throws HttpClientException
    {
        if ( names == null ) {
            throw new NullPointerException("the names array is null");
        }
        String[] sorted = new String[names.length];
        for ( int i = 0; i < names.length; ++i ) {
            sorted[i] = names[i];
        }
        Arrays.sort(sorted);
        String elem_name = myNode.getDisplayName();
        AxisIterator it = myNode.iterateAxis(Axis.ATTRIBUTE);
        NodeInfo attr;
        while ( (attr = (NodeInfo) it.next()) != null ) {
            String attr_name = attr.getDisplayName();
            if ( HttpConstants.HTTP_CLIENT_NS_URI.equals(attr.getURI()) ) {
                throw new HttpClientException("@" + attr_name + " not allowed on " + elem_name);
            }
            else if ( ! "".equals(attr.getURI()) ) {
                // ignore other-namespace-attributes
            }
            else if ( Arrays.binarySearch(sorted, attr.getLocalPart()) < 0 ) {
                throw new HttpClientException("@" + attr_name + " not allowed on " + elem_name);
            }
        }
    }

    @Override
    public Sequence getContent()
            throws HttpClientException
    {
        SequenceIterator it = myNode.iterateAxis(Axis.CHILD);
        return new SaxonSequence(it, myCtxt);
    }

    @Override
    public Iterable<Element> children()
            throws HttpClientException
    {
        AxisIterator it = myNode.iterateAxis(Axis.CHILD, NodeKindTest.ELEMENT);
        return new ElemIterable(it);
    }

    @Override
    public Iterable<Element> httpNsChildren()
            throws HttpClientException
    {
        String http_ns = HttpConstants.HTTP_CLIENT_NS_URI;
        NamePool pool = myNode.getNamePool();
        NodeTest pred = new NamespaceTest(pool, Type.ELEMENT, http_ns);
        AxisIterator it = myNode.iterateAxis(Axis.CHILD, pred);
        return new ElemIterable(it);
    }

    private NodeInfo myNode;
    private XPathContext myCtxt;

    private static class AttributeIterable
            implements Iterable<Attribute>
    {
        public AttributeIterable(AxisIterator it)
        {
            myIter = new AttributeIteratorWrapper(it);
        }

        @Override
        public Iterator<Attribute> iterator()
        {
            return myIter;
        }

        private Iterator myIter;
    }

    private static class AttributeIteratorWrapper
            implements Iterator<Attribute>
    {
        public AttributeIteratorWrapper(AxisIterator it)
        {
            myIter = it;
            myNext = (NodeInfo) it.next();
        }

        @Override
        public boolean hasNext()
        {
            return myNext != null;
        }

        @Override
        public Attribute next()
        {
            if ( myNext == null ) {
                // TODO: Throw an exception instead?
                return null;
            }
            Attribute a = new SaxonAttribute(myNext);
            myNext = (NodeInfo) myIter.next();
            return a;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException("remove() is not supported");
        }

        private AxisIterator myIter;
        private NodeInfo myNext;
    }

    private class ElemIterable
            implements Iterable<Element>
    {
        public ElemIterable(AxisIterator it)
        {
            myIter = new ElemIteratorWrapper(it);
        }

        @Override
        public Iterator<Element> iterator()
        {
            return myIter;
        }

        private Iterator myIter;
    }

    private class ElemIteratorWrapper
            implements Iterator<Element>
    {
        public ElemIteratorWrapper(AxisIterator it)
        {
            myIter = it;
            myNext = (NodeInfo) it.next();
        }

        @Override
        public boolean hasNext()
        {
            return myNext != null;
        }

        @Override
        public Element next()
        {
            if ( myNext == null ) {
                // TODO: Throw an exception instead?
                return null;
            }
            Element e;
            try {
                e = new SaxonElement(myNext, myCtxt);
            }
            catch ( HttpClientException ex ) {
                // because we're implementing the Iterator interface, we don't
                // have the choice but to throw a runtime exception, but we know
                // by construction this is not possible to arrive here (we've
                // just check nullness above, and we iterate only on elements,
                // those are the only two reasons the constructor can throw an
                // exception)
                throw new RuntimeException("[cannot happen] error building the saxon element", ex);
            }
            myNext = (NodeInfo) myIter.next();
            return e;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException("remove() is not supported");
        }

        private AxisIterator myIter;
        private NodeInfo myNext;
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
