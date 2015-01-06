<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:ext="http://www.armatiek.com/xslweb/functions/custom"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Custom extension function example</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <p>This example calls the custom XPath extension function <i>ext:hello-world(xs:string)</i> which
      returns the base64 encoding of the string argument. The extension function is part of the custom Java
      library <i>HelloWorld.jar</i> in the directory <i>&lt;&lt;webapp-home&gt;&gt;/lib</i>
    </p>
    <p>
      <xsl:value-of select="ext:hello-world('Output of extension function')"/>  
    </p>
  </xsl:template>
  
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="dispatcher-match" as="xs:string">custom-extension-function.html</xsl:variable>
  
</xsl:stylesheet>