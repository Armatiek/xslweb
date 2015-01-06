<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Static files example</xsl:template>
  
  <xsl:template name="head" as="element()*">
    <link rel="stylesheet" type="text/css" href="{/*/req:context-path}{/*/req:webapp-path}/styles/static.css"/>
  </xsl:template>
  
  <xsl:template name="tab-contents-1">
    <p>This example shows how to embed the static image file <i>&lt;&lt;webapp-home&gt;&gt;</i>/static/images/hello-world.jpg</p>
    
    <img src="{/*/req:context-path}{/*/req:webapp-path}/images/hello-world.jpg"/>
    
    <p>This word <span class="red">red</span> is <span class="red">red</span> because that is defined in the css file <i>&lt;&lt;webapp-home&gt;&gt;</i>/static/styles/static.css</p>
  </xsl:template>
  
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="dispatcher-match" as="xs:string">static.html</xsl:variable>
  
</xsl:stylesheet>