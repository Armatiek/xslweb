<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  xmlns:ser="http://www.armatiek.com/xslweb/functions/serialize"
  xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization"
  xmlns:val="http://www.armatiek.com/xslweb/validation"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:output method="xhtml" indent="yes" omit-xml-declaration="yes"/>
  
  <xsl:param name="val:schema-validation-report" as="document-node()?"/>
  <xsl:param name="val:schematron-validation-report" as="document-node()?"/>
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:param name="config:webapp-path" as="xs:string"/>
  
  <xsl:template name="title" as="xs:string">Example 26: XML Validation</xsl:template>
  
  <xsl:template name="tab-contents-1">
    
    <p>In this example the XML Schema and Schematron validation pipeline steps are used to validate a transformation result.</p>
    
    <xsl:variable name="output-parameters" as="element()">
      <output:serialization-parameters>
        <output:method value="xml"/>
        <output:indent value="yes"/>
        <output:omit-xml-declaration value="yes"/>
      </output:serialization-parameters>
    </xsl:variable>
    
    <h2>Sample document to validate</h2>
    <pre class="prettyprint lang-xml linenums">
      <xsl:sequence select="ser:serialize(., $output-parameters)"/>
    </pre>
    
    <h2>XML Schema validation report</h2>
    <xsl:choose>
      <xsl:when test="$val:schema-validation-report">  
        <pre class="prettyprint lang-xml linenums">
          <xsl:sequence select="ser:serialize($val:schema-validation-report, $output-parameters)"/>
        </pre>
      </xsl:when>
      <xsl:otherwise>
        <p>No XML schema validation errors where found</p>
      </xsl:otherwise>
    </xsl:choose>
    
    <h2>Schematron validation report</h2>
    <xsl:choose>
      <xsl:when test="$val:schematron-validation-report">  
        <pre class="prettyprint lang-xml linenums">
          <xsl:sequence select="ser:serialize($val:schematron-validation-report, $output-parameters)"/>
        </pre>
      </xsl:when>
      <xsl:otherwise>
        <p>No Schematron validation errors where found</p>
      </xsl:otherwise>
    </xsl:choose>
     
  </xsl:template>
  
  <xsl:template name="base-path" as="xs:string">
    <xsl:value-of select="req:get-attribute('base-path')"/>
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">xml-validation</xsl:variable>
  
</xsl:stylesheet>