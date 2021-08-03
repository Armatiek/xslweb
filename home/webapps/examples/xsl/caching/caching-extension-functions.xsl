<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"  
  xmlns:session="http://www.armatiek.com/xslweb/session"
  xmlns:context="http://www.armatiek.com/xslweb/functions/context"
  xmlns:webapp="http://www.armatiek.com/xslweb/functions/webapp"
  xmlns:ser="http://www.armatiek.com/xslweb/functions/serialize"
  xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization"  
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Example 14: Caching extension functions</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <p>In this example sequence of items are written en read from the named webapp cache <i>my-cache</i> 
      (which is configured in webapp.xml) using extension functions webapp:set-cache-value() and 
      webapp:get-cache-value(). The sequences will all be available in subsequent requests by users of this webapp
      until the configured <i>time-to-idle (tti)</i> or <i>time-to-live (ttl)</i> are reached.</p>
    <xsl:call-template name="atomic-items"/>
    <xsl:call-template name="node-items"/>
  </xsl:template>
  
  <xsl:template name="atomic-items"> 
    <h2>Sequence of atomic items</h2>
    <xsl:variable name="atomic-cache-values" as="xs:integer*" select="(1, 2, 3)"/>
    <xsl:sequence select="webapp:set-cache-value('my-cache', 'atomic-cache-key-name', $atomic-cache-values)"/>                
    <xsl:for-each select="webapp:get-cache-value('my-cache', 'atomic-cache-key-name')">
      <xsl:value-of select="."/><br/>
    </xsl:for-each> 
  </xsl:template>
  
  <xsl:template name="node-items">
    <h2>Sequence of node items</h2>
    <xsl:variable name="node-cache-values" as="node()*">
      <node1 xmlns="">
        <a>a</a>
        <b>b</b>
      </node1>
      <node2 xmlns="">
        <c>c</c>
        <d>b</d>
      </node2>
      <node3 xmlns="">
        <e>e</e>
        <f>f</f>
      </node3>
      <node4 xmlns="">
        <g>g</g>
        <h>h</h>
      </node4>
    </xsl:variable>  
    <xsl:sequence select="webapp:set-cache-value('my-cache', 'node-cache-key-name', $node-cache-values)"/>                
    <pre class="prettyprint lang-xml linenums">
      <xsl:for-each select="webapp:get-cache-value('my-cache', 'node-cache-key-name')">
        <xsl:sequence select="ser:serialize(., $output-parameters)"/>                          
      </xsl:for-each>
    </pre>
  </xsl:template>
  
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">caching-extension-functions</xsl:variable>
  
</xsl:stylesheet>