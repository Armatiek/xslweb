<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
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
        <title>Static files example</title>
        <link rel="stylesheet" type="text/css" href="{/req:request/req:context-path}/styles/examples/static/style.css"/>
      </head>
      <body>
        <h3>Static file example</h3>
        
        <p>This is a link to a static image file in <i>&lt;&lt;xslweb-home&gt;&gt;</i>/static/images/examples/static</p>
        <!-- Create image that references an image in home/static/images: -->
        <img src="{/req:request/req:context-path}/images/examples/hello-world/hello-world.jpg"/>
        
        <p>This word <span class="red">red</span> is <span class="red">red</span> because that is defined in a static css file in <i>&lt;&lt;xslweb-home&gt;&gt;</i>/static/styles/examples/static</p>
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>