<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:pipeline="http://www.armatiek.com/xslweb/pipeline"
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:param name="config:development-mode" as="xs:string"/>
  
  <xsl:template match="/">
    <pipeline:pipeline>
      <xsl:apply-templates/>
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/get-file-contents']" priority="2.0">    
    <pipeline:transformer name="get-file-contents" xsl-path="get-file-contents.xsl" log="false"/>
  </xsl:template>
  
  <xsl:template match="/req:request">    
    <pipeline:transformer name="debugger" xsl-path="debugger.xsl" log="false"/>
  </xsl:template>
  
</xsl:stylesheet>