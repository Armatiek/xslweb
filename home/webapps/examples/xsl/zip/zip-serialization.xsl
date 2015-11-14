<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet   
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"    
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  xmlns:zip="http://www.armatiek.com/xslweb/zip-serializer"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:param name="config:webapp-dir" as="xs:string"/>
  
  <xsl:output method="xml"/>
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:body>
        <xsl:call-template name="body"/>
      </resp:body>
    </resp:response>          
  </xsl:template>
  
  <xsl:template name="body">
    <zip:zip-serializer>
      <zip:file-entry name="file/zip-serialization.xsl" src="{concat($config:webapp-dir, '/xsl/zip/zip-serialization.xsl')}"/>        
      <zip:inline-entry name="dir1/test.xml" method="xml" encoding="UTF-8" omit-xml-declaration="no" indent="yes">
        <a>
          <b>Hello World</b>
        </a>
      </zip:inline-entry>
    </zip:zip-serializer>        
  </xsl:template>
  
</xsl:stylesheet>