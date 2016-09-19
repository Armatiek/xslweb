<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  xmlns:err="http://www.w3.org/2005/xqt-errors"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:output method="xhtml" indent="yes" omit-xml-declaration="yes"/>
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:param name="config:webapp-path" as="xs:string"/>
  
  <xsl:template name="title" as="xs:string">Example 25: XSLT 3.0 exception handling test with Saxon PE/EE</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <xsl:try>
      <xsl:sequence select="error(QName('http://www.w3.org/2005/xqt-errors', 'test-error'), 'This is the error description')"/>
      <xsl:catch errors="*">
        <p>An error has occured with code "<xsl:value-of select="$err:code"/>" and description "<xsl:value-of select="$err:description"/>".</p>
      </xsl:catch>
    </xsl:try> 
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">saxon-xslt3-pe</xsl:variable>
  
</xsl:stylesheet>