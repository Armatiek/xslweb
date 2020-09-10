<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:output method="xhtml" indent="yes" omit-xml-declaration="yes"/>
  
  <xsl:param name="config:version" as="xs:string"/>
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:body>
        <xsl:call-template name="body"/>
      </resp:body>
    </resp:response>          
  </xsl:template>
  
  <xsl:template name="body">
    <html>
      <head>
        <title>It works!</title>
        <xsl:variable name="base-path" select="concat(/*/req:context-path, /*/req:webapp-path)" as="xs:string"/>
        <link href="{$base-path}/styles/base.css" type="text/css" rel="stylesheet"/>
      </head>
      <body>        
        <h1>It works!</h1>        
        <p>Go to the <a href="https://armatiek.github.io/xslweb/XSLWeb%20Developer%20Manual.html" target="_blank">documentation</a> 
          or to the <a href="{/*/req:context-path}/examples">examples</a></p>
        <br/>        
        <p><i>XSLWeb version <xsl:value-of select="$config:version"/></i></p>
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>