<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:functx="http://www.functx.com"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Pipeline example</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <p>
      The following text is the result of concatinating words coming from four stylesheets that are executed in a pipeline:
    </p>
    <p>
      <xsl:value-of select="string-join((reverse(/req:request/word), 'pipeline'), ' ')"/>
    </p>
  </xsl:template>
  
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="dispatcher-match" as="xs:string">pipeline.html</xsl:variable>
  
</xsl:stylesheet>