<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:pipeline="http://www.armatiek.com/xslweb/pipeline"
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:param name="config:development-mode" as="xs:boolean"/>
  
  <xsl:template match="/req:request[req:path = '/']">    
    <pipeline:pipeline>
      <pipeline:transformer name="itworks" xsl-path="itworks.xsl"/>
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="text()"/>
  
</xsl:stylesheet>