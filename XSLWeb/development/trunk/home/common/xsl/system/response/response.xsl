<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:request="http://www.armatiek.com/xslweb/request"
  xmlns:response="http://www.armatiek.com/xslweb/response"
  xmlns:session="http://www.armatiek.com/xslweb/session"
  xmlns:error="http://www.armatiek.com/xslweb/error"  
  exclude-result-prefixes="#all"
  extension-element-prefixes="request response session"
  version="2.0">
  
  <xsl:template match="/response:response">
    <!-- HTTP Status: -->
    <xsl:if test="@status">
      <xsl:value-of select="if (empty(response:set-status(@status))) then () else error(xs:QName('error:response-status'), 'Could not set status of response')"/>
    </xsl:if>
    
    <!-- Headers: -->
    <xsl:for-each select="response:headers/response:header">
      <xsl:choose>
        <xsl:when test="self::response:header">
          <xsl:sequence select="if (empty(response:add-header(@name, xs:string(.)))) then () else error(xs:QName('error:response-header'), 'Could not add header to response')"/>
        </xsl:when>
        <xsl:when test="self::response:int-header">
          <xsl:sequence select="if (empty(response:add-int-header(@name, xs:integer(.)))) then () else error(xs:QName('error:response-header'), 'Could not add int header to response')"/>
        </xsl:when>
        <xsl:when test="self::response:date-header">
          <xsl:sequence select="if (empty(response:add-date-header(@name, xs:dateTime(.)))) then () else error(xs:QName('error:response-header'), 'Could not add date header to response')"/>
        </xsl:when>
      </xsl:choose>            
    </xsl:for-each>
    
    <!-- Session: -->
    <xsl:for-each select="response:session">
      <xsl:if test="@invalidate = 'true'">
        <xsl:sequence select="if (empty(session:invalidate())) then () else error(xs:QName('error:session-invalidate'), 'Could not invalidate session')"/>
      </xsl:if>
      <xsl:if test="@max-inactive-interval">
        <xsl:sequence select="if (empty(session:set-max-inactive-interval(xs:integer(@max-inactive-interval)))) then () else error(xs:QName('error:session-set-max-inactive-interval'), 'Could not set max inactive interval of session')"/>
      </xsl:if>
      <xsl:for-each select="response:attributes/response:attribute">
        <xsl:sequence select="if (empty(session:set-attribute(@name, ./node()))) then () else error(xs:QName('error:session-attribute'), 'Could not set session attribute')"/>
      </xsl:for-each>          
    </xsl:for-each>
    
    <!-- Cookies: -->
    <xsl:for-each select="response:cookies/response:cookie">
      <xsl:sequence select="if (empty(response:add-cookie(.))) then () else error(xs:QName('error:response-cookie'), 'Could not set add cookie to response')"/>
    </xsl:for-each>
              
    <xsl:apply-templates select="response:body/node()"/>
  </xsl:template>
  
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>