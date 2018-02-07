<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:req="http://www.armatiek.com/xslweb/request"  
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Example 8: File upload</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <form 
      method="post"           
      name="uploadform"          
      enctype="multipart/form-data"
      action="{/*/req:context-path}{/*/req:webapp-path}/upload-save.html">
      <fieldset>            
        <label for="file">File 1: </label>
        <input type="file" name="file1"/>
        <br/><br/>
        <label for="file">File 2: </label>
        <input type="file" name="file2"/>
        <br/><br/>
        <label for="description">Description: </label>
        <input type="text" name="description"/>
        <br/><br/>
        <input type="submit" value="Upload files"/>
      </fieldset>          
    </form>
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">upload-form</xsl:variable>
  
</xsl:stylesheet>