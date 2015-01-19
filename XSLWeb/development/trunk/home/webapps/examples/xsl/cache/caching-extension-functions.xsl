<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"  
  xmlns:session="http://www.armatiek.com/xslweb/session"
  xmlns:context="http://www.armatiek.com/xslweb/functions/context"
  xmlns:webapp="http://www.armatiek.com/xslweb/functions/webapp"
  xmlns:ser="http://www.armatiek.com/xslweb/functions/serialize"
  xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization"  
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Example 14: Caching extension functions</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <p>In this example a sequence of three integers is written and then read from the named 
      webapp cache <i>atomic-cache-name</i> using extension functions. The name of the cache 
      key is <i>atomic-cache-key-name</i>, the time-to-idle and time-to-live of the cache value
      are 10 seconds.</p>
    
    <xsl:variable name="atomic-cache-values" as="xs:integer*" select="(1, 2, 3)"/>
    <xsl:variable name="time-to-idle" as="xs:integer" select="10"/>
    <xsl:variable name="time-to-live" as="xs:integer" select="10"/>
    
    <xsl:value-of select="webapp:set-cache-value('atomic-cache-name', 'atomic-cache-key-name', 
      $atomic-cache-values, $time-to-idle, $time-to-live)"/>                
    <xsl:for-each select="webapp:get-cache-value('atomic-cache-name', 'atomic-cache-key-name')">
      <xsl:value-of select="."/><br/>
    </xsl:for-each>          
  </xsl:template>
  
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">cache-extension-functions</xsl:variable>
  
</xsl:stylesheet>