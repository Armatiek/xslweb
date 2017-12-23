<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:deltaxml="http://www.armatiek.com/xslweb/diff/well-formed-delta"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:output indent="yes"/>
  
  <xsl:strip-space elements="*"/>
  
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="*[deltaxml:attributes]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:for-each select="deltaxml:attributes/*[deltaxml:attributeValue/@deltaxml:delta='B']">
        <xsl:choose>
          <xsl:when test="namespace-uri() = 'http://www.armatiek.com/xslweb/diff/non-namespaced-attribute'">
            <xsl:attribute name="{local-name()}" select="deltaxml:attributeValue[@deltaxml:delta='B']"/>    
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="{name()}" namespace="{namespace-uri()}" select="deltaxml:attributeValue[@deltaxml:delta='B']"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
    
  <xsl:template match="deltaxml:textGroup[deltaxml:text/@deltaxml:delta='B']">
    <xsl:value-of select="deltaxml:text[@deltaxml:delta='B']"/>
  </xsl:template>
  
  <xsl:template match="
    *[@deltaxml:delta='A']|
    @deltaxml:*|
    deltaxml:attributes|
    deltaxml:textGroup[not(deltaxml:text/@deltaxml:delta='B')]"/>
  
</xsl:stylesheet>