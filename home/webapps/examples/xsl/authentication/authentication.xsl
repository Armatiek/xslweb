<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"  
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:output method="xhtml" indent="yes" omit-xml-declaration="yes"/>
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:headers>               
        <resp:header name="Expires">0</resp:header>
        <resp:header name="Pragma">no-cache</resp:header>
        <resp:header name="Cache-Control">no-store, no-cache, must-revalidate</resp:header>        
      </resp:headers>      
      <resp:body>
        <xsl:call-template name="body"/>
      </resp:body>
    </resp:response>          
  </xsl:template>
  
  <xsl:template name="body">
    <html>
      <head>
        <title>Authentication Example</title>
      </head>
      <body>        
        <h3>Authentication Example</h3>
        <p>You are authenticated!</p>
        <xsl:choose>
          <xsl:when test="true()">
            <a href="authentication.html?logout=true">Log out</a>    
          </xsl:when>
          <xsl:otherwise>
            <a href="authentication.html">Log in</a>
          </xsl:otherwise>
        </xsl:choose>
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>