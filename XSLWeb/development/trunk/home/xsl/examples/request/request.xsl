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
  
  <xsl:import href="xmlverbatim.xsl"/>
  
  <xsl:output method="xhtml" indent="no" omit-xml-declaration="yes" xml:space="preserve"/>
  
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
        <title>Request example</title>
        <link rel="stylesheet" type="text/css" href="{/req:request/req:context-path}/styles/examples/request/xmlverbatim.css"/>
      </head>
      <body>        
        <h3>Request example</h3>
        <p>This is your HTTP request serialized to XML:</p> 
        <tt>
          <xsl:apply-templates select="/*" mode="xmlverb"/>  
        </tt>        
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>