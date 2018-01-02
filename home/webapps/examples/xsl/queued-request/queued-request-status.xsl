<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet   
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"    
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:queue="http://www.armatiek.com/xslweb/functions/queue"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:variable name="query-params" select="/*/req:parameters/req:parameter" as="element()*"/>
  <xsl:variable name="ticket" select="$query-params[@name='ticket']/req:value" as="xs:string?"/>  
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:body>
        <status>
          <xsl:value-of select="if (normalize-space($ticket) and queue:is-available($ticket)) then 'true' else 'false'"/>  
        </status>
      </resp:body>
    </resp:response>          
  </xsl:template>
  
</xsl:stylesheet>