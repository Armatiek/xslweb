<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:response="http://www.armatiek.com/xslweb/response"
  xmlns:error="http://www.armatiek.com/xslweb/error"  
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:param name="response:response"/>  
  
  <xsl:template match="/">        
    <xsl:apply-templates select="response:response"/>
    <xsl:apply-templates select="response:response/response:headers/response:header"/>
    <!--
    <xsl:sequence select="response:response/response:body/node()" exclude-result-prefixes="response"/>
    -->    
    <xsl:sequence select="response:response/response:body/node()"/>
  </xsl:template>
  
  <xsl:template match="response:response">
    <xsl:value-of select="if (response:status(@status)) then () else error(xs:QName('error:response-status'), 'Could not set status of response')"/>       
  </xsl:template>
  
  <xsl:template match="response:header">
    <xsl:value-of select="if (response:header(@name, @value)) then () else error(xs:QName('error:response-header'), 'Could not set header of response')"/>
  </xsl:template>
  
  <!--
  <xsl:template match="response:body">
    <xsl:apply-templates exclude-result-prefixes="response"/>
  </xsl:template>
  -->
    
  <xsl:template match="node()|@*" mode="debug">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*" mode="debug"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>