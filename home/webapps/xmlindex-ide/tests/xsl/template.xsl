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
    <xsl:variable name="url" select="xw:path-to-file-uri(concat($conf:webapp-dir, '/tests/xml/testcase-1.xml'))" as="xs:string"/>
    <xsl:variable name="doc" select="document($url)" as="document-node()"/>
    <xsl:sequence select="xi:add-document('testcase-1.xml', $doc), xi:commit(true())"/>
  </xsl:template>
  
</xsl:stylesheet>