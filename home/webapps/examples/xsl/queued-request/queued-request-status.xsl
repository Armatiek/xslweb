<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet   
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:fn="http://www.w3.org/2005/xpath-functions"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:queue="http://www.armatiek.com/xslweb/functions/queue"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:output method="text"/>
  
  <xsl:variable name="query-params" select="/*/req:parameters/req:parameter" as="element()*"/>
  <xsl:variable name="ticket" select="$query-params[@name='ticket']/req:value" as="xs:string?"/>  
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:headers>                              
        <resp:header name="Content-Type">application/json;charset=UTF-8</resp:header>    
      </resp:headers>
      <resp:body>
        <xsl:variable name="xml-json" as="element(fn:map)">
          <fn:map>
            <fn:string key="status">
              <xsl:choose>
                <xsl:when test="normalize-space($ticket)">
                  <xsl:value-of select="queue:get-status($ticket)"/>
                </xsl:when>
                <xsl:otherwise>Error: no ticket specified</xsl:otherwise>
              </xsl:choose>
            </fn:string>
          </fn:map>  
        </xsl:variable>
        <xsl:value-of select="fn:xml-to-json($xml-json)"/>
      </resp:body>
    </resp:response>          
  </xsl:template>
  
</xsl:stylesheet>