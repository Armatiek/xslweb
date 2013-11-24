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
      <resp:body>
        <xsl:call-template name="body"/>
      </resp:body>
    </resp:response>          
  </xsl:template>
  
  <xsl:template name="body">
    <html>
      <head>
        <title>Hello World!</title>
      </head>
      <body>
        <!-- Create image that references an image in home/static/images: -->
        <img src="{/req:request/req:context-path}/images/hello-world.jpg"/>
        <p></p>
        <br/>
        <p>Hello World!</p>
        <p>Your IP adress is <xsl:value-of select="/req:request/req:remote-addr"/></p>        
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>