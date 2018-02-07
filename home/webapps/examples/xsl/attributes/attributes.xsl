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
  
  <xsl:template name="title" as="xs:string">Example 7: Session/Webapp/Context attributes using extension function</xsl:template>
  
  <xsl:variable name="output-parameters" as="node()">
    <output:serialization-parameters>
      <output:method value="xml"/>
      <output:indent value="yes"/>
      <output:omit-xml-declaration value="yes"/>
    </output:serialization-parameters>  
  </xsl:variable>
  
  <xsl:template name="tab-contents-1">
    <p>In this example attributes are written and then read from 1/ the Session scope (user specific), 
      2/ the Webapp scope (web application specific) and 3/ the Context scope (XSLWeb installation). The attributes will
    all be available in subsequent requests by 1/ the same user, 2/ all users of the same webapp or 3/ all users of
    the same XSLWeb instance.</p>
    <xsl:call-template name="session-attrs"/><hr/>  
    <xsl:call-template name="webapp-attrs"/><hr/>
    <xsl:call-template name="context-attrs"/>
  </xsl:template>
  
  <xsl:template name="session-attrs"> 
    <h2>Session attributes</h2>
    <p>Set and get session attribute 'atomic-attr-name' containing sequence of integers (atomic values):</p>
    <xsl:variable name="session:atomic-attr-values" as="xs:integer*" select="(1, 2, 3)"/>
    <xsl:value-of select="session:set-attribute('atomic-attr-name', $session:atomic-attr-values)"/>                
    <xsl:for-each select="session:get-attribute('atomic-attr-name')">
      <xsl:value-of select="."/><br/>
    </xsl:for-each>
    
    <p>Set and get session attribute 'node-attr-name' containing sequence of nodes:</p>        
    <xsl:variable name="session:node-attr-values" as="node()*">
      <node1 xmlns="">
        <a>a</a>
        <b>b</b>
      </node1>
      <node2 xmlns="">
        <c>c</c>
        <d>b</d>
      </node2>            
    </xsl:variable>          
    
    <xsl:sequence select="session:set-attribute('node-attr-name', $session:node-attr-values)"/>          
    
    <pre class="prettyprint lang-xml linenums">
      <xsl:for-each select="session:get-attribute('node-attr-name')">
        <xsl:sequence select="ser:serialize(., $output-parameters)"/>                          
      </xsl:for-each>
    </pre>
  </xsl:template>
  
  <xsl:template name="webapp-attrs">
    <h2>Webapp attributes</h2>
    <p>Set and get webapp attribute 'atomic-attr-name' containing sequence of integers (atomic values):</p>
    
    <xsl:variable name="webapp:atomic-attr-values" as="xs:integer*" select="(1, 2, 3)"/>
    
    <xsl:value-of select="webapp:set-attribute('atomic-attr-name', $webapp:atomic-attr-values)"/>                
    
    <xsl:for-each select="webapp:get-attribute('atomic-attr-name')">
      <xsl:value-of select="."/><br/>
    </xsl:for-each>
    
    <p>Set and get webapp attribute 'node-attr-name' containing sequence of nodes:</p>       
    <xsl:variable name="webapp:node-attr-values" as="node()*">
      <node3 xmlns="">
        <e>e</e>
        <f>f</f>
      </node3>
      <node4 xmlns="">
        <g>g</g>
        <h>h</h>
      </node4>            
    </xsl:variable>  
    
    <xsl:value-of select="webapp:set-attribute('node-attr-name', $webapp:node-attr-values)"/>          
    
    <pre class="prettyprint lang-xml linenums">
      <xsl:for-each select="webapp:get-attribute('node-attr-name')">
        <xsl:sequence select="ser:serialize(., $output-parameters)"/>                          
      </xsl:for-each>
    </pre>
  </xsl:template>
  
  <xsl:template name="context-attrs">
    <h2>Context attributes</h2>
    <p>Set and get context attribute 'atomic-attr-name' containing sequence of integers (atomic values):</p>
    
    <xsl:variable name="context:atomic-attr-values" as="xs:integer*" select="(1, 2, 3)"/>
    
    <xsl:value-of select="context:set-attribute('atomic-attr-name', $context:atomic-attr-values)"/>                
    
    <xsl:for-each select="context:get-attribute('atomic-attr-name')">
      <xsl:value-of select="."/><br/>
    </xsl:for-each>
    
    <p>Set and get context attribute 'node-attr-name' containing sequence of nodes:</p>       
    <xsl:variable name="context:node-attr-values" as="node()*">
      <node3 xmlns="">
        <e>e</e>
        <f>f</f>
      </node3>
      <node4 xmlns="">
        <g>g</g>
        <h>h</h>
      </node4>            
    </xsl:variable>  
    
    <xsl:value-of select="context:set-attribute('node-attr-name', $context:node-attr-values)"/>          
    
    <pre class="prettyprint lang-xml linenums">
      <xsl:for-each select="context:get-attribute('node-attr-name')">
        <xsl:sequence select="ser:serialize(., $output-parameters)"/>                          
      </xsl:for-each>
    </pre>
  </xsl:template>
  
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">attributes</xsl:variable>
  
</xsl:stylesheet>