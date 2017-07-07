<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:va="http://www.armatiek.nl/xmlindex/virtualattribute"
  xmlns:xi="http://www.armatiek.nl/xmlindex/functions"
  xmlns:xw="http://www.armatiek.com/xslweb/functions"
  xmlns:conf="http://www.armatiek.com/xslweb/configuration"
  xmlns:pf1="local:urn1"
  xmlns:pf2="local:urn2"
  xmlns:pf3="local:urn3"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:param name="conf:webapp-dir" as="xs:string"/>
  
  <xsl:include href="../../../../common/xsl/lib/xslweb/xslweb.xsl"/>
  
  <xsl:template match="/">
    <test>
      <xsl:variable name="x" as="xs:int">100</xsl:variable>
      <xsl:sequence select="//g[xs:double(@double1) lt xs:double(13.0)]"/>
    </test>
  </xsl:template>
  
</xsl:stylesheet>