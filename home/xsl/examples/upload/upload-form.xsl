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
  
  <xsl:param name="config:home-dir" as="xs:string"/>
  
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
        <form 
          method="post"           
          name="uploadform"          
          enctype="multipart/form-data"
          action="{/req:request/req:context-path}/examples/upload/upload-save.xsl">
          <fieldset>
            <ol>              
              <li>
                <label for="file">File</label>
                <input type="file" name="file"/>
              </li>
              <li>
                <label for="title">Description</label>
                <input type="text" name="title"/>
              </li>
            </ol>
          </fieldset>          
        </form>
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>