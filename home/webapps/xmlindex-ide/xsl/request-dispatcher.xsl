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
  
  <xsl:template match="/req:request[req:path = '/']">    
    <pipeline:transformer name="main" xsl-path="main.xsl" log="true"/>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/open']">    
    <pipeline:transformer name="open" xsl-path="open.xsl" log="true"/>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/save']">    
    <pipeline:transformer name="open" xsl-path="save.xsl" log="true"/>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/run']">    
    <pipeline:transformer name="run" xsl-path="run.xsl" log="true"/>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/filesystem']">    
    <pipeline:transformer name="filesystem" xsl-path="filesystem.xsl" log="true"/>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/createfolder']">    
    <pipeline:transformer name="createfolder" xsl-path="createfolder.xsl" log="true"/>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/createfile']">    
    <pipeline:transformer name="createfile" xsl-path="createfile.xsl" log="true"/>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/deletefile']">    
    <pipeline:transformer name="deletefile" xsl-path="deletefile.xsl" log="true"/>
  </xsl:template>
  
</xsl:stylesheet>