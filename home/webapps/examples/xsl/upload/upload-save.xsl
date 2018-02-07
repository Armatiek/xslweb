<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:file="http://expath.org/ns/file"  
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:param name="config:home-dir" as="xs:string"/>
  <xsl:param name="config:webapp-dir" as="xs:string"/>
  
  <xsl:template name="title" as="xs:string">File upload example</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <h3>Thanks for the upload!</h3>
    
    <!-- Create target directory to copy the uploaded files to: -->
    <xsl:variable name="target-dir" select="concat($config:webapp-dir, '/static/downloads')" as="xs:string"/>
    <xsl:sequence select="file:create-dir($target-dir)"/>
    
    <p>You can download your files again from:</p>
    
    <!-- Iterate over uploaded files: -->
    <xsl:for-each select="/*/req:file-uploads/req:file-upload">                             
      
      <!-- Copy the file to the target directory: -->          
      <xsl:sequence select="file:copy(req:file-path, $target-dir)"/>
      
      <!-- Output a hyperlink to the copied file: -->
      <a href="{concat(/*/req:context-path, /*/req:webapp-path, '/downloads/', req:file-name)}">
        <xsl:sequence select="concat(req:file-name, ' (', req:size, ' bytes)')"/>
      </a>
      <br/>
    </xsl:for-each>                       
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">upload-save</xsl:variable>
  
</xsl:stylesheet>