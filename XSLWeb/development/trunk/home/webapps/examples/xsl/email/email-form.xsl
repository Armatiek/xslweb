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
        <title>E-mail extension function example</title>
      </head>
      <body>        
        <h3>E-mail extension function example</h3>
        <form 
          method="post"
          enctype="multipart/form-data"
          action="{/*/req:context-path}{/*/req:webapp-path}/email/email-send.html">
          <fieldset>            
            <label for="from">From: </label>
            <input type="text" name="from" value="maarten.kroon@armatiek.nl"/><br/>            
            <label for="to">To: </label>
            <input type="text" name="to" value="maarten.kroon@armatiek.nl"/>
            <input type="text" name="to"/><br/>            
            <label for="cc">Cc: </label>
            <input type="text" name="cc" value="minder.gewenst.mk@armatiek.nl"/>
            <input type="text" name="cc"/><br/>            
            <label for="cc">Bcc: </label>
            <input type="text" name="bcc"/>
            <input type="text" name="bcc"/><br/>            
            <label for="subject">Subject: </label>
            <input type="text" name="subject" value="subject" size="80"/><br/>           
            <p>
              <label for="body" style="vertical-align:top">Body: </label>                            
              <textarea name="body" rows="5" cols="80">body</textarea><br/>              
            </p>                        
            <label for="file">Attachment: </label>
            <input type="file" name="attachment"/>
            <br/><br/>
            <input type="submit" value="Send E-mail"/>
          </fieldset>
        </form>        
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>