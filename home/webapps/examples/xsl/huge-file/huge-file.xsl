<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" 
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:template match="/">
    <!-- 
      Process only the interesting stuff that is available in this XSLT pipeline step,
      the rest is filtered out by the previous STX pipeline step.
    -->
    <xsl:apply-templates select="root/interesting-stuff"/>
  </xsl:template>
  
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>