<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" 
  xmlns:conf="http://www.armatiek.com/xslweb/configuration"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:json="http://www.armatiek.com/xslweb/functions/json"
  xmlns:file="http://expath.org/ns/file"
  xmlns:functx="http://www.functx.com"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:output method="text"/>
  
  <xsl:param name="conf:webapp-dir" as="xs:string"/>
  
  <xsl:variable name="quote" as="xs:string">"</xsl:variable>
  <xsl:variable name="apos" as="xs:string">'</xsl:variable>
  
  <xsl:include href="../../../common/xsl/lib/functx/functx-1.0.xsl"/>
  
  <xsl:variable name="query-params" select="/*/req:parameters/req:parameter" as="element(req:parameter)*"/>
  
  <xsl:variable name="id" select="$query-params[@name = 'id']/req:value" as="xs:string?"/>
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:body>
        <xsl:call-template name="body"/>
      </resp:body>  
    </resp:response>          
  </xsl:template>
  
  <xsl:template name="body">  
    <xsl:variable name="dir" select="concat($conf:webapp-dir, '/', if ($id = '#') then () else $id)"/>
    <xsl:variable name="prefix-path" select="if ($id = ('', '#')) then () else json:escape-json(concat($id, '/'))" as="xs:string?"/>
    
    <xsl:text>[</xsl:text>
    <xsl:variable name="nodes" as="xs:string*">
      <xsl:for-each select="file:children($dir)">
        <xsl:sort select="." data-type="text"/>
        <xsl:variable name="path" select="concat($dir, '/', .)" as="xs:string"/>
        <xsl:if test="file:is-dir($path)">
          <xsl:value-of select="concat('{', $quote, 'id', $quote, ':', $quote, $prefix-path, ., $quote, ',', $quote, 'text', $quote, ':', $quote, 
            file:name(.), $quote, ',', $quote, 'type', $quote, ':', $quote, 'folder', $quote, ',', $quote, 'children', $quote, if (count(file:children($path)) gt 0) then ':true' else ':false', '}')"/>
        </xsl:if>
      </xsl:for-each>
      <!-- Files: -->
      <xsl:for-each select="file:children($dir)">
        <xsl:sort select="." data-type="text"/>
        <xsl:variable name="path" select="concat($dir, '/', .)" as="xs:string"/>
        <xsl:if test="file:is-file($path)">
          <xsl:value-of select="concat('{', $quote, 'id', $quote, ':', $quote, $prefix-path, ., $quote, ',', $quote, 'text', $quote, ':', $quote, 
            file:name(.), $quote, ',', $quote, 'type', $quote, ':', $quote, 'file', $quote, ',', $quote, 'children', $quote, ':false', '}')"/>
        </xsl:if>
      </xsl:for-each>  
    </xsl:variable>
    <xsl:sequence select="string-join($nodes, ',')"/>
    <xsl:text>]</xsl:text>
  </xsl:template>
  
</xsl:stylesheet>