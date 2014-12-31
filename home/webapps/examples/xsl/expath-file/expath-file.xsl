<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:err="http://expath.org/ns/error"  
  xmlns:file="http://expath.org/ns/file"  
  xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:output method="xhtml" indent="yes" omit-xml-declaration="yes"/>
  
  <xsl:param name="config:home-dir" as="xs:string"/>
  <xsl:param name="config:webapp-dir" as="xs:string"/>
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:body>
        <xsl:call-template name="body"/>
      </resp:body>
    </resp:response>          
  </xsl:template>
  
  <xsl:template name="body">
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <title>EXPath File Handling example</title>
      </head>
      <body>
        <h3>EXPath File Handling example</h3>
        
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
        
        <!--
        <xsl:variable name="node-1" as="node()">
          <node1>This is node 1</node1>
        </xsl:variable>
        <xsl:variable name="node-2" as="node()">
          <node2>This is node 2</node2>
        </xsl:variable>
        <xsl:variable name="output-parameters" as="node()">
          <output:serialization-parameters>
            <output:method value="xml"/>
            <output:omit-xml-declaration value="yes"/>
          </output:serialization-parameters>  
        </xsl:variable>
        <xsl:value-of select="if (file:write('c:/test.tst', ($node-1, $node-2), $output-parameters)) then () else (error(xs:QName('err:XSLWEB0001'), 'Could not write'))"/>
        -->
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>