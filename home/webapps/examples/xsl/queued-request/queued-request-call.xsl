<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet   
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"    
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:queue="http://www.armatiek.com/xslweb/functions/queue"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:output method="xml"/>
  
  <xsl:variable name="query-params" select="/*/req:parameters/req:parameter" as="element()*"/>
  <xsl:variable name="file-name" select="$query-params[@name='file-name']/req:value" as="xs:string"/> 
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:body>
        <xsl:variable name="extra-info" as="element(extra-info)">
          <extra-info>
            <file-name>
              <xsl:value-of select="$file-name"/>
            </file-name>
          </extra-info>
        </xsl:variable>
        <ticket>
          <xsl:value-of select="queue:add-request('queue-test', 'queued-request', $extra-info)"/>  
        </ticket>
      </resp:body>
    </resp:response>          
  </xsl:template>
  
</xsl:stylesheet>