<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:function="http://www.armatiek.com/xslweb/functions/function"
  xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Example 21: Dynamic functions extensions</xsl:template>
  
  <xsl:variable name="output-parameters" as="node()">
    <output:serialization-parameters>
      <output:method value="xml"/>
      <output:indent value="yes"/>
      <output:omit-xml-declaration value="yes"/>
    </output:serialization-parameters>  
  </xsl:variable>
  
  <xsl:template name="tab-contents-1">
    <p>Text</p>
    <xsl:variable name="java-code" as="xs:string">
      <![CDATA[
      import net.sf.saxon.expr.XPathContext;
      import net.sf.saxon.lib.ExtensionFunctionCall;
      import net.sf.saxon.lib.ExtensionFunctionDefinition;
      import net.sf.saxon.om.Sequence;
      import net.sf.saxon.om.StructuredQName;
      import net.sf.saxon.trans.XPathException;
      import net.sf.saxon.value.Int64Value;
      import net.sf.saxon.value.IntegerValue;
      import net.sf.saxon.value.SequenceType;
      import net.sf.saxon.om.SequenceTool;
      
      public class Test extends ExtensionFunctionDefinition {
        @Override
        public StructuredQName getFunctionQName() {
          return new StructuredQName("eg", "http://example.com/saxon-extension", "shift-left");
        }
        
        @Override
        public SequenceType[] getArgumentTypes() {
          return new SequenceType[]{SequenceType.SINGLE_INTEGER, SequenceType.SINGLE_INTEGER};
        }
        
        @Override
        public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
          return SequenceType.SINGLE_INTEGER;
        }
        
        @Override
        public ExtensionFunctionCall makeCallExpression() {
          return new ExtensionFunctionCall() {
            @Override
            public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
              long v0 = ((IntegerValue) SequenceTool.itemAt(arguments[0], 0)).longValue();
              long v1 = ((IntegerValue) SequenceTool.itemAt(arguments[0], 1)).longValue();
              long result = v0<<v1;
              return Int64Value.makeIntegerValue(result);
            }
          };
        }
      }
      ]]>
    </xsl:variable>
    <xsl:variable name="result" select="function:register('test', $java-code)" as="element(function:diagnostics)?"/>
    <xsl:choose>
      <xsl:when test="$result/function:diagnostic/@kind = 'ERROR'">
        <p>Error compiling/registering function:</p>
        <pre class="prettyprint lang-xml linenums">
          <xsl:sequence select="serialize($result, $output-parameters)"/>
        </pre>
      </xsl:when>
      <xsl:otherwise>
        <p>Result of function call:</p>
        <xsl:sequence select="function:call(QName('http://example.com/saxon-extension', 'shift-left'), (1, 2))"/>  
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">script</xsl:variable>
  
</xsl:stylesheet>