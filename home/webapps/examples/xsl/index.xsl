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
  version="3.0">
  
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
                <a href="{$path}/pipeline.html">Pipeline of multiple transformations</a> 
              </li>
              <li>
                <a href="{$path}/static.html">Serving static files (i.e. images, javascript and css files)</a>
              </li>          
              <li>
                HTTP Reponse Headers <a href="{$path}/headers-extension-function.html">using extension functions</a> or <a href="{$path}/headers-response.html">using Response XML</a>
              </li>
              <li>
                Cookies <a href="{$path}/cookies-extension-function.html">using extension function</a> or <a href="{$path}/cookies-response.html">using Response XML</a>
              </li>
              <li>
                <a href="{$path}/attributes.html">Session/Webapp/Context attributes using extension functions</a>
              </li>
              <li>
                <a href="{$path}/upload.html">File upload</a>
              </li>
              <li>
                <a href="{$path}/authentication/authentication.html">User authentication (credentials: guest/secret)</a>
              </li>
              <li>
                <a href="{$path}/expath-file.html">File handling with EXPath extension functions</a>
              </li>
              <li>
                <a href="{$path}/expath-http.html">HTTP Client using EXPath extension functions</a>
              </li>
              <li>
                <a href="{$path}/email.html">E-Mail extension function and webapp parameters</a> (first set correct parameters in webapp.xml)
              </li>         
              <li>
                <a href="{$path}/responsecaching.html">Response caching</a>
              </li> 
              <li>
                <a href="{$path}/caching-extension-functions.html">Caching extension functions</a>
              </li>
              <li>
                <a href="{$path}/log.html">Logging</a>
              </li>              
              <li>
                <a href="{$path}/job-scheduling.html">Job scheduling</a>
              </li> 
            </ol>
          </div>
          <div id="tabs-2">
            <ol start="17">
              <li>
                <a href="{$path}/nestingpipeline.html">Nested pipepline</a>
              </li>
              <li>
                JSON <a href="{$path}/json-serialization.html">serialization</a> or <a href="{$path}/json-extension-functions.html">extension functions</a>
              </li>
              <li>
                <a href="{$path}/custom-extension-function.html">Custom XPath extension function</a>
              </li>
              <li>
                <a href="{$path}/script.html">Script extension function</a>
              </li>
              <li>
                <a href="{$path}/function.html">Dynamic/scripted extension functions, example</a>
              </li>
              <li>
                <a href="{$path}/testbed.html">Dynamic/scripted extension functions, testbed</a>
              </li>
              <li>
                <a href="{$path}/soap-client.html">SOAP client/SOAP server</a>
              </li>
              <li>
                <a href="{$path}/relational-database.html">Relation database access</a>
              </li>
              <li>
                <a href="{$path}/zip-serialization.zip">ZIP serialization</a>
              </li>
              <li>
                <a href="{$path}/fop-serialization.pdf">Apache FOP/PDF serialization</a>
              </li>
              <li>
                <a href="{$path}/xml-validation.html">XML validation with XML Schema and Schematron</a>
              </li>
              <li>
                <a href="{$path}/resource-serialization.jpg">Resource serialization (download of custom resources)</a>
              </li>
              <li>
                <a href="{$path}/tour.html?start=a1&amp;end=52">XQuery pipeline step example: Tour</a>
              </li>
              <li>
                <a href="{$path}/huge-file.html">STX transformation step: Huge file</a>
              </li>
              <li>
                <a href="{$path}/saxon-js.html">Saxon-JS integration (Saxon EE required)</a>
              </li>
            </ol>
          </div>
        </div>
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>