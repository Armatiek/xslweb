<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">HTTP headers example</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <p>This example adds a three HTTP headers to the response, the string header <i>Pragma</i>, 
    the integer header <i>Expires</i> and the date header <i>Last Modified</i>.</p>
    
    <!-- Add string header to response: -->
    <xsl:sequence select="resp:add-header('Pragma', 'no-cache')"/>
    
    <!-- Add int header to response: -->
    <xsl:sequence select="resp:add-int-header('Expires', 0)"/>
    
    <!-- Add date header to response: -->
    <xsl:sequence select="resp:add-date-header('Last-Modified', current-dateTime())"/>

  </xsl:template>
  
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="dispatcher-match" as="xs:string">headers.html</xsl:variable>
  
</xsl:stylesheet>