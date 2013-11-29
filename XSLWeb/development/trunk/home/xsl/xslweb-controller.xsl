<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:pipeline="http://www.armatiek.com/xslweb/pipeline"
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:param name="config:development-mode" as="xs:string"/>
  
  <xsl:template match="/">
    <pipeline:pipeline>
      <xsl:apply-templates/>
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/examples']">    
    <pipeline:transformer name="index" xsl-path="examples/index.xsl"/>              
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/docs']">    
    <pipeline:transformer name="upload-form" xsl-path="docs/index.xsl"/>              
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/examples/hello-world.html']">
    <xsl:variable name="lang-value" select="req:parameters/req:parameter[@name='lang']/@value" as="xs:string?"/>    
    <xsl:variable name="lang" select="if ($lang-value) then $lang-value else 'en'" as="xs:string"/>    
    <pipeline:transformer name="hello-world" xsl-path="{concat('examples/hello-world/hello-world-', $lang, '.xsl')}"/>              
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/examples/static.html']">    
    <pipeline:transformer name="upload-form" xsl-path="examples/static/static.xsl"/>              
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/examples/request.html']">    
    <pipeline:transformer name="upload-form" xsl-path="examples/request/request.xsl"/>              
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/examples/expath-file.html']">    
    <pipeline:transformer name="upload-form" xsl-path="examples/expath-file/expath-file.xsl"/>              
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/examples/upload.html']">    
    <pipeline:transformer name="upload-form" xsl-path="examples/upload/upload-form.xsl"/>              
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/examples/upload/upload-save.html']">    
    <pipeline:transformer name="upload-save" xsl-path="examples/upload/upload-save.xsl"/>              
  </xsl:template>
  
</xsl:stylesheet>