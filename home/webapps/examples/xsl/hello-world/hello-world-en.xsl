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
   
  <xsl:output method="html" indent="yes" html-version="5.0" omit-xml-declaration="yes"/>
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:headers>                              
        <resp:header name="Pragma">no-cache</resp:header>
        <resp:header name="Cache-Control">no-store, no-cache, must-revalidate</resp:header>        
        <resp:int-header name="Expires">0</resp:int-header>
        <resp:date-header name="Last-Modified">
          <xsl:value-of select="current-dateTime()"/>
        </resp:date-header>
      </resp:headers>      
      <resp:session invalidate="true" set-max-inactive-interval="3600">
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
      <resp:cookies>
        <resp:cookie>
          <resp:comment>Hoi</resp:comment>
          <resp:domain>armatiek.nl</resp:domain>
          <resp:max-age>10</resp:max-age>
          <resp:name>Name</resp:name>
          <resp:path>/test</resp:path>
          <resp:is-secure>true</resp:is-secure>          
          <resp:value>Value</resp:value>
          <resp:version>1</resp:version>
        </resp:cookie>
      </resp:cookies>
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