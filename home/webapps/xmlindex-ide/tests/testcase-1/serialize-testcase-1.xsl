<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xi="http://www.armatiek.nl/xmlindex/functions"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:template match="/">
    <xsl:sequence select="xi:document('testcase-1.xml')"/>
  </xsl:template>
  
</xsl:stylesheet>