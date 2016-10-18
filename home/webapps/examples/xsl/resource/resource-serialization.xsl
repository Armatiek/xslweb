<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet   
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"    
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  xmlns:res="http://www.armatiek.com/xslweb/resource-serializer"
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
    <res:resource-serializer path="webapps/examples/xsl/resource/leaves.jpg"/>
    <!--
    <res:resource-serializer path="webapps/examples/xsl/resource/leaves.jpg" content-type="image/jpg" content-disposition-filename="my-image.jpg"/>
    -->
  </xsl:template>
  
</xsl:stylesheet>