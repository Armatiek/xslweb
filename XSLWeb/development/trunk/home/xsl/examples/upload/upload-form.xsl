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
        <title>File upload example</title>
      </head>
      <body>    
        <h3>File upload example</h3>
        <form 
          method="post"           
          name="uploadform"          
          enctype="multipart/form-data"
          action="{/req:request/req:context-path}/examples/upload/upload-save.html">
          <fieldset>            
            <label for="file">File 1: </label>
            <input type="file" name="file1"/>
            <br/>
            <label for="file">File 2: </label>
            <input type="file" name="file2"/>
            <br/>
            <label for="description">Description: </label>
            <input type="text" name="description"/>
            <br/>
            <input type="submit" value="Upload files"/>
          </fieldset>          
        </form>
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>