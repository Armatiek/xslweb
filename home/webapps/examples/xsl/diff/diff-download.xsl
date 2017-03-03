<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:req="http://www.armatiek.com/xslweb/request" 
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:diff="http://www.armatiek.com/xslweb/functions/diff"
  xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization"  
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:output indent="yes"/>
  
  <xsl:variable name="params" select="/*/req:parameters/req:parameter" as="element(req:parameter)*"/>
  
  <xsl:variable name="diff-options" as="element(diff:options)">
    <diff:options>
      <diff:whitespace-stripping-policy value="all"/> <!-- all | ignorable | none -->
      <diff:enable-tnsm value="yes"/> <!-- Text node splitting & matching -->
      <diff:min-string-length value="8"/>
      <diff:min-word-count value="3"/>
      <diff:min-subtree-weight value="12"/>
    </diff:options>  
  </xsl:variable>
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:headers>                              
        <resp:header name="Content-Disposition">attachment; filename=diff.xml</resp:header>        
      </resp:headers>
      <resp:body>
        <xsl:sequence select="diff:diff-xml(
          document(concat('file:///', replace(/*/req:file-uploads/req:file-upload[1]/req:file-path, '\\', '/'))), 
          document(concat('file:///', replace(/*/req:file-uploads/req:file-upload[2]/req:file-path, '\\', '/'))),
          $diff-options)"/>
      </resp:body>
    </resp:response>
  </xsl:template>
 
</xsl:stylesheet>