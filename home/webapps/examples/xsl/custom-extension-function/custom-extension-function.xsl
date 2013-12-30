<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"    
  xmlns:resp="http://www.armatiek.com/xslweb/response"  
  xmlns:ext="http://www.armatiek.com/xslweb/functions/custom"  
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
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <title>Custom extension function example</title>
      </head>
      <body>
        <h3>Custom extension function example</h3>        
        <p>
          <xsl:value-of select="ext:hello-world('Output of extension function')"/>
        </p>        
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>