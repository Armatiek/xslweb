<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:log="http://www.armatiek.com/xslweb/functions/log"
  xmlns:ser="http://www.armatiek.com/xslweb/functions/serialize"
  xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:output method="xhtml" indent="yes" omit-xml-declaration="yes"/>
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:body>
        <html>
          <head>
            <title>
              <xsl:call-template name="title"/>
            </title>
            <xsl:variable name="base-path" select="concat(/*/req:context-path, /*/req:webapp-path)" as="xs:string"/>
            <link href="{$base-path}/styles/google-code-prettify/xslweb.css" type="text/css" rel="stylesheet"/>
            <link href="{$base-path}/styles/jquery-ui/jquery-ui.min.css" type="text/css" rel="stylesheet"/>
            <link href="{$base-path}/styles/xslweb/base.css" type="text/css" rel="stylesheet"/>
            <script src="{$base-path}/scripts/jquery-ui/external/jquery/jquery.js" type="text/javascript"></script>
            <script src="{$base-path}/scripts/jquery-ui/jquery-ui.min.js" type="text/javascript"></script>
            <script src="{$base-path}/scripts/google-code-prettify/prettify.js" type="text/javascript"></script>
            <script>
              $(function() {
                $("#tabs").tabs();
              });
            </script>
          </head>
          <body onload="prettyPrint()">
            <xsl:call-template name="body"/>
          </body>
        </html>
      </resp:body>
    </resp:response>          
  </xsl:template>
  
  <xsl:template name="title" as="xs:string">No title</xsl:template>
  
  <xsl:template name="tab-header-1" as="xs:string">Description</xsl:template>
  
  <xsl:template name="tab-header-2">Request XML</xsl:template>
  
  <xsl:template name="tab-header-3">Request dispatcher template</xsl:template>
    
  <xsl:template name="tab-header-4">Pipeline stylesheet</xsl:template>
  
  <xsl:template name="tab-header-5">webapp.xml</xsl:template>
  
  <xsl:template name="tab-contents-1"/>
  
  <xsl:template name="tab-contents-2">
    <pre class="prettyprint lang-xml linenums">
      <xsl:variable name="output-parameters" as="node()">
        <output:serialization-parameters>
          <output:method value="xml"/>
          <output:indent value="yes"/>
        </output:serialization-parameters>  
      </xsl:variable>
      <xsl:sequence select="ser:serialize(/, $output-parameters)"/>
    </pre>
  </xsl:template>
  
  <xsl:template name="tab-contents-3">
    <pre class="prettyprint lang-xml linenums">
      <xsl:variable name="output-parameters" as="node()">
        <output:serialization-parameters>
          <output:method value="xml"/>
          <output:indent value="yes"/>
        </output:serialization-parameters>  
      </xsl:variable>
      <xsl:sequence select="ser:serialize(document('../request-dispatcher.xsl')//xsl:template[contains(@match, $dispatcher-match)], $output-parameters)"/>
    </pre>
  </xsl:template>
  
  <xsl:template name="tab-contents-4">
    <pre class="prettyprint lang-xml linenums">
      <xsl:variable name="output-parameters" as="node()">
        <output:serialization-parameters>
          <output:method value="xml"/>
          <output:indent value="yes"/>
        </output:serialization-parameters>  
      </xsl:variable>
      <xsl:sequence select="ser:serialize(document(base-uri($pipeline-xsl)), $output-parameters)"/>
    </pre>
  </xsl:template>
  
  <xsl:template name="tab-contents-5">
    <pre class="prettyprint lang-xml linenums">
      <xsl:variable name="output-parameters" as="node()">
        <output:serialization-parameters>
          <output:method value="xml"/>
          <output:indent value="yes"/>
        </output:serialization-parameters>  
      </xsl:variable>
      <xsl:sequence select="ser:serialize(document('../../webapp.xml'), $output-parameters)"/>
    </pre>
  </xsl:template>
  
  <xsl:template name="body">
    <h1>
      <xsl:call-template name="title"/>
    </h1>
    <div id="tabs">
      <ul>
        <li><a href="#tabs-1"><xsl:call-template name="tab-header-1"/></a></li>
        <li><a href="#tabs-2"><xsl:call-template name="tab-header-2"/></a></li>
        <li><a href="#tabs-3"><xsl:call-template name="tab-header-3"/></a></li>
        <li><a href="#tabs-4"><xsl:call-template name="tab-header-4"/></a></li>
        <li><a href="#tabs-5"><xsl:call-template name="tab-header-5"/></a></li>
      </ul>
      <div id="tabs-1">
        <xsl:call-template name="tab-contents-1"/>
      </div>
      <div id="tabs-2">
        <xsl:call-template name="tab-contents-2"/>
      </div>
      <div id="tabs-3">
        <xsl:call-template name="tab-contents-3"/>
      </div>
      <div id="tabs-4">
        <xsl:call-template name="tab-contents-4"/>
      </div>
      <div id="tabs-5">
        <xsl:call-template name="tab-contents-5"/>
      </div>
    </div>
  </xsl:template>
  
</xsl:stylesheet>