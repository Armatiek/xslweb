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
  
  <xsl:output method="xhtml" indent="yes" omit-xml-declaration="yes"/>
  
  <xsl:param name="config:home-dir" as="xs:string"/>
  
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
        
        <xsl:variable name="dir-path" select="concat($config:home-dir, '/static/downloads/examples/expath-file')" as="xs:string"/>
        <xsl:variable name="file-path" select="concat($dir-path, '/text-file.txt')" as="xs:string"/>
        <xsl:variable name="file-path2" select="concat($dir-path, '/text-file2.txt')" as="xs:string"/>
        
        <p>
          <a href="{/*/req:context-path}/examples/expath-file.html?create=">Create or append text to</a> text file <i>&lt;&lt;xslweb-home&gt;&gt;</i>/static/downloads/examples/expath-file/text-file.txt
          <xsl:if test="/*/req:parameters/req:parameter[@name='create']">            
            <xsl:variable name="lines" select="(concat('This is a the first line, created on: ', current-dateTime()),  concat('This is a the first line, created on: ', current-dateTime()))" as="xs:string+"/>
            <xsl:value-of select="if (file:create-dir($dir-path)) then () else (error(xs:QName('err:FILE9999'), 'Could not create directory'))"/>
            <xsl:value-of select="if (file:append-text-lines($file-path, $lines)) then () else (error(xs:QName('err:FILE9999'), 'Could not append lines to file'))"/>
          </xsl:if>
        </p>
        
        <p>
          <a href="{/*/req:context-path}/examples/expath-file.html?view=">View</a> the contents of the text file <i>&lt;&lt;xslweb-home&gt;&gt;</i>/static/downloads/examples/expath-file/text-file.txt          
          <xsl:if test="/*/req:parameters/req:parameter[@name='view']">
            <br/><br/>                                 
            <xsl:for-each select="file:read-text-lines($file-path)">
              <xsl:value-of select="."/>
              <br/>
            </xsl:for-each>
          </xsl:if>
        </p>
        
        <p>
          <a href="{/*/req:context-path}/examples/expath-file.html?copy=">Copy</a> the text file <i>&lt;&lt;xslweb-home&gt;&gt;</i>/static/downloads/examples/expath-file/text-file.txt to text-file2.txt           
          <xsl:if test="/*/req:parameters/req:parameter[@name='copy']">                       
            <xsl:value-of select="if (file:copy($file-path, $file-path2)) then () else (error(xs:QName('err:FILE9999'), 'Could not copy file'))"/>
          </xsl:if>
        </p>
        
        <p>
          <a href="{/*/req:context-path}/examples/expath-file.html?delete=">Delete</a> the text file <i>&lt;&lt;xslweb-home&gt;&gt;</i>/static/downloads/examples/expath-file/text-file2.txt           
          <xsl:if test="/*/req:parameters/req:parameter[@name='delete']">                                                                                  
            <xsl:choose>
              <xsl:when test="file:exists($file-path2)">
                <xsl:value-of select="if (file:delete($file-path2)) then () else (error(xs:QName('err:FILE9999'), 'Could not delete file'))"/>
              </xsl:when>
              <xsl:otherwise>
                <br/><br/>ERROR: The file does not exists                
              </xsl:otherwise>
            </xsl:choose>
          </xsl:if>
        </p>
        
        <p>This is the contents of your xslweb home directory:</p>
        <ol>
          <xsl:for-each select="file:list($config:home-dir, true())">
            <li>
              <xsl:value-of select="concat(., ' (', file:size(concat($config:home-dir, '/', .)), ' bytes)')"/>
            </li>          
          </xsl:for-each>
        </ol>
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>