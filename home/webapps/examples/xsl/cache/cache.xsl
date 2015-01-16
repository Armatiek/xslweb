<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:param name="config:development-mode" as="xs:boolean"/>
  
  <xsl:template name="title" as="xs:string">Example 15: Caching</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <p>The output of this page will be cached by the caching framework of XSLWeb because 
      of the caching attributes on the pipeline element of the current pipeline.</p>
    <p>N.B. Caching is disabled in development-mode. Currently this mode is set to: 
      <span style="color:red"><xsl:value-of select="$config:development-mode"/></span>. 
      You can change this mode in the configuration file <i>webapp.xml</i> of this web 
      application.</p>
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">cache</xsl:variable>
  
</xsl:stylesheet>