<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:file="http://expath.org/ns/file" 
  xmlns:diff="http://www.armatiek.com/xslweb/functions/diff"
  xmlns:ser="http://www.armatiek.com/xslweb/functions/serialize"
  xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization"  
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Example 28: XML Differencing</xsl:template>
  
  <xsl:variable name="output-parameters" as="node()">
    <output:serialization-parameters>
      <output:method value="xml"/>
      <output:indent value="yes"/>
      <output:omit-xml-declaration value="yes"/>
    </output:serialization-parameters>  
  </xsl:variable>
  
  <xsl:variable name="params" select="/*/req:parameters/req:parameter" as="element(req:parameter)*"/>
  
  <xsl:template name="tab-contents-1">
    <form 
      method="post"           
      name="diffform"          
      enctype="multipart/form-data"
      action="{/*/req:context-path}{/*/req:webapp-path}/diff.html">
      <fieldset>            
        <label for="file">XML file 1: </label>
        <input type="file" name="file1"/>
        <br/><br/>
        <label for="file">XML file 2: </label>
        <input type="file" name="file2"/>
        <br/><br/>
        <input type="submit" value="Difference XML files"/>
      </fieldset>          
    </form>
    
    <xsl:if test="/*/req:file-uploads[count(req:file-upload) = 2]">
      <pre class="prettyprint lang-xml linenums">
        <xsl:sequence select="
          ser:serialize(
            diff:diff-xml(
              document(concat('file:///', replace(/*/req:file-uploads/req:file-upload[1]/req:file-path, '\\', '/'))), 
              document(concat('file:///', replace(/*/req:file-uploads/req:file-upload[2]/req:file-path, '\\', '/'))),
              'xydelta',
              'all'
            ),
            $output-parameters
          )"/>
      </pre>
      
      <pre class="prettyprint lang-xml linenums">
        <xsl:sequence select="
          ser:serialize(
            diff:diff-xml(
              document(concat('file:///', replace(/*/req:file-uploads/req:file-upload[1]/req:file-path, '\\', '/'))), 
              document(concat('file:///', replace(/*/req:file-uploads/req:file-upload[2]/req:file-path, '\\', '/'))),
              'deltaxml',
              'all'
            ),
            $output-parameters
          )"/>
      </pre>
    </xsl:if>
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">diff-form</xsl:variable>
  
</xsl:stylesheet>