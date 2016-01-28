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
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Example 12: E-mail extension function and webapp parameters</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <h3>N.B. First set the correct values for the parameters <i>hostname</i>, <i>port</i>, <i>username</i>, 
      <i>password</i> and <i>use-ssl</i> in <i>webapp.xml</i>.</h3>
    
    <form 
      method="post"
      enctype="multipart/form-data"
      action="{/*/req:context-path}{/*/req:webapp-path}/email-send.html">
      <fieldset>            
        <label for="from">From: </label>
        <input type="text" name="from" value="john.doe@company.com"/><br/>            
        <label for="to">To: </label>
        <input type="text" name="to" value="jane.doe@company.com"/>
        <input type="text" name="to"/><br/>            
        <label for="cc">Cc: </label>
        <input type="text" name="cc" value=""/>
        <input type="text" name="cc"/><br/>            
        <label for="cc">Bcc: </label>
        <input type="text" name="bcc"/>
        <input type="text" name="bcc"/><br/>            
        <label for="subject">Subject: </label>
        <input type="text" name="subject" value="Hello Jane!" size="80"/><br/>           
        <p>
          <label for="body" style="vertical-align:top">Body: </label>                            
          <textarea name="body" rows="5" cols="80">Hello Jane!</textarea><br/>              
        </p>                        
        <label for="file">Attachment: </label>
        <input type="file" name="attachment"/>
        <br/><br/>
        <input type="submit" value="Send E-mail"/>
      </fieldset>
    </form>
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string*">email-form</xsl:variable>
  
</xsl:stylesheet>