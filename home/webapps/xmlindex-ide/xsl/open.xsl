<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:conf="http://www.armatiek.com/xslweb/configuration"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:file="http://expath.org/ns/file"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:output method="text"/>
  
  <xsl:param name="conf:webapp-dir" as="xs:string"/>
  
  <xsl:variable name="query-params" select="/*/req:parameters/req:parameter" as="element(req:parameter)*"/>
  
  <xsl:variable name="path" select="$query-params[@name = 'path']/req:value" as="xs:string?"/>
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:body>
        <xsl:call-template name="body"/>
      </resp:body>  
    </resp:response>          
  </xsl:template>
  
  <xsl:template name="body">  
    <xsl:sequence select="file:read-text(concat($conf:webapp-dir, file:dir-separator(), $path), 'UTF-8')"/>
  </xsl:template>
  
</xsl:stylesheet>