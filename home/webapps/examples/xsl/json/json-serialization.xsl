<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet   
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:fn="http://www.w3.org/2005/xpath-functions"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"       
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:output method="text"/>
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:headers>                              
        <resp:header name="Content-Type">application/json;charset=UTF-8</resp:header>    
      </resp:headers>
      <resp:body>
        <xsl:call-template name="body"/>
      </resp:body>
    </resp:response>          
  </xsl:template>
  
  <xsl:template name="body" as="xs:string">
    <xsl:variable name="xml-json" as="element(fn:map)">
      <fn:map>
        <fn:map key="a">
          <fn:string key="b">Hello World</fn:string>
          <fn:number key="c">1.0</fn:number>
          <fn:boolean key="d">true</fn:boolean>
        </fn:map>
      </fn:map>  
    </xsl:variable>
    <xsl:value-of select="fn:xml-to-json($xml-json, map{'indent':true()})"/>        
  </xsl:template>
  
</xsl:stylesheet>