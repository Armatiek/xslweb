<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:log="http://www.armatiek.com/xslweb/functions/log"
  xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Logging example</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <p>This example logs a sequence of two nodes and one string to the log file using specified serialization parameters. See log file after requesting ths page.</p>
    
    <xsl:variable name="node-1" as="node()">
      <node1>This is node 1</node1>
    </xsl:variable>
    
    <xsl:variable name="node-2" as="node()">
      <node2>This is node 2</node2>
    </xsl:variable>
    
    <xsl:variable name="node-3" as="xs:string">This is a string</xsl:variable>
    
    <!-- Construct serialization parameters node: -->
    <xsl:variable name="output-parameters" as="node()">
      <output:serialization-parameters>
        <output:method value="xml"/>
        <output:omit-xml-declaration value="yes"/>
      </output:serialization-parameters>  
    </xsl:variable>
    
    <!-- Log nodes: -->
    <xsl:sequence select="log:log('INFO', ($node-1, $node-2, $node-3), $output-parameters)"/>
  </xsl:template>
  
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="dispatcher-match" as="xs:string">log.html</xsl:variable>
  
</xsl:stylesheet>