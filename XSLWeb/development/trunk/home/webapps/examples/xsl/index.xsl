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
        <title>XSLWeb Examples</title>
        <xsl:variable name="base-path" select="concat(/*/req:context-path, /*/req:webapp-path)" as="xs:string"/>
        <link href="{$base-path}/styles/jquery-ui/jquery-ui.min.css" type="text/css" rel="stylesheet"/>
        <link href="{$base-path}/styles/xslweb/base.css" type="text/css" rel="stylesheet"/>
        <script src="{$base-path}/scripts/jquery-ui/external/jquery/jquery.js" type="text/javascript"></script>
        <script src="{$base-path}/scripts/jquery-ui/jquery-ui.min.js" type="text/javascript"></script>
        <script>
          $(function() {
            $("#tabs").tabs();
          });
        </script>
      </head>
      <body>
        <h1>XSLWeb Examples</h1>        
        <div id="tabs">
          <xsl:variable name="path" select="concat(/*/req:context-path, /*/req:webapp-path)" as="xs:string"/>
          <ul>
            <li><a href="#tabs-1">Basic</a></li>
            <li><a href="#tabs-2">Advanced</a></li>
            <!--
            <li><a href="#tabs-3"><xsl:call-template name="tab-header-3"/></a></li>
            <li><a href="#tabs-4"><xsl:call-template name="tab-header-4"/></a></li>
            <li><a href="#tabs-5"><xsl:call-template name="tab-header-5"/></a></li>
            -->
          </ul>
          <div id="tabs-1">
            <ol>              
              <li>
                <a href="{$path}/hello-world.html">Hello world</a>
              </li>
              <li>
                Hello world (with dynamic generated pipeline) <a href="{$path}/hello-world-dynamic.html?lang=de">German</a> or <a href="{$path}/hello-world-dynamic.html?lang=fr">French</a> 
              </li>
              <li>
                <a href="{$path}/pipeline.html">Pipeline of transformations</a> 
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
                <a href="{$path}/email.html">E-Mail extension function</a> (first set mail parameters in webapp.xml)
              </li>
              <li>
                <a href="{$path}/attributes.html">Session/Webapp/Context attributes example</a>
              </li>              
              <li>
                <a href="{$path}/log/log.html">Logging</a>
              </li>              
              <li>
                <a href="{$path}/cache/cache.html">Caching</a>
              </li>                            
            </ol>
          </div>
          <div id="tabs-2">
            <li>
              <a href="{$path}/nestedpipeline/pipeline.html">Nested pipepline</a>
            </li>
            <li>
              <a href="{$path}/json/json.html">JSON serialization</a>
            </li>
            <li>
              <a href="{$path}/custom-extension-function.html">Custom XPath extension function</a>
            </li>
            <li>
              <a href="{$path}/soap/soap-client.html">SOAP client/SOAP server</a>
            </li>
          </div>
          <!--
          <div id="tabs-3">
            <xsl:call-template name="tab-contents-3"/>
          </div>
          <div id="tabs-4">
            <xsl:call-template name="tab-contents-4"/>
          </div>
          <div id="tabs-5">
            <xsl:call-template name="tab-contents-5"/>
          </div>
          -->
        </div>
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>