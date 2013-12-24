<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"  
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:output method="xhtml" indent="yes" omit-xml-declaration="yes"/>
  
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
        <title>XSLWeb examples</title>
      </head>
      <body>    
        <h3>XSLWeb examples</h3>
        <ul>
          <xsl:variable name="path" select="concat(/*/req:context-path, /*/req:webapp-path)" as="xs:string"/>
          <li>
            <a href="{$path}/hello-world.html">Hello world</a>
          </li>
          <li>
            <a href="{$path}/static.html">Static files (i.e. images and css files)</a>
          </li>          
          <li>
            <a href="{$path}/expath-file.html">EXPath File Handling</a>
          </li>
          <li>
            <a href="{$path}/expath-http.html">EXPath HTTP Client</a>
          </li>
          <li>
            <!--
            <a href="{$path}/expath-zip.html">EXPath ZIP Facility</a>
            -->
            EXPath ZIP Facility (nog geen voorbeeld beschikbaar) 
          </li>
          <li>
            <a href="{$path}/upload.html">File upload</a>
          </li>
        </ul>
        <a href="{/*/req:context-path}/documentation">XSLWeb documentation</a>
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>