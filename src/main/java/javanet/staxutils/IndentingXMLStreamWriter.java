/*
 * Copyright (c) 2006, John Kristian
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *      *   Redistributions of source code must retain the above copyright
 *          notice, this list of conditions and the following disclaimer.
 *
 *      *   Redistributions in binary form must reproduce the above copyright
 *          notice, this list of conditions and the following disclaimer in the
 *          documentation and/or other materials provided with the distribution.
 *
 *      *   Neither the name of StAX-Utils nor the names of its contributors
 *          may be used to endorse or promote products derived from this
 *          software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package javanet.staxutils;

import javanet.staxutils.helpers.StreamWriterDelegate;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * A filter that indents an XML stream. To apply it, construct a filter that
 * contains another {@link XMLStreamWriter}, which you pass to the constructor.
 * Then call methods of the filter instead of the contained stream. For example:
 * 
 * <pre>
 * {@link XMLStreamWriter} stream = ...
 * stream = new {@link IndentingXMLStreamWriter}(stream);
 * stream.writeStartDocument();
 * ...
 * </pre>
 * 
 * <p>
 * The filter inserts characters to format the document as an outline, with
 * nested elements indented. Basically, it inserts a line break and whitespace
 * before:
 * <ul>
 * <li>each DTD, processing instruction or comment that's not preceded by data</li>
 * <li>each starting tag that's not preceded by data</li>
 * <li>each ending tag that's preceded by nested elements but not data</li>
 * </ul>
 * This works well with 'data-oriented' XML, wherein each element contains
 * either data or nested elements but not both. It can work badly with other
 * styles of XML. For example, the data in a 'mixed content' document are apt to
 * be polluted with indentation characters.
 * <p>
 * Indentation can be adjusted by setting the newLine and indent properties. But
 * set them to whitespace only, for best results. Non-whitespace is apt to cause
 * problems, for example when this class attempts to insert newLine before the
 * root element.
 * 
 * @author <a href="mailto:jk2006@engineer.com">John Kristian</a>
 */
public class IndentingXMLStreamWriter extends StreamWriterDelegate implements Indentation {

    public IndentingXMLStreamWriter(XMLStreamWriter out) {
        super(out);
    }

    /** How deeply nested the current scope is. The root element is depth 1. */
    private int depth = 0; // document scope

    /** stack[depth] indicates what's been written into the current scope. */
    private int[] stack = new int[] { 0, 0, 0, 0 }; // nothing written yet

    private static final int WROTE_MARKUP = 1;

    private static final int WROTE_DATA = 2;

    private String indent = DEFAULT_INDENT;

    private String newLine = NORMAL_END_OF_LINE;

    /** newLine followed by copies of indent. */
    private char[] linePrefix = null;

    public void setIndent(String indent) {
        if (!indent.equals(this.indent)) {
            this.indent = indent;
            linePrefix = null;
        }
    }

    public String getIndent() {
        return indent;
    }

    public void setNewLine(String newLine) {
        if (!newLine.equals(this.newLine)) {
            this.newLine = newLine;
            linePrefix = null;
        }
    }

    /**
     * @return System.getProperty("line.separator"); or
     *         {@link #NORMAL_END_OF_LINE} if that fails.
     */
    public static String getLineSeparator() {
        try {
            return System.getProperty("line.separator");
        } catch (SecurityException ignored) {
        }
        return NORMAL_END_OF_LINE;
    }

    public String getNewLine() {
        return newLine;
    }

    public void writeStartDocument() throws XMLStreamException {
        beforeMarkup();
        out.writeStartDocument();
        afterMarkup();
    }

    public void writeStartDocument(String version) throws XMLStreamException {
        beforeMarkup();
        out.writeStartDocument(version);
        afterMarkup();
    }

    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        beforeMarkup();
        out.writeStartDocument(encoding, version);
        afterMarkup();
    }

    public void writeDTD(String dtd) throws XMLStreamException {
        beforeMarkup();
        out.writeDTD(dtd);
        afterMarkup();
    }

    public void writeProcessingInstruction(String target) throws XMLStreamException {
        beforeMarkup();
        out.writeProcessingInstruction(target);
        afterMarkup();
    }

    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        beforeMarkup();
        out.writeProcessingInstruction(target, data);
        afterMarkup();
    }

    public void writeComment(String data) throws XMLStreamException {
        beforeMarkup();
        out.writeComment(data);
        afterMarkup();
    }

    public void writeEmptyElement(String localName) throws XMLStreamException {
        beforeMarkup();
        out.writeEmptyElement(localName);
        afterMarkup();
    }

    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        beforeMarkup();
        out.writeEmptyElement(namespaceURI, localName);
        afterMarkup();
    }

    public void writeEmptyElement(String prefix, String localName, String namespaceURI)
            throws XMLStreamException {
        beforeMarkup();
        out.writeEmptyElement(prefix, localName, namespaceURI);
        afterMarkup();
    }

    public void writeStartElement(String localName) throws XMLStreamException {
        beforeStartElement();
        out.writeStartElement(localName);
        afterStartElement();
    }

    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        beforeStartElement();
        out.writeStartElement(namespaceURI, localName);
        afterStartElement();
    }

    public void writeStartElement(String prefix, String localName, String namespaceURI)
            throws XMLStreamException {
        beforeStartElement();
        out.writeStartElement(prefix, localName, namespaceURI);
        afterStartElement();
    }

    public void writeCharacters(String text) throws XMLStreamException {
        out.writeCharacters(text);
        afterData();
    }

    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        out.writeCharacters(text, start, len);
        afterData();
    }

    public void writeCData(String data) throws XMLStreamException {
        out.writeCData(data);
        afterData();
    }

    public void writeEntityRef(String name) throws XMLStreamException {
        out.writeEntityRef(name);
        afterData();
    }

    public void writeEndElement() throws XMLStreamException {
        beforeEndElement();
        out.writeEndElement();
        afterEndElement();
    }

    public void writeEndDocument() throws XMLStreamException {
        try {
            while (depth > 0) {
                writeEndElement(); // indented
            }
        } catch (Exception ignored) {
        }
        out.writeEndDocument();
        afterEndDocument();
    }

    /** Prepare to write markup, by writing a new line and indentation. */
    protected void beforeMarkup() {
        int soFar = stack[depth];
        if ((soFar & WROTE_DATA) == 0 // no data in this scope
                && (depth > 0 || soFar != 0)) // not the first line
        {
            try {
                writeNewLine(depth);
                if (depth > 0 && getIndent().length() > 0) {
                    afterMarkup(); // indentation was written
                }
            } catch (Exception e) {
            }
        }
    }

    /** Note that markup or indentation was written. */
    protected void afterMarkup() {
        stack[depth] |= WROTE_MARKUP;
    }

    /** Note that data were written. */
    protected void afterData() {
        stack[depth] |= WROTE_DATA;
    }

    /** Prepare to start an element, by allocating stack space. */
    protected void beforeStartElement() {
        beforeMarkup();
        if (stack.length <= depth + 1) {
            // Allocate more space for the stack:
            int[] newStack = new int[stack.length * 2];
            System.arraycopy(stack, 0, newStack, 0, stack.length);
            stack = newStack;
        }
        stack[depth + 1] = 0; // nothing written yet
    }

    /** Note that an element was started. */
    protected void afterStartElement() {
        afterMarkup();
        ++depth;
    }

    /** Prepare to end an element, by writing a new line and indentation. */
    protected void beforeEndElement() {
        if (depth > 0 && stack[depth] == WROTE_MARKUP) { // but not data
            try {
                writeNewLine(depth - 1);
            } catch (Exception ignored) {
            }
        }
    }

    /** Note that an element was ended. */
    protected void afterEndElement() {
        if (depth > 0) {
            --depth;
        }
    }

    /** Note that a document was ended. */
    protected void afterEndDocument() {
        if (stack[depth = 0] == WROTE_MARKUP) { // but not data
            try {
                writeNewLine(0);
            } catch (Exception ignored) {
            }
        }
        stack[depth] = 0; // start fresh
    }

    /** Write a line separator followed by indentation. */
    protected void writeNewLine(int indentation) throws XMLStreamException {
        final int newLineLength = getNewLine().length();
        final int prefixLength = newLineLength + (getIndent().length() * indentation);
        if (prefixLength > 0) {
            if (linePrefix == null) {
                linePrefix = (getNewLine() + getIndent()).toCharArray();
            }
            while (prefixLength > linePrefix.length) {
                // make linePrefix longer:
                char[] newPrefix = new char[newLineLength
                        + ((linePrefix.length - newLineLength) * 2)];
                System.arraycopy(linePrefix, 0, newPrefix, 0, linePrefix.length);
                System.arraycopy(linePrefix, newLineLength, newPrefix, linePrefix.length,
                        linePrefix.length - newLineLength);
                linePrefix = newPrefix;
            }
            out.writeCharacters(linePrefix, 0, prefixLength);
        }
    }

}
