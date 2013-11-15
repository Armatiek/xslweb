/****************************************************************************/
/*  File:       SaxonElement.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-02-21                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.zip.saxon;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.TreeReceiver;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.om.*;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.HexBinaryValue;
import net.sf.saxon.value.Whitespace;
import org.expath.zip.Attribute;
import org.expath.zip.Element;
import org.expath.zip.Serialization;
import org.expath.zip.ZipConstants;
import org.expath.zip.ZipException;
import org.expath.zip.ZipRuntimeException;

/**
 * The implementation for Saxon of an abstract element.
 *
 * @author Florent Georges
 * @date   2011-02-21
 */
public class SaxonElement
        implements Element
{
    public SaxonElement(NodeInfo node)
    {
        // TODO: Should we perform some checks (is it non-null?, is it an
        // element node?, etc.)  Or is it guaranteed by construction?
        myNode = node;
    }

    @Override
    public String getBaseUri()
    {
        return myNode.getBaseURI();
    }

    @Override
    public String getLocalName()
    {
        return myNode.getLocalPart();
    }

    @Override
    public String formatName()
    {
        return myNode.getDisplayName();
    }

    @Override
    public Iterable<Attribute> attributes()
    {
        AxisIterator it = myNode.iterateAxis(Axis.ATTRIBUTE);
        return new AttributeIterable(it);
    }

    @Override
    public Iterable<Element> entries()
    {
        AxisIterator it = myNode.iterateAxis(Axis.CHILD);
        return new EntryIterable(it);
    }

    @Override
    public Serialization makeSerialization()
            throws ZipException
    {
        return new SaxonSerialization();
    }

    @Override
    public void serialize(OutputStream out, Serialization serial)
            throws ZipException
                 , IOException
    {
        AxisIterator children = myNode.iterateAxis(Axis.CHILD);
        try {
            if ( "base64".equals(serial.getMethod()) ) {
                Base64BinaryValue val = new Base64BinaryValue(children.getStringValue());
                out.write(val.getBinaryValue());
            }
            else if ( "hex".equals(serial.getMethod()) ) {
                HexBinaryValue val = new HexBinaryValue(children.getStringValue());
                out.write(val.getBinaryValue());
            }
            else {
                Configuration         config   = myNode.getConfiguration();
                SerializerFactory     factory  = config.getSerializerFactory();
                PipelineConfiguration pipe     = config.makePipelineConfiguration();
                Result                result   = new StreamResult(out);
                Properties            options  = serial.getOutputOptions();
                Receiver              base     = factory.getReceiver(result, pipe, options);
                Receiver              receiver = new ZipNamespaceFilter(base);
                TreeReceiver          tr       = new TreeReceiver(receiver);
                tr.open();
                while ( true ) {
                    Item item = children.next();
                    if ( item == null ) {
                        break;
                    }
                    tr.append(item, 0, NodeInfo.ALL_NAMESPACES);
                }
                tr.close();
            }
        }
        catch ( XPathException ex ) {
            throw new ZipException("Error serializing the source", ex);
        }
    }

    private NodeInfo myNode;

    /**
     * Filters out the ZIP namespace binding.
     */
    private static class ZipNamespaceFilter
            extends ProxyReceiver
    {
        public ZipNamespaceFilter(Receiver base)
        {
            super(base);
        }

        @Override
        public void namespace(NamespaceBinding ns, int props)
                throws XPathException
        {
            String uri = ns.getURI();
            // forward the namespace binding event only if it is not the ZIP namespace
            if ( ! ZipConstants.ZIP_NS_URI.equals(uri) ) {
                super.namespace(ns, props);
            }
        }
    }

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

    private static class EntryIterable
            implements Iterable<Element>
    {
        public EntryIterable(AxisIterator it)
        {
            myIter = new EntryIteratorWrapper(it);
        }

        @Override
        public Iterator<Element> iterator()
        {
            return myIter;
        }

        private Iterator myIter;
    }

    private static class EntryIteratorWrapper
            implements Iterator<Element>
    {
        public EntryIteratorWrapper(AxisIterator it)
        {
            myIter = it;
            myNext = (NodeInfo) it.next();
            ensureNextEntry();
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
            Element e = new SaxonElement(myNext);
            myNext = (NodeInfo) myIter.next();
            ensureNextEntry();
            return e;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException("remove() is not supported");
        }

        private void ensureNextEntry()
                throws ZipRuntimeException
        {
            while ( myNext != null && ! isEntry(myNext) ) {
                myNext = (NodeInfo) myIter.next();
            }
        }

        // if unknown child of zip:file, throw an error, if not then return true if
        // it is an entry element, or false if it is an element to ignore (in another
        // namespace for instance)
        private boolean isEntry(NodeInfo node)
                throws ZipRuntimeException
        {
            String local = node.getLocalPart();
            int kind = node.getNodeKind();
            if ( kind == Type.TEXT ) {
                if ( ! Whitespace.isWhite(node.getStringValueCS()) ) {
                    throw new ZipRuntimeException("Non-whitespace text nodes are not allowed");
                }
                return false;
            }
            else if ( kind == Type.COMMENT || kind == Type.PROCESSING_INSTRUCTION ) {
                // ignore comment and PI nodes
                return false;
            }
            else if ( kind != Type.ELEMENT ) {
                // namespace, document and attribute nodes cannot be no the child axis
                throw new ZipRuntimeException("Not an element: could not happen");
            }
            else if ( "".equals(node.getURI()) ) {
                // elements in no namespace are an error
                throw new ZipRuntimeException("Element in no namespace: " + local);
            }
            else if ( ! ZipConstants.ZIP_NS_URI.equals(node.getURI()) ) {
                // ignore elements in other namespaces
                return false;
            }
            else if ( "entry".equals(local) || "dir".equals(local) ) {
                return true;
            }
            else {
                throw new ZipRuntimeException("Unknown element: " + local);
            }
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
