<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet   
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"    
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:file="http://expath.org/ns/file"
  xmlns:log="http://www.armatiek.com/xslweb/functions/log"    
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:output method="text"/>
    
  <xsl:param name="config:webapp-dir" as="xs:string"/>
  
  <xsl:template match="/">
    <resp:response status="200">      
      <resp:body>
        <xsl:sequence select="file:write-text(concat($config:webapp-dir, '/xsl/job-scheduling/time.txt'), xs:string(current-dateTime()))"/>
        <xsl:sequence select="log:log('INFO', 'Log: Time written to file')"/>        
        <xsl:text>Body: Time written to file</xsl:text>
      </resp:body>
    </resp:response>   
  </xsl:template>
  
</xsl:stylesheet>