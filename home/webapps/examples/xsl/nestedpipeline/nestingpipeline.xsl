<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml" 
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <!-- config:webapp-path: "/" for root webapp or "/examples" -->
  <xsl:param name="config:webapp-path" as="xs:string"/>
  
  <xsl:template name="title" as="xs:string">Example 17: Nested pipeline</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <p>This example shows how to execute an "internal" request to another pipeline and include the output
    of the nested pipeline within the current pipeline. The example calls the second pipeline using the
    standard XSLT function <i>document()</i> with an URL that starts with the custom scheme "xslweb". The path
    part of the URI starts with the name of the webapp.</p>
        
    <xsl:variable name="node-1" as="element(greeting)">
      <greeting xmlns="">Hello World!</greeting>
    </xsl:variable>    
        
    <xsl:sequence select="req:set-attribute('node-1', $node-1)"/>    
    <xsl:sequence select="document('xslweb:///examples/nestedpipeline.html?a=b&amp;c=d&amp;a=e')"/>
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">nested-pipeline</xsl:variable>
  
</xsl:stylesheet>