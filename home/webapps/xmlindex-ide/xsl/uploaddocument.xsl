<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:conf="http://www.armatiek.com/xslweb/configuration"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:xw="http://www.armatiek.com/xslweb/functions"
  xmlns:xix="http://www.armatiek.com/xslweb/functions/xmlindex"
  xmlns:file="http://expath.org/ns/file"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:output method="text"/>
  
  <xsl:include href="../../../common/xsl/lib/xslweb/xslweb.xsl"/>
  
  <xsl:variable name="query-params" select="/*/req:parameters/req:parameter" as="element(req:parameter)*"/>
  
  <xsl:variable name="index" select="$query-params[@name = 'index']/req:value" as="xs:string"/>
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:body>
        <xsl:call-template name="body"/>
      </resp:body>  
    </resp:response>          
  </xsl:template>
  
  <xsl:template name="body">
    <xsl:variable name="session" select="xix:get-session($index)"/>
    <xsl:for-each select="/*/req:file-uploads/req:file-upload">
      <xsl:variable name="doc" select="document(xw:path-to-file-uri(req:file-path))" as="document-node()"/>
      <xsl:sequence select="(xix:add-document($session, file:name(req:file-path), $doc), xix:commit($session))"/>
    </xsl:for-each>
    <xsl:sequence select="xix:close($session)"/>
  </xsl:template>
  
</xsl:stylesheet>