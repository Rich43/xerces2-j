/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999,2000,2001 The Apache Software Foundation.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.impl.v2;

import org.apache.xerces.impl.v2.datatypes.*;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;

import java.io.IOException;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLChar;

import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.StringTokenizer;

/**
 * The DTD validator. The validator implements a document
 * filter: receiving document events from the scanner; validating
 * the content and structure; augmenting the InfoSet, if applicable;
 * and notifying the parser of the information resulting from the
 * validation process.
 * <p>
 * This component requires the following features and properties from the
 * component manager that uses it:
 * <ul>
 *  <li>http://xml.org/sax/features/validation</li>
 *  <li>http://apache.org/xml/properties/internal/symbol-table</li>
 *  <li>http://apache.org/xml/properties/internal/error-reporter</li>
 *  <li>http://apache.org/xml/properties/internal/entity-resolver</li>
 * </ul>
 *
 * @author Eric Ye, IBM
 * @author Stubs generated by DesignDoc on Mon Sep 11 11:10:57 PDT 2000
 * @author Andy Clark, IBM
 * @author Jeffrey Rodriguez IBM
 *
 * @version $Id$
 */
public class SchemaValidator
    implements XMLComponent, XMLDocumentHandler {

    //
    // Constants
    //

    // feature identifiers

    /** Feature identifier: validation. */
    protected static final String VALIDATION =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE;

    // property identifiers

    /** Property identifier: symbol table. */
    protected static final String SYMBOL_TABLE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;

    /** Property identifier: error reporter. */
    protected static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;

    /** Property identifier: entiry resolver. */
    protected static final String ENTITY_RESOLVER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY;

    // recognized features and properties

    /** Recognized features. */
    protected static final String[] RECOGNIZED_FEATURES = {
        VALIDATION,
    };

    /** Recognized properties. */
    protected static final String[] RECOGNIZED_PROPERTIES = {
        SYMBOL_TABLE,
        ERROR_REPORTER,
        ENTITY_RESOLVER,
    };

    //
    // Data
    //

    // features

    /** Validation. */
    protected boolean fValidation;

    // properties

    /** Symbol table. */
    protected SymbolTable fSymbolTable;

    /** Error reporter. */
    protected XMLErrorReporter fErrorReporter;

    /** Entity resolver */
    protected XMLEntityResolver fEntityResolver;

    // handlers

    /** Document handler. */
    protected XMLDocumentHandler fDocumentHandler;

    //
    // XMLComponent methods
    //

    /*
     * Resets the component. The component can query the component manager
     * about any features and properties that affect the operation of the
     * component.
     *
     * @param componentManager The component manager.
     *
     * @throws SAXException Thrown by component on finitialization error.
     *                      For example, if a feature or property is
     *                      required for the operation of the component, the
     *                      component manager may throw a
     *                      SAXNotRecognizedException or a
     *                      SAXNotSupportedException.
     */
    public void reset(XMLComponentManager componentManager)
        throws XMLConfigurationException {

        ownReset(componentManager);

    } // reset(XMLComponentManager)

    /**
     * Returns a list of feature identifiers that are recognized by
     * this component. This method may return null if no features
     * are recognized by this component.
     */
    public String[] getRecognizedFeatures() {
        return RECOGNIZED_FEATURES;
    } // getRecognizedFeatures():String[]

    /**
     * Sets the state of a feature. This method is called by the component
     * manager any time after reset when a feature changes state.
     * <p>
     * <strong>Note:</strong> Components should silently ignore features
     * that do not affect the operation of the component.
     *
     * @param featureId The feature identifier.
     * @param state     The state of the feature.
     *
     * @throws SAXNotRecognizedException The component should not throw
     *                                   this exception.
     * @throws SAXNotSupportedException The component should not throw
     *                                  this exception.
     */
    public void setFeature(String featureId, boolean state)
        throws XMLConfigurationException {
        if (featureId.equals(VALIDATION))
            fValidation = state;
    } // setFeature(String,boolean)

    /**
     * Returns a list of property identifiers that are recognized by
     * this component. This method may return null if no properties
     * are recognized by this component.
     */
    public String[] getRecognizedProperties() {
        return RECOGNIZED_PROPERTIES;
    } // getRecognizedProperties():String[]

    /**
     * Sets the value of a property. This method is called by the component
     * manager any time after reset when a property changes value.
     * <p>
     * <strong>Note:</strong> Components should silently ignore properties
     * that do not affect the operation of the component.
     *
     * @param propertyId The property identifier.
     * @param value      The value of the property.
     *
     * @throws SAXNotRecognizedException The component should not throw
     *                                   this exception.
     * @throws SAXNotSupportedException The component should not throw
     *                                  this exception.
     */
    public void setProperty(String propertyId, Object value)
        throws XMLConfigurationException {
    } // setProperty(String,Object)

    //
    // XMLDocumentSource methods
    //

    /**
     * Sets the document handler to receive information about the document.
     *
     * @param documentHandler The document handler.
     */
    public void setDocumentHandler(XMLDocumentHandler documentHandler) {
        fDocumentHandler = documentHandler;
    } // setDocumentHandler(XMLDocumentHandler)

    //
    // XMLDocumentHandler methods
    //

    /**
     * The start of the document.
     *
     * @param systemId The system identifier of the entity if the entity
     *                 is external, null otherwise.
     * @param encoding The auto-detected IANA encoding name of the entity
     *                 stream. This value will be null in those situations
     *                 where the entity encoding is not auto-detected (e.g.
     *                 internal entities or a document entity that is
     *                 parsed from a java.io.Reader).
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void startDocument(XMLLocator locator, String encoding)
        throws XNIException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.startDocument(locator, encoding);
        }

    } // startDocument(XMLLocator,String)

    /**
     * Notifies of the presence of an XMLDecl line in the document. If
     * present, this method will be called immediately following the
     * startDocument call.
     *
     * @param version    The XML version.
     * @param encoding   The IANA encoding name of the document, or null if
     *                   not specified.
     * @param standalone The standalone value, or null if not specified.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void xmlDecl(String version, String encoding, String standalone)
        throws XNIException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.xmlDecl(version, encoding, standalone);
        }

    } // xmlDecl(String,String,String)

    /**
     * Notifies of the presence of the DOCTYPE line in the document.
     *
     * @param rootElement The name of the root element.
     * @param publicId    The public identifier if an external DTD or null
     *                    if the external DTD is specified using SYSTEM.
     * @param systemId    The system identifier if an external DTD, null
     *                    otherwise.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void doctypeDecl(String rootElement, String publicId, String systemId)
        throws XNIException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.doctypeDecl(rootElement, publicId, systemId);
        }

    } // doctypeDecl(String,String,String)

    /**
     * The start of a namespace prefix mapping. This method will only be
     * called when namespace processing is enabled.
     *
     * @param prefix The namespace prefix.
     * @param uri    The URI bound to the prefix.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void startPrefixMapping(String prefix, String uri)
        throws XNIException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.startPrefixMapping(prefix, uri);
        }

    } // startPrefixMapping(String,String)

    /**
     * The start of an element.
     *
     * @param element    The name of the element.
     * @param attributes The element attributes.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void startElement(QName element, XMLAttributes attributes)
        throws XNIException {

        handleStartElement(element, attributes);
        if (fDocumentHandler != null) {
            fDocumentHandler.startElement(element, attributes);
        }

    } // startElement(QName,XMLAttributes)

    /**
     * An empty element.
     *
     * @param element    The name of the element.
     * @param attributes The element attributes.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void emptyElement(QName element, XMLAttributes attributes)
        throws XNIException {

        handleStartElement(element, attributes);
        handleEndElement(element);
        if (fDocumentHandler != null) {
            fDocumentHandler.emptyElement(element, attributes);
        }

    } // emptyElement(QName,XMLAttributes)

    /**
     * Character content.
     *
     * @param text The content.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void characters(XMLString text) throws XNIException {

        handleCharacters(text);

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.characters(text);
        }

    } // characters(XMLString)

    /**
     * Ignorable whitespace. For this method to be called, the document
     * source must have some way of determining that the text containing
     * only whitespace characters should be considered ignorable. For
     * example, the validator can determine if a length of whitespace
     * characters in the document are ignorable based on the element
     * content model.
     *
     * @param text The ignorable whitespace.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void ignorableWhitespace(XMLString text) throws XNIException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.ignorableWhitespace(text);
        }

    } // ignorableWhitespace(XMLString)

    /**
     * The end of an element.
     *
     * @param element The name of the element.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endElement(QName element) throws XNIException {

        handleEndElement(element);
        if (fDocumentHandler != null) {
            fDocumentHandler.endElement(element);
        }

    } // endElement(QName)

    /**
     * The end of a namespace prefix mapping. This method will only be
     * called when namespace processing is enabled.
     *
     * @param prefix The namespace prefix.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endPrefixMapping(String prefix) throws XNIException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.endPrefixMapping(prefix);
        }

    } // endPrefixMapping(String)

    /**
     * The start of a CDATA section.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void startCDATA() throws XNIException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.startCDATA();
        }

    } // startCDATA()

    /**
     * The end of a CDATA section.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endCDATA() throws XNIException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.endCDATA();
        }

    } // endCDATA()

    /**
     * The end of the document.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endDocument() throws XNIException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.endDocument();
        }

    } // endDocument()

    //
    // XMLDocumentHandler and XMLDTDHandler methods
    //

    /**
     * This method notifies of the start of an entity. The DTD has the
     * pseudo-name of "[dtd]; parameter entity names start with '%'; and
     * general entity names are just the entity name.
     * <p>
     * <strong>Note:</strong> Since the DTD is an entity, the handler
     * will be notified of the start of the DTD entity by calling the
     * startEntity method with the entity name "[dtd]" <em>before</em> calling
     * the startDTD method.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     *
     * @param name     The name of the entity.
     * @param publicId The public identifier of the entity if the entity
     *                 is external, null otherwise.
     * @param systemId The system identifier of the entity if the entity
     *                 is external, null otherwise.
     * @param baseSystemId The base system identifier of the entity if
     *                     the entity is external, null otherwise.
     * @param encoding The auto-detected IANA encoding name of the entity
     *                 stream. This value will be null in those situations
     *                 where the entity encoding is not auto-detected (e.g.
     *                 internal parameter entities).
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void startEntity(String name,
                            String publicId, String systemId,
                            String baseSystemId,
                            String encoding) throws XNIException {

        if (fDocumentHandler != null) {
            fDocumentHandler.startEntity(name, publicId, systemId,
                                         baseSystemId, encoding);
        }

    } // startEntity(String,String,String,String,String)

    /**
     * Notifies of the presence of a TextDecl line in an entity. If present,
     * this method will be called immediately following the startEntity call.
     * <p>
     * <strong>Note:</strong> This method will never be called for the
     * document entity; it is only called for external general entities
     * referenced in document content.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     *
     * @param version  The XML version, or null if not specified.
     * @param encoding The IANA encoding name of the entity.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void textDecl(String version, String encoding) throws XNIException {

        if (fDocumentHandler != null) {
            fDocumentHandler.textDecl(version, encoding);
        }

    } // textDecl(String,String)

    /**
     * A comment.
     *
     * @param text The text in the comment.
     *
     * @throws XNIException Thrown by application to signal an error.
     */
    public void comment(XMLString text) throws XNIException {

        if (fDocumentHandler != null) {
            fDocumentHandler.comment(text);
        }

    } // comment(XMLString)

    /**
     * A processing instruction. Processing instructions consist of a
     * target name and, optionally, text data. The data is only meaningful
     * to the application.
     * <p>
     * Typically, a processing instruction's data will contain a series
     * of pseudo-attributes. These pseudo-attributes follow the form of
     * element attributes but are <strong>not</strong> parsed or presented
     * to the application as anything other than text. The application is
     * responsible for parsing the data.
     *
     * @param target The target.
     * @param data   The data or null if none specified.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void processingInstruction(String target, XMLString data)
        throws XNIException {

        if (fDocumentHandler != null) {
            fDocumentHandler.processingInstruction(target, data);
        }

    } // processingInstruction(String,XMLString)

    /**
     * This method notifies the end of an entity. The DTD has the pseudo-name
     * of "[dtd]; parameter entity names start with '%'; and general entity
     * names are just the entity name.
     * <p>
     * <strong>Note:</strong> Since the DTD is an entity, the handler
     * will be notified of the end of the DTD entity by calling the
     * endEntity method with the entity name "[dtd]" <em>after</em> calling
     * the endDTD method.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     *
     * @param name The name of the entity.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endEntity(String name) throws XNIException {

        if (fDocumentHandler != null) {
            fDocumentHandler.endEntity(name);
        }

    } // endEntity(String)

    // constants

    static final int INITIAL_STACK_SIZE = 8;
    static final int INC_STACK_SIZE     = 8;

    //
    // Data
    //

    // some constants

    String URI_XSI;
    String XSI_SCHEMALOCACTION;
    String XSI_NONAMESPACESCHEMALOCACTION;
    String XSI_TYPE;
    String XSI_NIL;
    String URI_SCHEMAFORSCHEMA;

    // state

    /** Schema grammar resolver. */
    final XSGrammarResolver fGrammarResolver;

    /** Schema handler */
    final XSDHandler fSchemaHandler;

    /** Skip validation. */
    int fSkipValidationDepth;

    /** Element depth. */
    int fElementDepth;

    /** Child count. */
    int fChildCount;

    /** Element decl stack. */
    int[] fChildCountStack = new int[INITIAL_STACK_SIZE];

    /** Current element declaration. */
    XSElementDecl fCurrentElemDecl;

    /** Element decl stack. */
    XSElementDecl[] fElemDeclStack = new XSElementDecl[INITIAL_STACK_SIZE];

    /** Current type. */
    XSTypeDecl fCurrentType;

    /** type stack. */
    XSTypeDecl[] fTypeStack = new XSTypeDecl[INITIAL_STACK_SIZE];

    /** Current content model. */
    XSCMValidator fCurrentCM;

    /** Content model stack. */
    XSCMValidator[] fCMStack = new XSCMValidator[INITIAL_STACK_SIZE];

    /** the current state of the current content model */
    Object fCurrCMState;

    /** stack to hold content model states */
    Object[] fCMStateStack = new Object[INITIAL_STACK_SIZE];

    /** Temporary string buffers. */
    StringBuffer fBuffer;

    /** stack to hold string for all nodes */
    StringBuffer[] fBufferStack = new StringBuffer[INITIAL_STACK_SIZE];

    /**
     * This table has to be own by instance of XMLValidator and shared
     * among ID, IDREF and IDREFS.
     * <p>
     * <strong>Note:</strong> Only ID has read/write access.
     * <p>
     * <strong>Note:</strong> Should revisit and replace with a ligther
     * structure.
     */
    Hashtable fTableOfIDs = new Hashtable();
    Hashtable fTableOfIDRefs = new Hashtable();

    //
    // Constructors
    //

    /** Default constructor. */
    public SchemaValidator() {

        fGrammarResolver = new XSGrammarResolver();
        fSchemaHandler = new XSDHandler(fGrammarResolver);

    } // <init>()

    void ownReset(XMLComponentManager componentManager)
        throws XMLConfigurationException {

        try {
            fValidation = componentManager.getFeature(VALIDATION);
        }
        catch (XMLConfigurationException e) {
            fValidation = false;
        }

        // get needed components
        fErrorReporter = (XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER);

        SymbolTable symbolTable = (SymbolTable)componentManager.getProperty(SYMBOL_TABLE);
        if (symbolTable != fSymbolTable) {
            URI_XSI = symbolTable.addSymbol(SchemaSymbols.OURI_XSI);
            XSI_SCHEMALOCACTION = symbolTable.addSymbol(SchemaSymbols.OXSI_SCHEMALOCACTION);
            XSI_NONAMESPACESCHEMALOCACTION = symbolTable.addSymbol(SchemaSymbols.OXSI_NONAMESPACESCHEMALOCACTION);
            XSI_TYPE = symbolTable.addSymbol(SchemaSymbols.OXSI_TYPE);
            XSI_NIL = symbolTable.addSymbol(SchemaSymbols.OXSI_NIL);
            URI_SCHEMAFORSCHEMA = symbolTable.addSymbol(SchemaSymbols.OURI_SCHEMAFORSCHEMA);
        }
        fSymbolTable = symbolTable;

        fEntityResolver = (XMLEntityResolver)componentManager.getProperty(ENTITY_RESOLVER);
        if (fEntityResolver == null)
            fEntityResolver = new XMLEntityResolver() {
                // REVISIT: what's the default dehavior
                public XMLInputSource resolveEntity(String publicId, String systemId,
                                                    String baseSystemId)
                    throws XNIException, IOException {
                    return new XMLInputSource(null, systemId, baseSystemId);
                }
            };

        // clear grammars
        fGrammarResolver.reset();
        fGrammarResolver.putGrammar(URI_SCHEMAFORSCHEMA, SchemaGrammar.SG_SchemaNS);

        fSchemaHandler.reset(fErrorReporter, fEntityResolver, fSymbolTable);

        // initialize state
        fCurrentElemDecl = null;
        fCurrentType = null;
        fCurrentCM = null;
        fCurrCMState = null;
        fSkipValidationDepth = -1;
        fElementDepth = -1;
        fChildCount = 0;

        fTableOfIDs.clear();
        fTableOfIDRefs.clear();

    } // reset(XMLComponentManager)

    /** ensure element stack capacity */
    void ensureStackCapacity() {

        if (fElementDepth == fElemDeclStack.length) {
            int newSize = fElementDepth + INC_STACK_SIZE;
            int[] newArrayI = new int[newSize];
            System.arraycopy(fChildCountStack, 0, newArrayI, 0, newSize);
            fChildCountStack = newArrayI;
            XSElementDecl[] newArrayE = new XSElementDecl[newSize];
            System.arraycopy(fElemDeclStack, 0, newArrayE, 0, newSize);
            fElemDeclStack = newArrayE;
            XSTypeDecl[] newArrayT = new XSTypeDecl[newSize];
            System.arraycopy(fTypeStack, 0, newArrayT, 0, newSize);
            fTypeStack = newArrayT;
            XSCMValidator[] newArrayC = new XSCMValidator[newSize];
            System.arraycopy(fCMStack, 0, newArrayC, 0, newSize);
            fCMStack = newArrayC;
            StringBuffer[] newArrayB = new StringBuffer[newSize];
            System.arraycopy(fBufferStack, 0, newArrayB, 0, newSize);
            fBufferStack = newArrayB;
            Object[] newArrayO = new Object[newSize];
            System.arraycopy(fCMStateStack, 0, newArrayO, 0, newSize);
            fCMStateStack = newArrayO;
        }

    } // ensureStackCapacity

    //
    // Protected methods
    //

    /** Handle element. */
    void handleStartElement(QName element, XMLAttributes attributes) {

        // if we are in the content of "skip", then just skip this element
        if (fSkipValidationDepth >= 0) {
            fElementDepth++;
            return;
        }

        // if it's not the root element, we store
        // ElementDecl, ContentModel, ElementValue in the stacks
        if (fElementDepth != -1) {
            ensureStackCapacity();
            fChildCountStack[fElementDepth] = fChildCount+1;
            fChildCount = 0;
            fElemDeclStack[fElementDepth] = fCurrentElemDecl;
            fTypeStack[fElementDepth] = fCurrentType;
            fCMStack[fElementDepth] = fCurrentCM;
            fBufferStack[fElementDepth] = fBuffer;
        }

        // get xsi:schemaLocation and xsi:noNamespaceSchemaLocation attributes,
        // parser them to get the grammars
        // REVISIT: we'll defer this operation until there is a reference to
        //          a component from that namespace
        String sLocation = attributes.getValue(URI_XSI, XSI_SCHEMALOCACTION);
        String nsLocation = attributes.getValue(URI_XSI, XSI_NONAMESPACESCHEMALOCACTION);
        if (sLocation != null) {
            StringTokenizer t = new StringTokenizer(sLocation, " \n\t\r");
            String namespace, location;
            while (t.hasMoreTokens()) {
                namespace = t.nextToken ();
                if (!t.hasMoreTokens()) {
                    // REVISIT: report error for wrong number of uris
                    break;
                }
                location = t.nextToken();
                if (fGrammarResolver.getGrammar(namespace) == null)
                    fSchemaHandler.parseSchema(namespace, location);
            }
        }
        if (nsLocation != null) {
            if (fGrammarResolver.getGrammar(fSchemaHandler.EMPTY_STRING) == null)
                fSchemaHandler.parseSchema(fSchemaHandler.EMPTY_STRING, nsLocation);
        }

        // get the element decl for this element
        fCurrentElemDecl = null;

        XSWildcardDecl wildcard = null;
        // if there is a content model, then get the decl from that
        if (fCurrentCM != null) {
            Object decl = fCurrentCM.oneTransition(element, fCurrCMState);
            if (decl instanceof XSElementDecl) {
                fCurrentElemDecl = (XSElementDecl)decl;
            } else if (decl instanceof XSWildcardDecl) {
                wildcard = (XSWildcardDecl)decl;
            } else if (fCurrCMState != null) {
                // REVISIT: report error: invalid content
                reportGenericSchemaError("invlid content starting with element '"+element.rawname+"'");
                fCurrCMState = null;
            }
        }

        // save the current content model state in the stack
        if (fElementDepth != -1)
            fCMStateStack[fElementDepth] = fCurrCMState;

        // increase the element depth after we've saved all states for the
        // parent element
        fElementDepth++;

        // if the wildcard is skip, then return
        if (wildcard != null && wildcard.fPprocessContents == XSWildcardDecl.WILDCARD_SKIP) {
            fSkipValidationDepth = fElementDepth;
            return;
        }

        // try again to get the element decl
        if (fCurrentElemDecl == null) {
            // REVISIT: null or ""
            SchemaGrammar sGrammar = fGrammarResolver.getGrammar(element.uri == null ? "" : element.uri);
            if (sGrammar != null)
                fCurrentElemDecl = sGrammar.getGlobalElementDecl(element.localpart);
        }

        // get the type for the current element
        fCurrentType = null;
        if (fCurrentElemDecl != null)
            fCurrentType = fCurrentElemDecl.fType;

        // get type from xsi:type
        String xsiType = attributes.getValue(URI_XSI, XSI_TYPE);
        if (xsiType != null) {
            // REVISIT: bind namespace, get grammar, get type
            // report error if type not found.
        }

        // if the element decl is not found
        if (fCurrentType == null) {
            // if this is the root element, or wildcard = strict, report error
            if (fElementDepth == 0) {
                // REVISIT: report error, because it's root element
                reportGenericSchemaError("can't find decl for root element '"+element.rawname+"'");
            } else if (wildcard != null &&
                wildcard.fPprocessContents == XSWildcardDecl.WILDCARD_STRICT) {
                // REVISIT: report error, because wilcard = strict
                reportGenericSchemaError("can't find decl for strict wildcard '"+element.rawname+"'");
            }

            // no element decl found, have to skip this element
            fSkipValidationDepth = fElementDepth;
            return;
        }

        // then try to get the content model
        fCurrentCM = null;
        if (fCurrentType != null) {
            if ((fCurrentType.getXSType() & XSTypeDecl.COMPLEX_TYPE) != 0) {
                fCurrentCM = ((XSComplexTypeDecl)fCurrentType).getContentModel();
            }
        }

        // and get the initial content model state
        fCurrCMState = null;
        if (fCurrentCM != null)
            fCurrCMState = fCurrentCM.startContentModel();

        // and the buffer to hold the value of the element
        fBuffer = new StringBuffer();

        // REVISIT: get xsi:nil (was bind...)

        // REVISIT: validate attributes (was validateEle...)

    } // handleStartElement(QName,XMLAttributes,boolean)

    /** Handle end element. */
    void handleEndElement(QName element) {

        // if we are skipping, return
        if (fSkipValidationDepth >= 0) {
            if (fSkipValidationDepth == fElementDepth &&
                fSkipValidationDepth > 0) {
                fSkipValidationDepth = -1;
                fElementDepth--;
                fChildCount = fChildCountStack[fElementDepth];
                fCurrentElemDecl = fElemDeclStack[fElementDepth];
                fCurrentType = fTypeStack[fElementDepth];
                fCurrentCM = fCMStack[fElementDepth];
                fCurrCMState = fCMStateStack[fElementDepth];
                fBuffer = fBufferStack[fElementDepth];
            } else {
                fElementDepth--;
            }
            return;
        }

        if (fValidation) {
            // REVISIT: fCurrentElemDecl: fixed/default value; ...
            // REVISIT: fCurrentType: value content, element content
            if (fCurrentType != null) {
                if (fCurrentCM == null && fChildCount != 0) {
                    // the parent element doesn't expect any child element
                    // REVISIT: report an error
                    reportGenericSchemaError("element content doesn't allowed here");
                }
                if ((fCurrentType.getXSType() & XSTypeDecl.SIMPLE_TYPE) != 0) {
                    try {
                        DatatypeValidator dv = (DatatypeValidator)fCurrentType;
                        String content = XSAttributeChecker.normalize(fBuffer.toString(), dv.getWSFacet());
                        dv.validate(content, null);
                    } catch (InvalidDatatypeValueException e) {
                        //REVISIT
                        reportGenericSchemaError("datatype error: " + e.getMessage());
                    }
                } else {
                    if (fCurrentCM != null && !fCurrentCM.endContentModel(fCurrCMState)) {
                        XSComplexTypeDecl ctype = (XSComplexTypeDecl)fCurrentType;
                        // REVISIT: report error for not matching content
                        reportGenericSchemaError("the content of '"+element.rawname+"' doesn't match ("+ctype.fParticle.toString()+")");
                    }
                }
            }
        }

        // decrease element depth and restore states
        fElementDepth--;
        if (fElementDepth == -1) {
            if (fValidation) {
                try {
                    //Do final validation of IDREFS against IDs
                    // REVISIT: how to do it? new simpletype design?
                    IDREFDatatypeValidator.checkIdRefs(fTableOfIDs, fTableOfIDRefs);
                }
                catch (InvalidDatatypeValueException ex) {
                    fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                               "MSG_ELEMENT_WITH_ID_REQUIRED",
                                               new Object[]{ ex.getMessage()},
                                               XMLErrorReporter.SEVERITY_ERROR);
                }
            }
        } else {
            fChildCount = fChildCountStack[fElementDepth];
            fCurrentElemDecl = fElemDeclStack[fElementDepth];
            fCurrentType = fTypeStack[fElementDepth];
            fCurrentCM = fCMStack[fElementDepth];
            fCurrCMState = fCMStateStack[fElementDepth];
            fBuffer = fBufferStack[fElementDepth];
        }

    } // handleEndElement(QName,boolean)*/

    void handleCharacters(XMLString text) {
        if (fSkipValidationDepth >= 0)
            return;
        fBuffer.append(text.toString());
    } // handleCharacters(XMLString)

    void reportSchemaError(String key, Object[] arguments) {
        if (fValidation)
            fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                       key, arguments,
                                       XMLErrorReporter.SEVERITY_ERROR);
    }

    void reportGenericSchemaError(String msg) {
        if (fValidation)
            fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                       "General", new Object[]{msg},
                                       XMLErrorReporter.SEVERITY_ERROR);
    }

} // class XMLDTDValidator
