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
    <xsl:variable name="function-name" select="QName('http://www.armatiek.com/xslweb/functions/text', 'lcs')"/>
    <xsl:variable name="java-code" as="xs:string">
      <![CDATA[
      import org.apache.commons.text.similarity.LongestCommonSubsequence;
      
      public class LongestCommonSubsequenceFunction {
      
        public String call(String left, String right) {
          LongestCommonSubsequence lcs = new LongestCommonSubsequence();
          return lcs.longestCommonSubsequence(left, right).toString();
        }
      
      }
      ]]>
    </xsl:variable>
    <xsl:variable name="result" select="function:register($function-name, ($java-code))" as="element(function:diagnostics)?"/>
    <xsl:choose>
      <xsl:when test="$result/function:diagnostic/@kind = 'ERROR'">
        <p>Error compiling/registering function:</p>
        <pre class="prettyprint lang-xml linenums">
          <xsl:sequence select="serialize($result, $output-parameters)"/>
        </pre>
      </xsl:when>
      <xsl:otherwise>
        <p>Result of function call:</p>
        <xsl:sequence select="function:call($function-name, 'ABACCD', 'ACDF')"/>  
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">function</xsl:variable>
  
</xsl:stylesheet>