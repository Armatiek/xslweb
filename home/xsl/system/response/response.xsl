<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:error="http://www.armatiek.com/xslweb/error"  
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:param name="req:request"/>
  <xsl:param name="resp:response"/>  
  
  <xsl:template match="/"> 
    <xsl:apply-templates select="resp:response"/>
    <xsl:apply-templates select="resp:response/(resp:headers|resp:session|resp:cookies)"/>       
    <xsl:sequence select="resp:response/resp:body/node()"/>
  </xsl:template>
  
  <xsl:template match="resp:response">
    <xsl:value-of select="if (resp:set-status(@status)) then () else error(xs:QName('error:response-status'), 'Could not set status of response')"/>       
  </xsl:template>
  
  <xsl:template match="resp:headers">
    <xsl:value-of select="if (resp:headers(.)) then () else error(xs:QName('error:response-headers'), 'Could not set headers of response')"/>
  </xsl:template>
  
  <xsl:template match="resp:session">
    <xsl:value-of select="if (resp:session(.)) then () else error(xs:QName('error:response-session'), 'Could not set session of response')"/>
  </xsl:template>
  
  <xsl:template match="resp:cookies">
    <xsl:value-of select="if (resp:cookies(.)) then () else error(xs:QName('error:response-cookies'), 'Could not set cookies of response')"/>
  </xsl:template>
  
</xsl:stylesheet>