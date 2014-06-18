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
      <!--
      <resp:session>
        <resp:attributes>
          <resp:attribute name="foo">
            <resp:item type="xs:string">bar1</resp:item>
            <resp:item type="node()">
              <node1>
                <x>x</x>
                <y>y</y>
              </node1>
            </resp:item>
          </resp:attribute>
        </resp:attributes>
      </resp:session>
      -->      
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
        <xsl:comment>Dit is een comment!</xsl:comment>
        <h3>Hello World!</h3>
        <p>It's <xsl:value-of select="substring(xs:string(current-time()), 1, 8)"/>, and your IP adress is <xsl:value-of select="/req:request/req:remote-addr"/></p>        
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>