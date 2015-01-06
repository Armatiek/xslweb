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
        <title>XSLWeb examples</title>
      </head>
      <body>    
        <h3>XSLWeb examples</h3>
        <ul>
          <xsl:variable name="path" select="concat(/*/req:context-path, /*/req:webapp-path)" as="xs:string"/>
          <li>
            <a href="{$path}/hello-world.html">Hello world</a>
          </li>
          <li>
            <a href="{$path}/static.html">Static files (i.e. images and css files)</a>
          </li>          
          <li>
            HTTP Reponse Headers <a href="{$path}/headers-extension-function.html">using extension functions</a> or <a href="{$path}/headers-response.html">using Response XML</a>
          </li>
          <li>
            Cookies <a href="{$path}/cookies-extension-function.html">using extension function</a> or <a href="{$path}/cookies-response.html">using Response XML</a>
          </li>
          <li>
            Session (TODO)
          </li>
          <li>
            <a href="{$path}/upload.html">File upload</a>
          </li>
          <li>
            <a href="{$path}/authentication/authentication.html">Basic authentication (credentials: guest/secret)</a>
          </li>
          <li>
            <a href="{$path}/expath-file.html">EXPath File Handling</a>
          </li>
          <li>
            <a href="{$path}/expath-http.html">EXPath HTTP Client</a>
          </li>
          <li>
            <!--
            <a href="{$path}/expath-zip.html">EXPath ZIP Facility</a>
            -->
            EXPath ZIP Facility (nog geen voorbeeld beschikbaar) 
          </li>
          <li>
            <a href="{$path}/email.html">E-Mail extension function</a> (first set mail parameters in webapp.xml)
          </li>
          <li>
            <a href="{$path}/attributes.html">Session/Webapp/Context attributes example</a>
          </li>
          <li>
            <a href="{$path}/custom-extension-function.html">Custom XPath extension function</a>
          </li>
          <li>
            <a href="{$path}/log/log.html">Logging</a>
          </li>
          <li>
            <a href="{$path}/json/json.html">JSON serialization</a>
          </li>
          <li>
            <a href="{$path}/cache/cache.html">Caching</a>
          </li>
        </ul>
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>