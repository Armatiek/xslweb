<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" 
  xmlns:sec="http://www.armatiek.com/xslweb/functions/security"
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Example 33: Security - secured for read permission</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <p>Hello <xsl:value-of select="sec:principal()"/>, you are <xsl:value-of select="if (sec:is-authenticated()) then '' else ' not '"/> authenicated.</p>
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">security-secured-for-read-permission</xsl:variable>
  
</xsl:stylesheet>