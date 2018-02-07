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
  xmlns:util="http://www.armatiek.com/xslweb/functions/util"
  xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization"  
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Example 29: XML Differencing - sandbox</xsl:template>
  
  <xsl:variable name="diff-options" as="element(diff:options)">
    <diff:options>
      <diff:whitespace-stripping-policy value="all"/> <!-- all | ignorable | none -->
      <diff:enable-tnsm value="yes"/> <!-- Text node splitting & matching -->
      <diff:min-string-length value="8"/>
      <diff:min-word-count value="3"/>
      <diff:min-subtree-weight value="12"/>
    </diff:options>  
  </xsl:variable>
  
  <xsl:variable name="output-parameters" as="element(output:serialization-parameters)">
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
      action="{/*/req:context-path}{/*/req:webapp-path}/diff-sandbox.html">
      <fieldset>
        <label for="docA" style="width: 400px; float: left; margin: 0 20px 0 0;">
          <b>Document A</b>
          <textarea name="docA" id="docA" style="width: 400px; height: 200px; border: 1px solid #000; padding: 5px;">
            <xsl:choose>
              <xsl:when test="normalize-space($params[@name='docA'])">
                <xsl:value-of select="replace(replace($params[@name='docA'],'\s+$',''),'^\s+','')"/>
              </xsl:when>
              <xsl:otherwise><![CDATA[<root xmlns:test="urn:test">
  <a><a1/></a>
  <b/>
  <c>hello world</c>
  <d color="green"/>
  <f/>
  <e>The quick brown fox jumps over the lazy dog</e>
</root>]]></xsl:otherwise>
            </xsl:choose>
          </textarea>
        </label>
        <label for="docB" style="width: 400px; float: left; margin: 0 20px 0 0;">
          <b>Document B</b>
          <textarea name="docB" id="docB" style="width: 400px; height: 200px; border: 1px solid #000; padding: 5px;">
            <xsl:choose>
              <xsl:when test="normalize-space($params[@name='docB'])">
                <xsl:value-of select="replace(replace($params[@name='docB'],'\s+$',''),'^\s+','')"/>
              </xsl:when>
              <xsl:otherwise><![CDATA[<root xmlns:test="urn:test">
  <a><a1/></a>
  <b><b1/></b>
  <c attr="hello world"/>
  <d color="red"/>
  <e>The quick brown fox jumped over the lazy dog</e>
</root>]]></xsl:otherwise>  
            </xsl:choose>
          </textarea>
        </label>
        <br/>
        <input type="submit" value="Difference XML files"/>
      </fieldset>        
    </form>
    
    <xsl:if test="normalize-space($params[@name='docA']/req:value) and normalize-space($params[@name='docB']/req:value)">
      
      <pre class="prettyprint lang-xml linenums">
        <xsl:sequence select="
          ser:serialize(
            diff:diff-xml(
              util:parse($params[@name='docA']/req:value[1]), 
              util:parse($params[@name='docB']/req:value[1]),
              $diff-options
            ),
            $output-parameters
          )"/>
      </pre>
      
    </xsl:if>
    
    
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">differencing-sandbox</xsl:variable>
  
</xsl:stylesheet>