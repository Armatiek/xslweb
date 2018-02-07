<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet   
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"    
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  xmlns:res="http://www.armatiek.com/xslweb/resource-serializer"
  xmlns:xw="http://www.armatiek.com/xslweb/functions"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:param name="config:home-dir" as="xs:string"/>
  
  <xsl:include href="../../../../common/xsl/lib/xslweb/xslweb.xsl"/>
  
  <xsl:variable name="query-params" select="/*/req:parameters/req:parameter" as="element()*"/>
  <xsl:variable name="ticket" select="$query-params[@name='ticket']/req:value" as="xs:string?"/> 
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:body>
        <xsl:variable name="file-name" select="document(xw:path-to-file-uri(concat($config:home-dir, '/queue/', $ticket, '.xml')))/extra-info/file-name" as="xs:string"/>
        <res:resource-serializer path="queue/{$ticket}.bin" content-type="application/pdf" content-disposition-filename="{$file-name}"/>
      </resp:body>
    </resp:response>          
  </xsl:template>
  
</xsl:stylesheet>