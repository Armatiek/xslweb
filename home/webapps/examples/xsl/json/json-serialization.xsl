<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet   
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"    
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"       
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:output method="xml"/>
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:body>
        <xsl:call-template name="body"/>
      </resp:body>
    </resp:response>          
  </xsl:template>
  
  <xsl:template name="body">
    <a x="y">
      <b>
        <c>One</c>
        <c>Two</c>
        <c>Three</c>
      </b>
    </a>        
  </xsl:template>
  
</xsl:stylesheet>