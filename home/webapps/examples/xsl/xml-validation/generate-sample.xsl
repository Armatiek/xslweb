<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:req="http://www.armatiek.com/xslweb/request"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:output method="xml" indent="yes"/>
  
  <xsl:template match="/">
    <xsl:sequence select="req:set-attribute('base-path', concat(/*/req:context-path, /*/req:webapp-path))"/>
    <root>
      <a a="a">This is text within the a element</a>
      <b b="b">This is text within the b element</b>
      <!-- Let's forget the element c here -->
      <d d="x">This is text within the d element</d>
    </root>
  </xsl:template>
  
</xsl:stylesheet>