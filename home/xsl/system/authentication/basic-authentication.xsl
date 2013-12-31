<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"  
  xmlns:auth="http://www.armatiek.com/xslweb/auth"
  xmlns:base64="http://www.armatiek.com/xslweb/functions/base64"
  xmlns:session="http://www.armatiek.com/xslweb/functions/session"
  xmlns:log="http://www.armatiek.com/xslweb/functions/log"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:variable name="session:attr-name-user-profile" as="xs:string">xslweb-user-profile</xsl:variable>
  
  <xsl:function name="auth:logout" as="xs:string*">
    <xsl:value-of select="if (session:set-attribute($session:attr-name-user-profile)) then () 
      else (error(xs:QName('err:XSLWEB0001'), 'Could not set session attribute'))"/>
  </xsl:function>
    
  <xsl:function name="auth:credentials" as="xs:string*">    
    <xsl:param name="request" as="document-node()"/>        
    <xsl:variable name="authorization-header" select="$request/*/req:headers/req:header[lower-case(@name) = 'authorization']/text()" as="xs:string?"/>    
    <xsl:sequence select="if ($authorization-header) then tokenize(base64:decode(substring-after(normalize-space($authorization-header), ' ')), ':') else ()"/> 
  </xsl:function>
  
  <xsl:template match="/req:request[auth:must-authenticate(/)]" priority="9">
    <xsl:variable name="user-profile" select="session:get-attribute($session:attr-name-user-profile)" as="element()?"/>
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
                <xsl:value-of select="if (session:set-attribute($session:attr-name-user-profile, $user-profile)) then () 
                  else (error(xs:QName('err:XSLWEB0001'), 'Could not set session attribute'))"/>                
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
      <resp:body/>                      
    </resp:response>
  </xsl:template>
  
</xsl:stylesheet>