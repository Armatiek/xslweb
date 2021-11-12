<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:req="http://www.armatiek.com/xslweb/request" 
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:template match="/">
    <div>
      <p style="color:red">This is the output of the nested pipeline</p>
      <p style="color:red">
        <xsl:value-of select="req:get-attribute('node-1')/self::greeting/text()"/>
      </p>  
    </div>
  </xsl:template>
  
</xsl:stylesheet>