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
  xmlns:err="http://expath.org/ns/error"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:import href="../common/xmlverbatim.xsl"/>
  
  <xsl:output method="xhtml" indent="yes" omit-xml-declaration="yes"/>
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:body>
        <xsl:call-template name="body"/>
      </resp:body>
    </resp:response>          
  </xsl:template>
  
  <xsl:template name="body">
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <title>Session/Webapp/Context attributes example</title>
        <link rel="stylesheet" type="text/css" href="{/*/req:context-path}{/*/req:webapp-path}/styles/xmlverbatim.css"/>
      </head>
      <body>
        <h3>Session/Webapp/Context attributes example</h3>
        
        <xsl:call-template name="session-attrs"/><hr/>  
        <xsl:call-template name="webapp-attrs"/><hr/>
        <xsl:call-template name="context-attrs"/><hr/>
        <xsl:call-template name="webapp-cache"/>
        
      </body>
    </html>
  </xsl:template>
  
  <xsl:template name="session-attrs">    
    <p>Atomic session attribute value for 'atomic-attr-name':</p>
    <xsl:variable name="session:atomic-attr-values" as="xs:integer*" select="(1, 2, 3)"/>
    <xsl:value-of select="if (empty(session:set-attribute('atomic-attr-name', $session:atomic-attr-values))) then () 
      else (error(xs:QName('err:XSLWEB0001'), 'Could not set session attribute'))"/>                
    <xsl:for-each select="session:get-attribute('atomic-attr-name')">
      <xsl:value-of select="."/><br/>
    </xsl:for-each>
    
    <p>Node session attribute value for 'node-attr-name':</p>        
    <xsl:variable name="session:node-attr-values" as="node()*">
      <node1>
        <a>a</a>
        <b>b</b>
      </node1>
      <node2>
        <c>c</c>
        <d>b</d>
      </node2>            
    </xsl:variable>          
    <xsl:value-of select="if (empty(session:set-attribute('node-attr-name', $session:node-attr-values))) then () 
      else (error(xs:QName('err:XSLWEB0001'), 'Could not set session attribute'))"/>          
    
    <xsl:for-each select="session:get-attribute('node-attr-name')">
      <tt>
        <xsl:apply-templates select="." mode="xmlverb"/>
      </tt>                          
      <br/>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="webapp-attrs">
    <p>Atomic webapp attribute value for 'atomic-attr-name':</p>
    <xsl:variable name="webapp:atomic-attr-values" as="xs:integer*" select="(1, 2, 3)"/>
    <xsl:value-of select="if (empty(webapp:set-attribute('atomic-attr-name', $webapp:atomic-attr-values))) then () 
      else (error(xs:QName('err:XSLWEB0001'), 'Could not set webapp attribute'))"/>                
    <xsl:for-each select="webapp:get-attribute('atomic-attr-name')">
      <xsl:value-of select="."/><br/>
    </xsl:for-each>
    
    <p>Node webapp attribute value for 'node-attr-name':</p>        
    <xsl:variable name="webapp:node-attr-values" as="node()*">
      <node3>
        <e>e</e>
        <f>f</f>
      </node3>
      <node4>
        <g>g</g>
        <h>h</h>
      </node4>            
    </xsl:variable>          
    <xsl:value-of select="if (empty(webapp:set-attribute('node-attr-name', $webapp:node-attr-values))) then () 
      else (error(xs:QName('err:XSLWEB0001'), 'Could not set webapp attribute'))"/>          
    
    <xsl:for-each select="webapp:get-attribute('node-attr-name')">
      <tt>
        <xsl:apply-templates select="." mode="xmlverb"/>
      </tt>                          
      <br/>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="context-attrs">
    <p>Atomic context attribute value for 'atomic-attr-name':</p>
    <xsl:variable name="context:atomic-attr-values" as="xs:integer*" select="(1, 2, 3)"/>
    <xsl:value-of select="if (empty(context:set-attribute('atomic-attr-name', $context:atomic-attr-values))) then () 
      else (error(xs:QName('err:XSLWEB0001'), 'Could not set context attribute'))"/>                
    <xsl:for-each select="context:get-attribute('atomic-attr-name')">
      <xsl:value-of select="."/><br/>
    </xsl:for-each>
    
    <p>Node context attribute value for 'node-attr-name':</p>        
    <xsl:variable name="context:node-attr-values" as="node()*">
      <node5>
        <i>i</i>
        <j>j</j>
      </node5>
      <node6>
        <k>k</k>
        <l>l</l>
      </node6>            
    </xsl:variable>          
    <xsl:value-of select="if (empty(context:set-attribute('node-attr-name', $context:node-attr-values))) then () 
      else (error(xs:QName('err:XSLWEB0001'), 'Could not set context attribute'))"/>          
    
    <xsl:for-each select="context:get-attribute('node-attr-name')">
      <tt>
        <xsl:apply-templates select="." mode="xmlverb"/>
      </tt>                          
      <br/>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="webapp-cache">
    <p>Atomic webapp attribute value for 'atomic-cache-key-name':</p>
    <xsl:variable name="webapp:atomic-cache-values" as="xs:integer*" select="(1, 2, 3)"/>
    <xsl:value-of select="if (empty(webapp:set-cache-value('atomic-cache-name', 'atomic-cache-key-name', $webapp:atomic-cache-values, 1800))) then () 
      else (error(xs:QName('err:XSLWEB0001'), 'Could not set webapp cache value'))"/>                
    <xsl:for-each select="webapp:get-cache-value('atomic-cache-name', 'atomic-cache-key-name')">
      <xsl:value-of select="."/><br/>
    </xsl:for-each>        
  </xsl:template>
  
</xsl:stylesheet>