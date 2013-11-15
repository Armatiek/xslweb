<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:request="http://www.armatiek.com/xslweb/request"
  xmlns:pipeline="http://www.armatiek.com/xslweb/pipeline"
  xmlns:configuration="http://www.armatiek.com/xslweb/configuration"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:param name="configuration:development-mode" as="xs:string"/>
  
  <xsl:template match="/">
    <pipeline:pipeline>
      <xsl:apply-templates/>
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/request:request[request:path = '/hello-world']">
    <xsl:variable name="lang-value" select="request:parameters/request:parameter[@name='lang']/@value" as="xs:string?"/>    
    <xsl:variable name="lang" select="if ($lang-value) then $lang-value else 'en'" as="xs:string"/>    
    <pipeline:transformer name="hello-world" xsl-path="{concat('hello-world/hello-world-', $lang, '.xsl')}"/>              
  </xsl:template>
  
</xsl:stylesheet>