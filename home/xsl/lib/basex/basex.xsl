<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:http="http://expath.org/ns/http-client"
  xmlns:rest="http://basex.org/rest"
  xmlns:basex="http://basex.org"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:function name="basex:query" as="node()*">
    <xsl:param name="username" as="xs:string"/>
    <xsl:param name="password" as="xs:string"/>  
    <xsl:variable name="req" as="element()">
      <http:request 
        href="''"
        method="get"
        username="{$username}"
        password="{$password}"
        auth-method="basic"
        send-authorization="true"/>
    </xsl:variable>
    <xsl:sequence select="http:send-request($req)[2]"/>
  </xsl:function>
  
  <xsl:function name="basex:run">
    
  </xsl:function>
  
  <xsl:function name="basex:command">
    
  </xsl:function>
  
  <xsl:function name="basex:get">
    
  </xsl:function>
  
</xsl:stylesheet>