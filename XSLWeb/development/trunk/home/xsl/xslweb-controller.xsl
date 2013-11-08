<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xwc="http://www.armatiek.com/xslweb/configuration"
  xmlns:xwr="http://www.armatiek.com/xslweb/request"
  xmlns:xwp="http://www.armatiek.com/xslweb/pipeline"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:param name="xwc:properties" as="xs:string*"/>
  
  <xsl:template match="/">
    <xwp:pipeline>
      <xsl:apply-templates/>
    </xwp:pipeline>
  </xsl:template>
  
  <xsl:template match="/xwr:request[xwr:contextPath = '/hello-world']">
    <xsl:variable name="lang" select="xwr:parameters/xwr:parameter[@name='lang']/@value"/>
    <xwp:transformer path="concat('hello-world/hello-world-', $lang, '.xsl')"/>              
  </xsl:template>
  
</xsl:stylesheet>