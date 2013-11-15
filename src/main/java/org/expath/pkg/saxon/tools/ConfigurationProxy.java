/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.expath.pkg.saxon.tools;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.FilterFactory;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceReceiver;
import net.sf.saxon.expr.CollationMap;
import net.sf.saxon.expr.PendingUpdateList;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Debugger;
import net.sf.saxon.expr.instruct.DocumentInstr;
import net.sf.saxon.expr.instruct.ElementCreator;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.parser.PathMap.PathMapRoot;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.functions.IntegratedFunctionLibrary;
import net.sf.saxon.functions.VendorFunctionLibrary;
import net.sf.saxon.lib.*;
import net.sf.saxon.om.*;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.serialize.charcode.CharacterSetFactory;
import net.sf.saxon.trans.CompilerInfo;
import net.sf.saxon.trans.DynamicLoader;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.DocumentNumberAllocator;
import net.sf.saxon.type.SchemaDeclaration;
import net.sf.saxon.type.SchemaException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.ValidationException;
import org.xml.sax.XMLReader;

/**
 *
 * @author georgfl
 */
@Deprecated
public class ConfigurationProxy
        extends Configuration
{
    public ConfigurationProxy(Configuration config)
    {
        myConfig = config;
    }

    @Override
    public synchronized void reuseStyleParser(XMLReader parser)
    {
        myConfig.reuseStyleParser(parser);
    }

    @Override
    public String getStyleParserClass()
    {
        System.err.println("WTF?");
        if ( myConfig == null ) {
            return super.getStyleParserClass();
        }
        return myConfig.getStyleParserClass();
    }

    @Override
    public synchronized XMLReader getStyleParser() throws TransformerFactoryConfigurationError {
        System.err.println("WTF? yo");
        if ( myConfig == null ) {
            return super.getStyleParser();
        }
        return myConfig.getStyleParser();
    }

    @Override
    public SimpleType validateAttribute(int nameCode, CharSequence value, int validation) throws ValidationException {
        if ( myConfig == null ) {
            return super.validateAttribute(nameCode, value, validation);
        }
        return myConfig.validateAttribute(nameCode, value, validation);
    }

    @Override
    public boolean useTypedValueCache() {
        if ( myConfig == null ) {
            return super.useTypedValueCache();
        }
        return myConfig.useTypedValueCache();
    }

    @Override
    public NodeInfo unravel(Source source) {
        if ( myConfig == null ) {
            return super.unravel(source);
        }
        return myConfig.unravel(source);
    }

    @Override
    public void setXMLVersion(int version) {
        if ( myConfig == null ) {
            super.setXMLVersion(version);
            return;
        }
        myConfig.setXMLVersion(version);
    }

    @Override
    public void setXIncludeAware(boolean state) {
        if ( myConfig == null ) {
            super.setXIncludeAware(state);
            return;
        }
        myConfig.setXIncludeAware(state);
    }

    @Override
    public void setVersionWarning(boolean warn) {
        if ( myConfig == null ) {
            super.setVersionWarning(warn);
            return;
        }
        myConfig.setVersionWarning(warn);
    }

    @Override
    public void setValidationWarnings(boolean warn) {
        if ( myConfig == null ) {
            super.setValidationWarnings(warn);
            return;
        }
        myConfig.setValidationWarnings(warn);
    }

    @Override
    public void setValidation(boolean validation) {
        if ( myConfig == null ) {
            super.setValidation(validation);
            return;
        }
        myConfig.setValidation(validation);
    }

    @Override
    public void setURIResolver(URIResolver resolver) {
        if ( myConfig == null ) {
            super.setURIResolver(resolver);
            return;
        }
        myConfig.setURIResolver(resolver);
    }

    @Override
    public void setTreeModel(int treeModel) {
        if ( myConfig == null ) {
            super.setTreeModel(treeModel);
            return;
        }
        myConfig.setTreeModel(treeModel);
    }

    @Override
    public void setTraceListenerClass(String className) {
        if ( myConfig == null ) {
            super.setTraceListenerClass(className);
            return;
        }
        myConfig.setTraceListenerClass(className);
    }

    @Override
    public void setTraceListener(TraceListener traceListener) {
        if ( myConfig == null ) {
            super.setTraceListener(traceListener);
            return;
        }
        myConfig.setTraceListener(traceListener);
    }

    @Override
    public void setTraceExternalFunctions(boolean traceExternalFunctions) {
        if ( myConfig == null ) {
            super.setTraceExternalFunctions(traceExternalFunctions);
            return;
        }
        myConfig.setTraceExternalFunctions(traceExternalFunctions);
    }

    @Override
    public void setTiming(boolean timing) {
        if ( myConfig == null ) {
            super.setTiming(timing);
            return;
        }
        myConfig.setTiming(timing);
    }

    @Override
    public void setStyleParserClass(String parser) {
        if ( myConfig == null ) {
            super.setStyleParserClass(parser);
            return;
        }
        myConfig.setStyleParserClass(parser);
    }

    @Override
    public void setStripsWhiteSpace(int kind) {
        if ( myConfig == null ) {
            super.setStripsWhiteSpace(kind);
            return;
        }
        myConfig.setStripsWhiteSpace(kind);
    }

    @Override
    public void setStripsAllWhiteSpace(boolean stripsAllWhiteSpace) {
        if ( myConfig == null ) {
            super.setStripsAllWhiteSpace(stripsAllWhiteSpace);
            return;
        }
        myConfig.setStripsAllWhiteSpace(stripsAllWhiteSpace);
    }

    @Override
    public void setSourceResolver(SourceResolver resolver) {
        if ( myConfig == null ) {
            super.setSourceResolver(resolver);
            return;
        }
        myConfig.setSourceResolver(resolver);
    }

    @Override
    public void setSourceParserClass(String sourceParserClass) {
        if ( myConfig == null ) {
            super.setSourceParserClass(sourceParserClass);
            return;
        }
        myConfig.setSourceParserClass(sourceParserClass);
    }

    @Override
    public void setSerializerFactory(SerializerFactory factory) {
        if ( myConfig == null ) {
            super.setSerializerFactory(factory);
            return;
        }
        myConfig.setSerializerFactory(factory);
    }

    @Override
    public void setSchemaValidationMode(int validationMode) {
        if ( myConfig == null ) {
            super.setSchemaValidationMode(validationMode);
            return;
        }
        myConfig.setSchemaValidationMode(validationMode);
    }

    @Override
    public void setSchemaURIResolver(SchemaURIResolver resolver) {
        if ( myConfig == null ) {
            super.setSchemaURIResolver(resolver);
            return;
        }
        myConfig.setSchemaURIResolver(resolver);
    }

    @Override
    public void setRetainDTDAttributeTypes(boolean useTypes) throws TransformerFactoryConfigurationError {
        if ( myConfig == null ) {
            super.setRetainDTDAttributeTypes(useTypes);
            return;
        }
        myConfig.setRetainDTDAttributeTypes(useTypes);
    }

    @Override
    public void setRecoveryPolicy(int recoveryPolicy) {
        if ( myConfig == null ) {
            super.setRecoveryPolicy(recoveryPolicy);
            return;
        }
        myConfig.setRecoveryPolicy(recoveryPolicy);
    }

    @Override
    public void setProcessor(Object processor) {
        if ( myConfig == null ) {
            super.setProcessor(processor);
            return;
        }
        myConfig.setProcessor(processor);
    }

    @Override
    public void setParameterizedURIResolver() {
        if ( myConfig == null ) {
            super.setParameterizedURIResolver();
            return;
        }
        myConfig.setParameterizedURIResolver();
    }

    @Override
    public void setOutputURIResolver(OutputURIResolver outputURIResolver) {
        if ( myConfig == null ) {
            super.setOutputURIResolver(outputURIResolver);
            return;
        }
        myConfig.setOutputURIResolver(outputURIResolver);
    }

    @Override
    public void setOptimizerTracing(boolean trace) {
        if ( myConfig == null ) {
            super.setOptimizerTracing(trace);
            return;
        }
        myConfig.setOptimizerTracing(trace);
    }

    @Override
    public void setNamePool(NamePool targetNamePool) {
        if ( myConfig == null ) {
            super.setNamePool(targetNamePool);
            return;
        }
        myConfig.setNamePool(targetNamePool);
    }

    @Override
    public void setMultiThreading(boolean multithreading) {
        if ( myConfig == null ) {
            super.setMultiThreading(multithreading);
            return;
        }
        myConfig.setMultiThreading(multithreading);
    }

    @Override
    public void setModuleURIResolver(String className) throws TransformerException {
        if ( myConfig == null ) {
            super.setModuleURIResolver(className);
            return;
        }
        myConfig.setModuleURIResolver(className);
    }

    @Override
    public void setModuleURIResolver(ModuleURIResolver resolver) {
        if ( myConfig == null ) {
            super.setModuleURIResolver(resolver);
            return;
        }
        myConfig.setModuleURIResolver(resolver);
    }

    @Override
    public void setMessageEmitterClass(String messageReceiverClassName) {
        if ( myConfig == null ) {
            super.setMessageEmitterClass(messageReceiverClassName);
            return;
        }
        myConfig.setMessageEmitterClass(messageReceiverClassName);
    }

    @Override
    public void setLocalizerFactory(LocalizerFactory factory) {
        if ( myConfig == null ) {
            super.setLocalizerFactory(factory);
            return;
        }
        myConfig.setLocalizerFactory(factory);
    }

    @Override
    public void setLineNumbering(boolean lineNumbering) {
        if ( myConfig == null ) {
            super.setLineNumbering(lineNumbering);
            return;
        }
        myConfig.setLineNumbering(lineNumbering);
    }

    @Override
    public void setLazyConstructionMode(boolean lazy) {
        if ( myConfig == null ) {
            super.setLazyConstructionMode(lazy);
            return;
        }
        myConfig.setLazyConstructionMode(lazy);
    }

    @Override
    public void setHostLanguage(int hostLanguage) {
        if ( myConfig == null ) {
            super.setHostLanguage(hostLanguage);
            return;
        }
        myConfig.setHostLanguage(hostLanguage);
    }

    @Override
    public void setExpandAttributeDefaults(boolean expand) {
        if ( myConfig == null ) {
            super.setExpandAttributeDefaults(expand);
            return;
        }
        myConfig.setExpandAttributeDefaults(expand);
    }

    @Override
    public void setErrorListener(ErrorListener listener) {
        if ( myConfig == null ) {
            super.setErrorListener(listener);
            return;
        }
        myConfig.setErrorListener(listener);
    }

    @Override
    public void setDynamicLoader(DynamicLoader dynamicLoader) {
        if ( myConfig == null ) {
            super.setDynamicLoader(dynamicLoader);
            return;
        }
        myConfig.setDynamicLoader(dynamicLoader);
    }

    @Override
    public void setDocumentNumberAllocator(DocumentNumberAllocator allocator) {
        if ( myConfig == null ) {
            super.setDocumentNumberAllocator(allocator);
            return;
        }
        myConfig.setDocumentNumberAllocator(allocator);
    }

    @Override
    public void setDefaultSerializationProperties(Properties props) {
        if ( myConfig == null ) {
            super.setDefaultSerializationProperties(props);
            return;
        }
        myConfig.setDefaultSerializationProperties(props);
    }

    @Override
    public void setDefaultLanguage(String language) {
        if ( myConfig == null ) {
            super.setDefaultLanguage(language);
            return;
        }
        myConfig.setDefaultLanguage(language);
    }

    @Override
    public void setDefaultCountry(String country) {
        if ( myConfig == null ) {
            super.setDefaultCountry(country);
            return;
        }
        myConfig.setDefaultCountry(country);
    }

    @Override
    public void setDefaultCollection(String uri) {
        if ( myConfig == null ) {
            super.setDefaultCollection(uri);
            return;
        }
        myConfig.setDefaultCollection(uri);
    }

    @Override
    public void setDebugger(Debugger debugger) {
        if ( myConfig == null ) {
            super.setDebugger(debugger);
            return;
        }
        myConfig.setDebugger(debugger);
    }

    @Override
    public void setDOMLevel(int level) {
        if ( myConfig == null ) {
            super.setDOMLevel(level);
            return;
        }
        myConfig.setDOMLevel(level);
    }

    @Override
    public void setConfigurationProperty(String name, Object value) {
        if ( myConfig == null ) {
            super.setConfigurationProperty(name, value);
            return;
        }
        myConfig.setConfigurationProperty(name, value);
    }

    @Override
    public void setCompileWithTracing(boolean trace) {
        if ( myConfig == null ) {
            super.setCompileWithTracing(trace);
            return;
        }
        myConfig.setCompileWithTracing(trace);
    }

    @Override
    public void setCollectionURIResolver(CollectionURIResolver resolver) {
        if ( myConfig == null ) {
            super.setCollectionURIResolver(resolver);
            return;
        }
        myConfig.setCollectionURIResolver(resolver);
    }

    @Override
    public void setCollationURIResolver(CollationURIResolver resolver) {
        if ( myConfig == null ) {
            super.setCollationURIResolver(resolver);
            return;
        }
        myConfig.setCollationURIResolver(resolver);
    }

    @Override
    public void setAllowExternalFunctions(boolean allowExternalFunctions) {
        if ( myConfig == null ) {
            super.setAllowExternalFunctions(allowExternalFunctions);
            return;
        }
        myConfig.setAllowExternalFunctions(allowExternalFunctions);
    }

    @Override
    public void sealNamespace(String namespace) {
        if ( myConfig == null ) {
            super.sealNamespace(namespace);
            return;
        }
        myConfig.sealNamespace(namespace);
    }

    @Override
    public synchronized void reuseSourceParser(XMLReader parser) {
        if ( myConfig == null ) {
            super.reuseSourceParser(parser);
            return;
        }
        myConfig.reuseSourceParser(parser);
    }

    @Override
    public Source resolveSource(Source source, Configuration config) throws XPathException {
        if ( myConfig == null ) {
            return super.resolveSource(source, config);
        }
        return myConfig.resolveSource(source, config);
    }

    @Override
    public void reportFatalError(XPathException err) {
        if ( myConfig == null ) {
            super.reportFatalError(err);
            return;
        }
        myConfig.reportFatalError(err);
    }

    @Override
    public void registerExternalObjectModel(ExternalObjectModel model) {
        if ( myConfig == null ) {
            super.registerExternalObjectModel(model);
            return;
        }
        myConfig.registerExternalObjectModel(model);
    }

    @Override
    public void registerExtensionFunction(ExtensionFunctionDefinition function) {
        if ( myConfig == null ) {
            super.registerExtensionFunction(function);
            return;
        }
        myConfig.registerExtensionFunction(function);
    }

    @Override
    public String readSchema(PipelineConfiguration pipe, String baseURI, String schemaLocation, String expected) throws SchemaException {
        if ( myConfig == null ) {
            return super.readSchema(pipe, baseURI, schemaLocation, expected);
        }
        return myConfig.readSchema(pipe, baseURI, schemaLocation, expected);
    }

    @Override
    public void readMultipleSchemas(PipelineConfiguration pipe, String baseURI, Collection schemaLocations, String expected) throws SchemaException {
        if ( myConfig == null ) {
            super.readMultipleSchemas(pipe, baseURI, schemaLocations, expected);
            return;
        }
        myConfig.readMultipleSchemas(pipe, baseURI, schemaLocations, expected);
    }

    @Override
    public String readInlineSchema(NodeInfo root, String expected, ErrorListener errorListener) throws SchemaException {
        if ( myConfig == null ) {
            return super.readInlineSchema(root, expected, errorListener);
        }
        return myConfig.readInlineSchema(root, expected, errorListener);
    }

    @Override
    public UserFunction newUserFunction(boolean memoFunction) {
        if ( myConfig == null ) {
            return super.newUserFunction(memoFunction);
        }
        return myConfig.newUserFunction(memoFunction);
    }

    @Override
    public StaticQueryContext newStaticQueryContext() {
        if ( myConfig == null ) {
            return super.newStaticQueryContext();
        }
        return myConfig.newStaticQueryContext();
    }

    @Override
    public PendingUpdateList newPendingUpdateList() {
        if ( myConfig == null ) {
            return super.newPendingUpdateList();
        }
        return myConfig.newPendingUpdateList();
    }

    @Override
    public NodeInfo makeUnconstructedElement(ElementCreator instr, XPathContext context) throws XPathException {
        if ( myConfig == null ) {
            return super.makeUnconstructedElement(instr, context);
        }
        return myConfig.makeUnconstructedElement(instr, context);
    }

    @Override
    public NodeInfo makeUnconstructedDocument(DocumentInstr instr, XPathContext context) throws XPathException {
        if ( myConfig == null ) {
            return super.makeUnconstructedDocument(instr, context);
        }
        return myConfig.makeUnconstructedDocument(instr, context);
    }

    @Override
    public URIResolver makeURIResolver(String className) throws TransformerException {
        if ( myConfig == null ) {
            return super.makeURIResolver(className);
        }
        return myConfig.makeURIResolver(className);
    }

    @Override
    public TraceListener makeTraceListener(String className) throws XPathException {
        if ( myConfig == null ) {
            return super.makeTraceListener(className);
        }
        return myConfig.makeTraceListener(className);
    }

    @Override
    public TraceListener makeTraceListener() throws XPathException {
        if ( myConfig == null ) {
            return super.makeTraceListener();
        }
        return myConfig.makeTraceListener();
    }

    @Override
    public Receiver makeStreamingTransformer(XPathContext context, Mode mode) throws XPathException {
        if ( myConfig == null ) {
            return super.makeStreamingTransformer(context, mode);
        }
        return myConfig.makeStreamingTransformer(context, mode);
    }

    @Override
    public SlotManager makeSlotManager() {
        if ( myConfig == null ) {
            return super.makeSlotManager();
        }
        return myConfig.makeSlotManager();
    }

    @Override
    public PipelineConfiguration makePipelineConfiguration() {
        if ( myConfig == null ) {
            return super.makePipelineConfiguration();
        }
        return myConfig.makePipelineConfiguration();
    }

    @Override
    public XMLReader makeParser(String className) throws TransformerFactoryConfigurationError {
        if ( myConfig == null ) {
            return super.makeParser(className);
        }
        return myConfig.makeParser(className);
    }

    @Override
    public Numberer makeNumberer(String language, String country) {
        if ( myConfig == null ) {
            return super.makeNumberer(language, country);
        }
        return myConfig.makeNumberer(language, country);
    }

    @Override
    public Receiver makeEmitter(String clarkName, Properties props) throws XPathException {
        if ( myConfig == null ) {
            return super.makeEmitter(clarkName, props);
        }
        return myConfig.makeEmitter(clarkName, props);
    }

    @Override
    public FilterFactory makeDocumentProjector(PathMapRoot map) {
        if ( myConfig == null ) {
            return super.makeDocumentProjector(map);
        }
        return myConfig.makeDocumentProjector(map);
    }

    @Override
    public void loadSchema(String absoluteURI) throws SchemaException {
        if ( myConfig == null ) {
            super.loadSchema(absoluteURI);
            return;
        }
        myConfig.loadSchema(absoluteURI);
    }

    @Override
    public boolean isXIncludeAware() {
        if ( myConfig == null ) {
            return super.isXIncludeAware();
        }
        return myConfig.isXIncludeAware();
    }

    @Override
    public boolean isVersionWarning() {
        if ( myConfig == null ) {
            return super.isVersionWarning();
        }
        return myConfig.isVersionWarning();
    }

    @Override
    public boolean isValidationWarnings() {
        if ( myConfig == null ) {
            return super.isValidationWarnings();
        }
        return myConfig.isValidationWarnings();
    }

    @Override
    public boolean isValidation() {
        if ( myConfig == null ) {
            return super.isValidation();
        }
        return myConfig.isValidation();
    }

    @Override
    public boolean isTraceExternalFunctions() {
        if ( myConfig == null ) {
            return super.isTraceExternalFunctions();
        }
        return myConfig.isTraceExternalFunctions();
    }

    @Override
    public boolean isTiming() {
        if ( myConfig == null ) {
            return super.isTiming();
        }
        return myConfig.isTiming();
    }

    @Override
    public boolean isStripsAllWhiteSpace() {
        if ( myConfig == null ) {
            return super.isStripsAllWhiteSpace();
        }
        return myConfig.isStripsAllWhiteSpace();
    }

    @Override
    public boolean isSchemaAware(int language) {
        if ( myConfig == null ) {
            return super.isSchemaAware(language);
        }
        return myConfig.isSchemaAware(language);
    }

    @Override
    public boolean isSchemaAvailable(String targetNamespace) {
        if ( myConfig == null ) {
            return super.isSchemaAvailable(targetNamespace);
        }
        return myConfig.isSchemaAvailable(targetNamespace);
    }

    @Override
    public boolean isRetainDTDAttributeTypes() {
        if ( myConfig == null ) {
            return super.isRetainDTDAttributeTypes();
        }
        return myConfig.isRetainDTDAttributeTypes();
    }

    @Override
    public boolean isOptimizerTracing() {
        if ( myConfig == null ) {
            return super.isOptimizerTracing();
        }
        return myConfig.isOptimizerTracing();
    }

    @Override
    public boolean isMultiThreading() {
        if ( myConfig == null ) {
            return super.isMultiThreading();
        }
        return myConfig.isMultiThreading();
    }

    @Override
    public boolean isLineNumbering() {
        if ( myConfig == null ) {
            return super.isLineNumbering();
        }
        return myConfig.isLineNumbering();
    }

    @Override
    public boolean isLicensedFeature(int feature) {
        if ( myConfig == null ) {
            return super.isLicensedFeature(feature);
        }
        return myConfig.isLicensedFeature(feature);
    }

    @Override
    public boolean isLazyConstructionMode() {
        if ( myConfig == null ) {
            return super.isLazyConstructionMode();
        }
        return myConfig.isLazyConstructionMode();
    }

    @Override
    public boolean isExpandAttributeDefaults() {
        if ( myConfig == null ) {
            return super.isExpandAttributeDefaults();
        }
        return myConfig.isExpandAttributeDefaults();
    }

    @Override
    public boolean isCompileWithTracing() {
        if ( myConfig == null ) {
            return super.isCompileWithTracing();
        }
        return myConfig.isCompileWithTracing();
    }

    @Override
    public boolean isCompatible(Configuration other) {
        if ( myConfig == null ) {
            return super.isCompatible(other);
        }
        return myConfig.isCompatible(other);
    }

    @Override
    public boolean isAllowExternalFunctions() {
        if ( myConfig == null ) {
            return super.isAllowExternalFunctions();
        }
        return myConfig.isAllowExternalFunctions();
    }

    @Override
    public void importComponents(Source source) throws XPathException {
        if ( myConfig == null ) {
            super.importComponents(source);
            return;
        }
        myConfig.importComponents(source);
    }

    @Override
    public int getXsdVersion() {
        if ( myConfig == null ) {
            return super.getXsdVersion();
        }
        return myConfig.getXsdVersion();
    }

    @Override
    public int getXMLVersion() {
        if ( myConfig == null ) {
            return super.getXMLVersion();
        }
        return myConfig.getXMLVersion();
    }

    @Override
    public VendorFunctionLibrary getVendorFunctionLibrary() {
        if ( myConfig == null ) {
            return super.getVendorFunctionLibrary();
        }
        return myConfig.getVendorFunctionLibrary();
    }

    @Override
    public URIResolver getURIResolver() {
        if ( myConfig == null ) {
            return super.getURIResolver();
        }
        return myConfig.getURIResolver();
    }

    @Override
    public int getTreeModel() {
        if ( myConfig == null ) {
            return super.getTreeModel();
        }
        return myConfig.getTreeModel();
    }

    @Override
    public String getTraceListenerClass() {
        if ( myConfig == null ) {
            return super.getTraceListenerClass();
        }
        return myConfig.getTraceListenerClass();
    }

    @Override
    public TraceListener getTraceListener() {
        if ( myConfig == null ) {
            return super.getTraceListener();
        }
        return myConfig.getTraceListener();
    }

    @Override
    public StandardURIResolver getSystemURIResolver() {
        if ( myConfig == null ) {
            return super.getSystemURIResolver();
        }
        return myConfig.getSystemURIResolver();
    }

    @Override
    public int getStripsWhiteSpace() {
        if ( myConfig == null ) {
            return super.getStripsWhiteSpace();
        }
        return myConfig.getStripsWhiteSpace();
    }

    @Override
    public ModuleURIResolver getStandardModuleURIResolver() {
        if ( myConfig == null ) {
            return super.getStandardModuleURIResolver();
        }
        return myConfig.getStandardModuleURIResolver();
    }

    @Override
    public SourceResolver getSourceResolver() {
        if ( myConfig == null ) {
            return super.getSourceResolver();
        }
        return myConfig.getSourceResolver();
    }

    @Override
    public String getSourceParserClass() {
        if ( myConfig == null ) {
            return super.getSourceParserClass();
        }
        return myConfig.getSourceParserClass();
    }

    @Override
    public synchronized XMLReader getSourceParser() throws TransformerFactoryConfigurationError {
        if ( myConfig == null ) {
            return super.getSourceParser();
        }
        return myConfig.getSourceParser();
    }

    @Override
    public SerializerFactory getSerializerFactory() {
        if ( myConfig == null ) {
            return super.getSerializerFactory();
        }
        return myConfig.getSerializerFactory();
    }

    @Override
    public int getSchemaValidationMode() {
        if ( myConfig == null ) {
            return super.getSchemaValidationMode();
        }
        return myConfig.getSchemaValidationMode();
    }

    @Override
    public SchemaURIResolver getSchemaURIResolver() {
        if ( myConfig == null ) {
            return super.getSchemaURIResolver();
        }
        return myConfig.getSchemaURIResolver();
    }

    @Override
    public SchemaType getSchemaType(int fingerprint) {
        if ( myConfig == null ) {
            return super.getSchemaType(fingerprint);
        }
        return myConfig.getSchemaType(fingerprint);
    }

    @Override
    public int getRecoveryPolicy() {
        if ( myConfig == null ) {
            return super.getRecoveryPolicy();
        }
        return myConfig.getRecoveryPolicy();
    }

    @Override
    public String getProductTitle() {
        if ( myConfig == null ) {
            return super.getProductTitle();
        }
        return myConfig.getProductTitle();
    }

    @Override
    public Object getProcessor() {
        if ( myConfig == null ) {
            return super.getProcessor();
        }
        return myConfig.getProcessor();
    }

    @Override
    public ParseOptions getParseOptions() {
        if ( myConfig == null ) {
            return super.getParseOptions();
        }
        return myConfig.getParseOptions();
    }

    @Override
    public OutputURIResolver getOutputURIResolver() {
        if ( myConfig == null ) {
            return super.getOutputURIResolver();
        }
        return myConfig.getOutputURIResolver();
    }

    @Override
    public NamePool getNamePool() {
        if ( myConfig == null ) {
            return super.getNamePool();
        }
        return myConfig.getNamePool();
    }

    @Override
    public NameChecker getNameChecker() {
        if ( myConfig == null ) {
            return super.getNameChecker();
        }
        return myConfig.getNameChecker();
    }

    @Override
    public ModuleURIResolver getModuleURIResolver() {
        if ( myConfig == null ) {
            return super.getModuleURIResolver();
        }
        return myConfig.getModuleURIResolver();
    }

    @Override
    public String getMessageEmitterClass() {
        if ( myConfig == null ) {
            return super.getMessageEmitterClass();
        }
        return myConfig.getMessageEmitterClass();
    }

    @Override
    public LocalizerFactory getLocalizerFactory() {
        if ( myConfig == null ) {
            return super.getLocalizerFactory();
        }
        return myConfig.getLocalizerFactory();
    }

    @Override
    public IntegratedFunctionLibrary getIntegratedFunctionLibrary() {
        if ( myConfig == null ) {
            return super.getIntegratedFunctionLibrary();
        }
        return myConfig.getIntegratedFunctionLibrary();
    }

    @Override
    public Object getInstance(String className, ClassLoader classLoader) throws XPathException {
        if ( myConfig == null ) {
            return super.getInstance(className, classLoader);
        }
        return myConfig.getInstance(className, classLoader);
    }

    @Override
    public Set getImportedNamespaces() {
        if ( myConfig == null ) {
            return super.getImportedNamespaces();
        }
        return myConfig.getImportedNamespaces();
    }

    @Override
    public int getHostLanguage() {
        if ( myConfig == null ) {
            return super.getHostLanguage();
        }
        return myConfig.getHostLanguage();
    }

    @Override
    public DocumentPool getGlobalDocumentPool() {
        if ( myConfig == null ) {
            return super.getGlobalDocumentPool();
        }
        return myConfig.getGlobalDocumentPool();
    }

    @Override
    public List<ExternalObjectModel> getExternalObjectModels() {
        if ( myConfig == null ) {
            return super.getExternalObjectModels();
        }
        return myConfig.getExternalObjectModels();
    }

    @Override
    public ExternalObjectModel getExternalObjectModel(Class nodeClass) {
        if ( myConfig == null ) {
            return super.getExternalObjectModel(nodeClass);
        }
        return myConfig.getExternalObjectModel(nodeClass);
    }

    @Override
    public ExternalObjectModel getExternalObjectModel(String uri) {
        if ( myConfig == null ) {
            return super.getExternalObjectModel(uri);
        }
        return myConfig.getExternalObjectModel(uri);
    }

    @Override
    public Iterator getExtensionsOfType(SchemaType type) {
        if ( myConfig == null ) {
            return super.getExtensionsOfType(type);
        }
        return myConfig.getExtensionsOfType(type);
    }

    @Override
    public ErrorListener getErrorListener() {
        if ( myConfig == null ) {
            return super.getErrorListener();
        }
        return myConfig.getErrorListener();
    }

    @Override
    public SequenceReceiver getElementValidator(SequenceReceiver receiver, NodeName elemName, int locationId, SchemaType schemaType, int validation) throws XPathException {
        if ( myConfig == null ) {
            return super.getElementValidator(receiver, elemName, locationId, schemaType, validation);
        }
        return myConfig.getElementValidator(receiver, elemName, locationId, schemaType, validation);
    }

    @Override
    public SchemaDeclaration getElementDeclaration(int fingerprint) {
        if ( myConfig == null ) {
            return super.getElementDeclaration(fingerprint);
        }
        return myConfig.getElementDeclaration(fingerprint);
    }

    @Override
    public String getEditionCode() {
        if ( myConfig == null ) {
            return super.getEditionCode();
        }
        return myConfig.getEditionCode();
    }

    @Override
    public DynamicLoader getDynamicLoader() {
        if ( myConfig == null ) {
            return super.getDynamicLoader();
        }
        return myConfig.getDynamicLoader();
    }

    @Override
    public Receiver getDocumentValidator(Receiver receiver, String systemId, int validationMode, int stripSpace, SchemaType schemaType, int topLevelElementName) {
        if ( myConfig == null ) {
            return super.getDocumentValidator(receiver, systemId, validationMode, stripSpace, schemaType, topLevelElementName);
        }
        return myConfig.getDocumentValidator(receiver, systemId, validationMode, stripSpace, schemaType, topLevelElementName);
    }

    @Override
    public DocumentNumberAllocator getDocumentNumberAllocator() {
        if ( myConfig == null ) {
            return super.getDocumentNumberAllocator();
        }
        return myConfig.getDocumentNumberAllocator();
    }

    @Override
    public CompilerInfo getDefaultXsltCompilerInfo() {
        if ( myConfig == null ) {
            return super.getDefaultXsltCompilerInfo();
        }
        return myConfig.getDefaultXsltCompilerInfo();
    }

    @Override
    public StaticQueryContext getDefaultStaticQueryContext() {
        if ( myConfig == null ) {
            return super.getDefaultStaticQueryContext();
        }
        return myConfig.getDefaultStaticQueryContext();
    }

    @Override
    public Properties getDefaultSerializationProperties() {
        if ( myConfig == null ) {
            return super.getDefaultSerializationProperties();
        }
        return myConfig.getDefaultSerializationProperties();
    }

    @Override
    public String getDefaultLanguage() {
        if ( myConfig == null ) {
            return super.getDefaultLanguage();
        }
        return myConfig.getDefaultLanguage();
    }

    @Override
    public String getDefaultCountry() {
        if ( myConfig == null ) {
            return super.getDefaultCountry();
        }
        return myConfig.getDefaultCountry();
    }

    @Override
    public String getDefaultCollection() {
        if ( myConfig == null ) {
            return super.getDefaultCollection();
        }
        return myConfig.getDefaultCollection();
    }

    @Override
    public Debugger getDebugger() {
        if ( myConfig == null ) {
            return super.getDebugger();
        }
        return myConfig.getDebugger();
    }

    @Override
    public int getDOMLevel() {
        if ( myConfig == null ) {
            return super.getDOMLevel();
        }
        return myConfig.getDOMLevel();
    }

    @Override
    public XPathContext getConversionContext() {
        if ( myConfig == null ) {
            return super.getConversionContext();
        }
        return myConfig.getConversionContext();
    }

    @Override
    public Object getConfigurationProperty(String name) {
        if ( myConfig == null ) {
            return super.getConfigurationProperty(name);
        }
        return myConfig.getConfigurationProperty(name);
    }

    @Override
    public CollectionURIResolver getCollectionURIResolver() {
        if ( myConfig == null ) {
            return super.getCollectionURIResolver();
        }
        return myConfig.getCollectionURIResolver();
    }

    @Override
    public CollationURIResolver getCollationURIResolver() {
        if ( myConfig == null ) {
            return super.getCollationURIResolver();
        }
        return myConfig.getCollationURIResolver();
    }

    @Override
    public CollationMap getCollationMap() {
        if ( myConfig == null ) {
            return super.getCollationMap();
        }
        return myConfig.getCollationMap();
    }

    @Override
    public Class getClass(String className, boolean tracing, ClassLoader classLoader) throws XPathException {
        if ( myConfig == null ) {
            return super.getClass();
        }
        return myConfig.getClass(className, tracing, classLoader);
    }

    @Override
    public CharacterSetFactory getCharacterSetFactory() {
        if ( myConfig == null ) {
            return super.getCharacterSetFactory();
        }
        return myConfig.getCharacterSetFactory();
    }

    @Override
    public SchemaDeclaration getAttributeDeclaration(int fingerprint) {
        if ( myConfig == null ) {
            return super.getAttributeDeclaration(fingerprint);
        }
        return myConfig.getAttributeDeclaration(fingerprint);
    }

    @Override
    public Receiver getAnnotationStripper(Receiver destination) {
        if ( myConfig == null ) {
            return super.getAnnotationStripper(destination);
        }
        return myConfig.getAnnotationStripper(destination);
    }

    @Override
    public void exportComponents(Receiver out) throws XPathException {
        if ( myConfig == null ) {
            super.exportComponents(out);
            return;
        }
        myConfig.exportComponents(out);
    }

    @Override
    public void displayLicenseMessage() {
        if ( myConfig == null ) {
            super.displayLicenseMessage();
            return;
        }
        myConfig.displayLicenseMessage();
    }

    @Override
    public void checkTypeDerivationIsOK(SchemaType derived, SchemaType base, int block) throws SchemaException {
        if ( myConfig == null ) {
            super.checkTypeDerivationIsOK(derived, base, block);
            return;
        }
        myConfig.checkTypeDerivationIsOK(derived, base, block);
    }

    @Override
    public DocumentInfo buildDocument(Source source, ParseOptions parseOptions) throws XPathException {
        if ( myConfig == null ) {
            return super.buildDocument(source, parseOptions);
        }
        return myConfig.buildDocument(source, parseOptions);
    }

    @Override
    public DocumentInfo buildDocument(Source source) throws XPathException {
        if ( myConfig == null ) {
            return super.buildDocument(source);
        }
        return myConfig.buildDocument(source);
    }

    @Override
    public void addSchemaSource(Source schemaSource, ErrorListener errorListener) throws SchemaException {
        if ( myConfig == null ) {
            super.addSchemaSource(schemaSource, errorListener);
            return;
        }
        myConfig.addSchemaSource(schemaSource, errorListener);
    }

    @Override
    public void addSchemaSource(Source schemaSource) throws SchemaException {
        if ( myConfig == null ) {
            super.addSchemaSource(schemaSource);
            return;
        }
        myConfig.addSchemaSource(schemaSource);
    }

    @Override
    public void addExtensionBinders(FunctionLibraryList list) {
        if ( myConfig == null ) {
            super.addExtensionBinders(list);
            return;
        }
        myConfig.addExtensionBinders(list);
    }

    private Configuration myConfig;
}
