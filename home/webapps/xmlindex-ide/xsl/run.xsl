<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:conf="http://www.armatiek.com/xslweb/configuration"
  xmlns:map="http://www.w3.org/2005/xpath-functions/map"
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:xix="http://www.armatiek.com/xslweb/functions/xmlindex"
  xmlns:json="http://www.armatiek.com/xslweb/functions/json"
  xmlns:ser="http://www.armatiek.com/xslweb/functions/serialize"
  xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization"
  xmlns:err="http://www.w3.org/2005/xqt-errors"
  xmlns:local="urn:local"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:output method="text"/>
  
  <xsl:param name="conf:webapp-dir" as="xs:string"/>
  
  <xsl:variable name="query-params" select="/*/req:parameters/req:parameter" as="element(req:parameter)*"/>
  
  <xsl:variable name="index" select="$query-params[@name = 'index']/req:value" as="xs:string"/>
  <xsl:variable name="code" select="$query-params[@name = 'code']/req:value" as="xs:string?"/>
  <xsl:variable name="path" select="$query-params[@name = 'path']/req:value" as="xs:string?"/>
  
  <xsl:variable name="output-parameters" as="element(output:serialization-parameters)">
    <output:serialization-parameters>
      <output:method value="xml"/>
      <output:indent value="yes"/>
      <output:omit-xml-declaration value="yes"/>
    </output:serialization-parameters>  
  </xsl:variable>
  
  <xsl:template match="/">
    <xsl:variable name="request-ok" select="not(normalize-space($code) = '')" as="xs:boolean"/>
    <resp:response status="{if ($request-ok) then '200' else '400'}">
      <xsl:if test="$request-ok">
        <resp:body>
          <xsl:call-template name="body"/>
        </resp:body>  
      </xsl:if>
    </resp:response>          
  </xsl:template>
  
  <xsl:template name="body">  
    <xsl:variable name="session" select="xix:get-session($index)"/>
    <xsl:choose>
      <xsl:when test="contains($code, 'xsl:stylesheet')">
        <xsl:sequence select="xix:transform-adhoc($session, $code, (), false())"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="base-uri" select="xs:anyURI(local:path-to-file-uri(concat($conf:webapp-dir, '/', $path)))" as="xs:anyURI"/>
        <xsl:variable name="results" select="xix:query-adhoc($session, $code, $base-uri, (), false(), true())" as="item()*"/>
        <xsl:text>{ "time" : </xsl:text> 
        <xsl:value-of select="$results[1]"/>
        <xsl:if test="$results[2]/self::err:error">
          <xsl:apply-templates select="$results[2]"/>
        </xsl:if>
        <xsl:text>, "result" : "</xsl:text>
        <xsl:sequence select="json:escape-json(ser:serialize($results[2], $output-parameters))"/>  
        <xsl:text>"}</xsl:text>  
      </xsl:otherwise>
    </xsl:choose>
    <xsl:sequence select="xix:close($session)"/>
  </xsl:template>
  
  <xsl:template match="err:code">
    <xsl:text>, "errCode" : "</xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>"</xsl:text>
  </xsl:template>
  
  <xsl:template match="err:description">
    <xsl:text>, "errDescription" : "</xsl:text>
    <xsl:value-of select="json:escape-json(.)"/>
    <xsl:text>"</xsl:text>
  </xsl:template>
  
  <xsl:template match="err:module">
    <xsl:text>, "errModule" : "</xsl:text>
    <xsl:value-of select="json:escape-json(.)"/>
    <xsl:text>"</xsl:text>
  </xsl:template>
  
  <xsl:template match="err:line-number">
    <xsl:text>, "errLine" : </xsl:text>
    <xsl:value-of select="."/>
  </xsl:template>
  
  <xsl:template match="err:column-number">
    <xsl:text>, "errColumn" : </xsl:text>
    <xsl:value-of select="."/>
  </xsl:template>
  
  <xsl:function name="local:path-to-file-uri" as="xs:string">
    <xsl:param name="path" as="xs:string"/>
    <xsl:variable name="protocol-prefix" as="xs:string">
      <xsl:choose>
        <xsl:when test="starts-with($path, '\\')">file://</xsl:when> <!-- UNC path -->
        <xsl:when test="matches($path, '[a-zA-Z]:[\\/]')">file:///</xsl:when> <!-- Windows drive path -->
        <xsl:when test="starts-with($path, '/')">file://</xsl:when> <!-- Unix path -->
        <xsl:otherwise>file://</xsl:otherwise>
      </xsl:choose>  
    </xsl:variable>
    <xsl:variable name="norm-path" select="translate($path, '\', '/')" as="xs:string"/>
    <xsl:variable name="path-parts" select="tokenize($norm-path, '/')" as="xs:string*"/>
    <xsl:variable name="encoded-path" select="string-join(for $p in $path-parts return encode-for-uri($p), '/')" as="xs:string"/>
    <xsl:value-of select="concat($protocol-prefix, $encoded-path)"/>        
  </xsl:function>
  
</xsl:stylesheet>