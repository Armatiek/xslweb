<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Example 5: HTTP headers using Response XML</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <p>This example adds three HTTP headers to the response using the Response XML, the string header <i>Pragma</i>, 
      the integer header <i>Expires</i> and the date header <i>Last Modified</i>. See the tab "Pipeline stylesheet".</p>
  </xsl:template>
  
  <xsl:template name="headers">
    <resp:headers>                              
      <resp:header name="Pragma">no-cache</resp:header>        
      <resp:int-header name="Expires">0</resp:int-header>
      <resp:date-header name="Last-Modified">
        <xsl:value-of select="current-dateTime()"/>
      </resp:date-header>
    </resp:headers>
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">headers-response</xsl:variable>
  
</xsl:stylesheet>