/****************************************************************************/
/*  File:       XMLStreamHelper.java                                        */
/*  Author:     F. Georges                                                  */
/*  Company:    H2O Consulting                                              */
/*  Date:       2010-05-13                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo.parser;

import java.io.InputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.expath.pkg.repo.PackageException;

/**
 * Helper class for the package {@link javax.xml.stream}.
 *
 * @author Florent Georges - H2O Consulting
 * @date   2010-05-13
 */
public class XMLStreamHelper
{
    public XMLStreamHelper(String targetNS)
    {
        myTargetNS = targetNS;
    }

    public String getTargetNs()
    {
        return myTargetNS;
    }

    public XMLStreamReader makeDescriptorParser(InputStream desc)
            throws PackageException
    {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader  = factory.createXMLStreamReader(desc);
            StreamFilter    filter  = new WhitespaceFilter();
            return factory.createFilteredReader(reader, filter);
        }
        catch ( XMLStreamException ex ) {
            throw new PackageException("Error parsing the package descriptor", ex);
        }
    }

    public void ensureDocument(XMLStreamReader parser)
            throws PackageException
    {
        int event = parser.getEventType();
        if ( event != XMLStreamConstants.START_DOCUMENT ) {
            stateError("The current event is not START_DOCUMENT", event);
        }
    }

    public boolean isNextElement(XMLStreamReader parser, String local_name)
            throws PackageException
    {
        try {
            parser.next();
            return isElement(parser, local_name);
        }
        catch ( XMLStreamException ex ) {
            throw new PackageException("Error parsing the package descriptor", ex);
        }
    }

    public void ensureNextElement(XMLStreamReader parser, String local_name)
            throws PackageException
    {
        try {
            parser.next();
            ensureElement(parser, local_name);
        }
        catch ( XMLStreamException ex ) {
            throw new PackageException("Error parsing the package descriptor", ex);
        }
    }

    public boolean isElement(XMLStreamReader parser, String local_name)
            throws PackageException
    {
        int event = parser.getEventType();
        if ( event != XMLStreamConstants.START_ELEMENT ) {
            return false;
        }
        if ( ! new QName(myTargetNS, local_name).equals(parser.getName()) ) {
            return false;
        }
        return true;
    }

    public void ensureElement(XMLStreamReader parser, String local_name)
            throws PackageException
    {
        int event = parser.getEventType();
        if ( event != XMLStreamConstants.START_ELEMENT ) {
            stateError("The current event is not START_ELEMENT", event);
        }
        if ( ! new QName(myTargetNS, local_name).equals(parser.getName()) ) {
            throw new PackageException("The element is not a pkg:" + local_name);
        }
    }

    public String getEventName(int event)
            throws PackageException
    {
        switch ( event ) {
            case XMLStreamConstants.ATTRIBUTE:
                return "ATTRIBUTE";
            case XMLStreamConstants.CDATA:
                return "CDATA";
            case XMLStreamConstants.CHARACTERS:
                return "CHARACTERS";
            case XMLStreamConstants.COMMENT:
                return "COMMENT";
            case XMLStreamConstants.DTD:
                return "DTD";
            case XMLStreamConstants.END_DOCUMENT:
                return "END_DOCUMENT";
            case XMLStreamConstants.END_ELEMENT:
                return "END_ELEMENT";
            case XMLStreamConstants.ENTITY_DECLARATION:
                return "ENTITY_DECLARATION";
            case XMLStreamConstants.ENTITY_REFERENCE:
                return "ENTITY_REFERENCE";
            case XMLStreamConstants.NAMESPACE:
                return "NAMESPACE";
            case XMLStreamConstants.NOTATION_DECLARATION:
                return "NOTATION_DECLARATION";
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                return "PROCESSING_INSTRUCTION";
            case XMLStreamConstants.SPACE:
                return "SPACE";
            case XMLStreamConstants.START_DOCUMENT:
                return "START_DOCUMENT";
            case XMLStreamConstants.START_ELEMENT:
                return "START_ELEMENT";
            default:
                throw new PackageException("Unknown event code: " + event);
        }
    }

    public String getAttributeValue(XMLStreamReader parser, String name)
            throws PackageException
    {
        String value = parser.getAttributeValue(null, name);
        if ( value == null ) {
            throw new PackageException("The element does not have an attribute " + name);
        }
        return value;
    }

    public String getElementValue(XMLStreamReader parser)
            throws PackageException
    {
        return accumulateStringValue(parser);
    }

    private String accumulateStringValue(XMLStreamReader parser)
            throws PackageException
    {
        StringBuilder buf = new StringBuilder();
        try {
            int event;
            while ( (event = parser.next()) != XMLStreamConstants.END_ELEMENT ) {
                if ( event != XMLStreamConstants.CHARACTERS ) {
                    stateError("Current event is not CHARACTERS", event);
                }
                buf.append(parser.getTextCharacters(), parser.getTextStart(), parser.getTextLength());
            }
        }
        catch ( XMLStreamException ex ) {
            throw new PackageException("Error parsing the package descriptor", ex);
        }
        return buf.toString();
    }

    /**
     * The parser's current event must be START_ELEMENT.
     *
     * Ignore all this element end stops on the corresponding END_ELEMENT event.
     */
    public void ignoreElement(XMLStreamReader parser)
            throws PackageException
    {
        // ignore this element entirely
        int opened = 1;
        while ( opened > 0 ) {
            try {
                parser.next();
                if ( parser.getEventType() == XMLStreamConstants.START_ELEMENT ) {
                    ++opened;
                }
                else if ( parser.getEventType() == XMLStreamConstants.END_ELEMENT ) {
                    --opened;
                }
            }
            catch ( XMLStreamException ex ) {
                throw new PackageException("Error reading the package descriptor", ex);
            }
        }
    }

    public boolean stateError(String msg, int event)
            throws PackageException
    {
        String event_name = getEventName(event);
        throw new PackageException(msg + " (" + event_name + ")");
    }

    private String myTargetNS;

    /**
     * Filter out whitespace text nodes, as well as comments and PIs.
     *
     * TODO: Mmh... Could maybe use nextTag() instead of next() on the parser
     * in order to achieve the same purpose?
     */
    private static class WhitespaceFilter
            implements StreamFilter
    {
        @Override
        public boolean accept(XMLStreamReader parser)
        {
            int event = parser.getEventType();
            return event != XMLStreamConstants.COMMENT
              &&   event != XMLStreamConstants.PROCESSING_INSTRUCTION
              &&   event != XMLStreamConstants.SPACE
              && ( event != XMLStreamConstants.CHARACTERS || ! parser.isWhiteSpace() );
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
