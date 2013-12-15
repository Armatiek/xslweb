<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:http="http://expath.org/ns/http-client"
  xmlns:rest="http://basex.org/rest"
  xmlns:basex="http://basex.org"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:function name="basex:create-db">
    
  </xsl:function>
  
  <xsl:function name="basex:delete-db">
    
  </xsl:function>
  
  <xsl:function name="basex:create-resource">
    
  </xsl:function>
  
  <xsl:function name="basex:delete-resource">
    
  </xsl:function>
  
  <xsl:function name="basex:query" as="item()*">
    <xsl:param name="uri" as="xs:anyURI"/>
    <xsl:param name="username" as="xs:string"/>
    <xsl:param name="password" as="xs:string"/> 
    <xsl:param name="query" as="xs:string"/>
    <xsl:param name="variables" as="element()*"/>
    <xsl:param name="options" as="element()*"/>
    <xsl:variable name="req" as="element()">
      <http:request 
        href="''"
        method="post"
        username="{$username}"
        password="{$password}"
        auth-method="basic"
        send-authorization="true">
        <http:body content-type="application/xml">
          <rest:query>
            <rest:text><xsl:value-of select="$query"/></rest:text>           
          </rest:query>  
        </http:body>
      </http:request>
    </xsl:variable>
    <xsl:sequence select="http:send-request($req)"/>
  </xsl:function>
  
  <xsl:function name="basex:run">
    
  </xsl:function>
  
  <xsl:function name="basex:command">
    
  </xsl:function>
  
  <xsl:function name="basex:get">
    
  </xsl:function>
  
</xsl:stylesheet>