<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"  
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:output method="xhtml" indent="yes" omit-xml-declaration="yes"/>
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:body>
        <xsl:call-template name="body"/>
      </resp:body>
    </resp:response>          
  </xsl:template>
  
  <xsl:template name="body">
    <html>
      <head>
        <title>XSLWeb Documentation</title>
      </head>
      <body>    
        <h3>XSLWeb Documentation</h3>
        <ul>   
          <li>
            <a href="quickstart.html" target="basex">XSLWeb Quick Start</a>
          </li>
          <!--
          <li>
            <a href="http://www.xsltfunctions.com" target="basex">XSLWeb BaseX module</a>
          </li>
          <li>
            <a href="http://www.xsltfunctions.com" target="exist">XSLWeb eXist/DB module</a>
          </li>
          -->
          <li>
            <a href="http://expath.org/spec/file" target="expath-file">EXPath File Handling</a>
          </li>
          <li>
            <a href="http://expath.org/spec/http-client" target="expath-httpclient">EXPath HTTP Client</a>
          </li>
          <li>
            <a href="http://expath.org/spec/zip" target="expath-zip">EXPath ZIP Facility</a>
          </li>
          <li>
            <a href="http://www.xsltfunctions.com" target="functx">XSLWeb Email Module</a>
          </li>
          <li>
            <a href="http://www.xsltfunctions.com" target="functx">FunctX XSLT Function Library</a>
          </li>                    
        </ul>
        <a href="{/*/req:context-path}/examples">XSLWeb examples</a>
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>