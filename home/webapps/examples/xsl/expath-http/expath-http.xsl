<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  xmlns:basex="http://basex.org/rest"  
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"  
  xmlns:http="http://expath.org/ns/http-client"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:import href="../common/xmlverbatim.xsl"/>
  
  <xsl:output method="xhtml" indent="yes" omit-xml-declaration="yes" xml:space="preserve"/>
  
  <xsl:param name="config:basex-rest-base-uri" as="xs:string"/>
  
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
        <title>EXPath HTTP Client</title>
        <link rel="stylesheet" type="text/css" href="{/*/req:context-path}{/*/req:webapp-path}/styles/xmlverbatim.css"/>        
      </head>
      <body>
        <h2>EXPath HTTP Client</h2>        
        <h3>BaseX XQuery result:</h3>        
        <xsl:variable name="request" as="element()">
          <http:request
            href="{$config:basex-rest-base-uri}"
            method="post">
            <http:body media-type="application/xml">
              <basex:query>
                <basex:text><![CDATA[ collection('bwb')/toestand[@bwb-id='BWBR0001822'] ]]></basex:text>
                <!--
                <basex:parameter name="method" value="xml"/>
                -->                
              </basex:query>
            </http:body>            
          </http:request>
        </xsl:variable>
        <xsl:variable name="response" select="http:send-request($request)" as="item()+"/>
        <h4>Response header:</h4>
        <tt>
          <xsl:apply-templates select="$response[1]" mode="xmlverb"/>
        </tt>
        <h4>Response body:</h4>
        <tt>                                      
          <xsl:apply-templates select="$response[2]" mode="xmlverb"/>                   
        </tt>                         
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>