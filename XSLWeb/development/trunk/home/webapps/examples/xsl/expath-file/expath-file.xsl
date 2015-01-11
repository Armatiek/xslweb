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
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:param name="config:home-dir" as="xs:string"/>
  <xsl:param name="config:webapp-dir" as="xs:string"/>
  
  <xsl:template name="title" as="xs:string">EXPath file example</xsl:template>
  
  <xsl:template name="tab-contents-1">                
    <xsl:variable name="dir-path" select="concat($config:webapp-dir, '/static/downloads/expath-file')" as="xs:string"/>
    <xsl:variable name="file-path" select="concat($dir-path, '/text-file.txt')" as="xs:string"/>
    <xsl:variable name="file-path2" select="concat($dir-path, '/text-file2.txt')" as="xs:string"/>
    
    <p>
      <a href="{/*/req:context-path}{/*/req:webapp-path}/expath-file.html?create=">Create or append text to</a> text file <i>&lt;&lt;webapp-home&gt;&gt;</i>/static/downloads/expath-file/text-file.txt
      <xsl:if test="/*/req:parameters/req:parameter[@name='create']">            
        <xsl:variable name="lines" select="(concat('This is a the first line, created on: ', current-dateTime()),  concat('This is a the second line, created on: ', current-dateTime()))" as="xs:string+"/>
        <xsl:sequence select="file:create-dir($dir-path)"/>
        <xsl:sequence select="file:append-text-lines($file-path, $lines)"/>
      </xsl:if>
    </p>
    
    <p>
      <a href="{/*/req:context-path}{/*/req:webapp-path}/expath-file.html?view=">View</a> the contents of the text file <i>&lt;&lt;webapp-home&gt;&gt;</i>/static/downloads/expath-file/text-file.txt          
      <xsl:if test="/*/req:parameters/req:parameter[@name='view']">
        <br/><br/>                                 
        <xsl:for-each select="file:read-text-lines($file-path)">
          <xsl:value-of select="."/>
          <br/>
        </xsl:for-each>
      </xsl:if>
    </p>
    
    <p>
      <a href="{/*/req:context-path}{/*/req:webapp-path}/expath-file.html?copy=">Copy</a> the text file <i>&lt;&lt;webapp-home&gt;&gt;</i>/static/downloads/expath-file/text-file.txt to text-file2.txt           
      <xsl:if test="/*/req:parameters/req:parameter[@name='copy']">                       
        <xsl:sequence select="file:copy($file-path, $file-path2)"/>
      </xsl:if>
    </p>
    
    <p>
      <a href="{/*/req:context-path}{/*/req:webapp-path}/expath-file.html?delete=">Delete</a> the text file <i>&lt;&lt;webapp-home&gt;&gt;</i>/static/downloads/expath-file/text-file2.txt           
      <xsl:if test="/*/req:parameters/req:parameter[@name='delete']">                                                                                  
        <xsl:choose>
          <xsl:when test="file:exists($file-path2)">
            <xsl:sequence select="file:delete($file-path2)"/>
          </xsl:when>
          <xsl:otherwise>
            <br/><br/>ERROR: The file does not exists                
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </p>
    
    <p>This is the contents of your webapp home directory:</p>
    <ol>
      <xsl:for-each select="file:list($config:webapp-dir, true())">
        <li>
          <xsl:variable name="path" select="concat($config:webapp-dir, '/', .)" as="xs:string"/>
          <xsl:choose>
            <xsl:when test="file:is-file($path)">
              <xsl:value-of select="concat(., ' (', file:size($path), ' bytes)')"/>  
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="concat(., ' [DIR]')"/>
            </xsl:otherwise>
          </xsl:choose>             
        </li>          
      </xsl:for-each>
    </ol>                
  </xsl:template>
  
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="dispatcher-match" as="xs:string">expath-file.html</xsl:variable>
  
</xsl:stylesheet>