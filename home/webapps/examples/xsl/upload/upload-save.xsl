<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:file="http://expath.org/ns/file"  
  xmlns:err="http://expath.org/ns/error"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:param name="config:home-dir" as="xs:string"/>
  <xsl:param name="config:webapp-dir" as="xs:string"/>
  
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
        <title>File upload example</title>
      </head>
      <body>
        <h3>Thanks for the upload!</h3>
        <p>You can download your files again from:</p>
        
        <!-- Create target directory to copy the uploaded files to: -->
        <xsl:variable name="target-dir" select="concat($config:webapp-dir, '/static/downloads')"/>
        <xsl:value-of select="if (file:create-dir($target-dir)) then () else (error(xs:QName('err:FILE9999'), 'Could not create directory'))"/>
        
        <!-- Iterate over uploaded files: -->
        <xsl:for-each select="/*/req:file-uploads/req:file-upload">                             
          
          <!-- Copy the file to the target directory: -->          
          <xsl:value-of select="if (file:copy(req:file-path, $target-dir)) then () else (error(xs:QName('err:FILE9999'), 'Could not copy file'))"/>
          
          <!-- Output a hyperlink to the copied file: -->
          <a href="{concat(/*/req:context-path, /*/req:webapp-path, '/downloads/', req:file-name)}">
            <xsl:value-of select="concat(req:file-name, ' (', req:size, ' bytes)')"/>
          </a>
          <br/>
        </xsl:for-each>                       
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>