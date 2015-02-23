<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:json="http://www.armatiek.com/xslweb/functions/json"
  xmlns:ser="http://www.armatiek.com/xslweb/functions/serialize"
  xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization"  
  xmlns:local="urn:local"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Example 18: JSON extension functions</xsl:template>
  
  <xsl:template name="tab-contents-1">
    
    <p>This example shows how to use the extension functions <i>json:serialize-json()</i>
      and  <i>json:parse-json()</i>.</p>
    
    <xsl:variable name="output-parameters" as="element()">
      <output:serialization-parameters>
        <output:method value="xml"/>
        <output:indent value="yes"/>
        <output:omit-xml-declaration value="yes"/>
      </output:serialization-parameters>
    </xsl:variable>
    
    <xsl:variable name="xml" as="element()">
      <a xmlns="">
        <b x="y"/>
        <xsl:processing-instruction name="xml-multiple"/>
        <c>Two</c>
        <c>Three</c>
        <c>Four</c>        
      </a>      
    </xsl:variable>
    
    <h3>Input XML ($xml):</h3>
    <pre class="prettyprint lang-xml linenums">
      <xsl:sequence select="ser:serialize($xml, $output-parameters)"/>
    </pre>
    
    <xsl:variable name="json" select="json:serialize-json($xml)" as="xs:string"/>
    
    <h3>Result of <i>json:serialize-json($xml)</i> ($json):</h3>
    <pre class="prettyprint lang-json linenums">
      <xsl:value-of select="$json"/>
    </pre>
    
    <xsl:variable name="xml-parsed" select="json:parse-json($json)" as="document-node()"/>
    
    <h3>Result of <i>json:parse-json($json)</i>:</h3>
    <pre class="prettyprint lang-xml linenums">
      <xsl:sequence select="ser:serialize($xml-parsed/*, $output-parameters)"/>          
    </pre>
    
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">json-extension-functions</xsl:variable>
  
</xsl:stylesheet>