<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"   
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:output method="xhtml" indent="yes" omit-xml-declaration="yes"/>
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:headers>                              
        <resp:header name="Content-Type">text/html;charset=UTF-8</resp:header>    
      </resp:headers>
      <resp:body>
        <html>
          <head>
            <title>Hello World!</title>
          </head>
          <body>        
            <h1>Hello World!</h1>        
          </body>
        </html>
      </resp:body>
    </resp:response>          
  </xsl:template>
  
</xsl:stylesheet>