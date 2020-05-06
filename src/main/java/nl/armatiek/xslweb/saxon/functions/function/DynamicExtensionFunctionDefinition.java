package nl.armatiek.xslweb.saxon.functions.function;

import javax.xml.XMLConstants;

import org.apache.commons.lang3.StringUtils;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionDefinition;

public class DynamicExtensionFunctionDefinition extends ExtensionFunctionDefinition {

  private QName funcName;
  private int minArguments;
  private int maxArguments;
  private String[] argTypes;
  private String resultType;
  private boolean hasSideEffects;
  
  public DynamicExtensionFunctionDefinition(Configuration configuration, QName funcName, 
      int minArguments, int maxArguments, String[] argTypes, String resultType, boolean hasSideEffects) {
    super(configuration);
    this.minArguments = minArguments;
    this.maxArguments = maxArguments;
    this.argTypes = argTypes;
    this.resultType = resultType;
    this.hasSideEffects = hasSideEffects;
  }

  @Override
  public StructuredQName getFunctionQName() {
    return new StructuredQName("", funcName.getNamespaceURI(), funcName.getLocalName());
  }
  
  @Override
  public int getMinimumNumberOfArguments() {
    return minArguments;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return maxArguments;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    SequenceType[] types = new SequenceType[argTypes.length];
    for (int i=0; i<argTypes.length; i++) {
      types[i] = getSequenceType(argTypes[i]);
    }
    return types;
  }
  
  @Override
  public boolean hasSideEffects() {
    return hasSideEffects;
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return getSequenceType(resultType);
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    // TODO Auto-generated method stub
    return null;
  }
  
  private SequenceType getSequenceType(String lexicalType) {
    ItemType itemType;
    SequenceType sequenceType = null;
    lexicalType = lexicalType.trim().toLowerCase();
    
    if (lexicalType.startsWith("item()")) {
      itemType = AnyItemType.getInstance();
    } else if (lexicalType.startsWith("node(") || lexicalType.startsWith("element(") || lexicalType.startsWith("attribute(") ||
        lexicalType.startsWith("document-node(") || lexicalType.startsWith("comment(") || lexicalType.startsWith("processing-instruction(")) {
      itemType = AnyNodeTest.getInstance();
    } else if (lexicalType.startsWith("empty-sequence()")) {
      itemType = null;
      sequenceType = SequenceType.EMPTY_SEQUENCE;
    } else if (lexicalType.startsWith("xs:") || lexicalType.startsWith("xsd:")) {
      itemType = Type.getBuiltInItemType(XMLConstants.W3C_XML_SCHEMA_NS_URI, StringUtils.substringAfter(lexicalType, ":"));
    } else {
      itemType = AnyNodeTest.getInstance(); // TODO: raise exception?
    }
    
    if (sequenceType != null) {
      return sequenceType;
    }
    
    int cardinality = StaticProperty.EXACTLY_ONE; 
    if (lexicalType.endsWith("*")) {
      cardinality = StaticProperty.ALLOWS_ZERO_OR_MORE;
    } else if (lexicalType.endsWith("+")) {
      cardinality = StaticProperty.ALLOWS_ONE_OR_MORE;
    } else if (lexicalType.endsWith("?")) {
      cardinality = StaticProperty.ALLOWS_ZERO_OR_ONE;
    }
 
    return SequenceType.makeSequenceType(itemType, cardinality);
  }

}