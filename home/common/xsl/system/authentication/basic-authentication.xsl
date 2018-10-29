<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:session="http://www.armatiek.com/xslweb/session"  
  xmlns:auth="http://www.armatiek.com/xslweb/auth"
  xmlns:base64="http://www.armatiek.com/xslweb/functions/base64"  
  xmlns:log="http://www.armatiek.com/xslweb/functions/log"
  xmlns:err="http://expath.org/ns/error"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:variable name="session:attr-name-userprofile" as="xs:string">xslweb-userprofile</xsl:variable>
  
  <xsl:function name="auth:logout" as="xs:boolean?">
    <xsl:sequence select="session:set-attribute($session:attr-name-userprofile)"/>
  </xsl:function>
    
  <xsl:function name="auth:credentials" as="xs:string*">    
    <xsl:param name="request" as="document-node()"/>        
    <xsl:variable name="authorization-header" select="$request/*/req:headers/req:header[lower-case(@name) = 'authorization']/text()" as="xs:string?"/>    
    <xsl:sequence select="if ($authorization-header) then tokenize(base64:decode(substring-after(normalize-space($authorization-header), ' ')), ':') else ()"/> 
  </xsl:function>
  
  <xsl:template match="/req:request[auth:must-authenticate(/)]" priority="9">
    <xsl:variable name="user-profile" select="session:get-attribute($session:attr-name-userprofile)" as="element()?"/>
    <xsl:choose>
      <xsl:when test="$user-profile">        
        <xsl:next-match/>
      </xsl:when>
      <xsl:otherwise>        
        <xsl:variable name="credentials" select="auth:credentials(/)" as="xs:string*"/>        
        <xsl:choose>                    
          <xsl:when test="(count($credentials) = 2)">
            <xsl:variable name="user-profile" select="auth:login($credentials[1], $credentials[2])" as="element()?"/>
            <xsl:choose>
              <xsl:when test="$user-profile">
                <xsl:value-of select="session:set-attribute($session:attr-name-userprofile, $user-profile)"/>                
                <xsl:next-match/>    
              </xsl:when>
              <xsl:otherwise>
                <xsl:call-template name="auth:unauthorized-response"/>
              </xsl:otherwise>
            </xsl:choose>                                    
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="auth:unauthorized-response"/>                   
          </xsl:otherwise>
        </xsl:choose>        
      </xsl:otherwise>      
    </xsl:choose>                
  </xsl:template>
  
  <xsl:template name="auth:unauthorized-response" as="element()">    
    <resp:response status="401"> <!-- Unauthorized -->
      <resp:headers>            
        <resp:header name="WWW-Authenticate">
          <xsl:value-of select="concat('Basic realm=', '&quot;', auth:get-realm(), '&quot;')"/>
        </resp:header>                   
      </resp:headers>
      <resp:body>
        <html><body><h2>Unauthorized (401)</h2></body></html>
      </resp:body>                      
    </resp:response>
  </xsl:template>
  
</xsl:stylesheet>