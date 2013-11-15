<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:request="http://www.armatiek.com/xslweb/request"
  xmlns:response="http://www.armatiek.com/xslweb/response"  
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:output method="xhtml" indent="yes" omit-xml-declaration="yes"/>
  
  <xsl:template match="/">
    <response:response status="200">
      <response:body>
        <xsl:call-template name="body"/>
      </response:body>
    </response:response>          
  </xsl:template>
  
  <xsl:template name="body">
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <title>Hello World!</title>
      </head>
      <body>
        <p></p>
        <br/>
        <p>Hello World!</p>
        <p>Your IP adress is <xsl:value-of select="/request:request/request:remote-addr"/></p>
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>