<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xw="http://www.armatiek.com/xslweb/functions"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:function name="xw:path-to-file-uri" as="xs:string">
    <xsl:param name="path" as="xs:string"/>
    <xsl:variable name="protocol-prefix" as="xs:string">
      <xsl:choose>
        <xsl:when test="starts-with($path, '\\')">file://</xsl:when> <!-- UNC path -->
        <xsl:when test="matches($path, '[a-zA-Z]:[\\/]')">file:///</xsl:when> <!-- Windows drive path -->
        <xsl:when test="starts-with($path, '/')">file://</xsl:when> <!-- Unix path -->
        <xsl:otherwise>file://</xsl:otherwise>
      </xsl:choose>  
    </xsl:variable>
    <xsl:variable name="norm-path" select="translate($path, '\', '/')" as="xs:string"/>
    <xsl:variable name="path-parts" select="tokenize($norm-path, '/')" as="xs:string*"/>
    <xsl:variable name="encoded-path" select="string-join(for $p in $path-parts return encode-for-uri($p), '/')" as="xs:string"/>
    <xsl:value-of select="concat($protocol-prefix, $encoded-path)"/>        
  </xsl:function>
  
</xsl:stylesheet>