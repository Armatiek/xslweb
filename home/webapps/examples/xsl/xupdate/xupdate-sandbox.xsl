<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:xquery="http://www.armatiek.com/xslweb/functions/xquery"
  xmlns:ser="http://www.armatiek.com/xslweb/functions/serialize"
  xmlns:util="http://www.armatiek.com/xslweb/functions/util"
  xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization"  
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Example 32: XUpdate - sandbox</xsl:template>
  
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
      name="xupdateform"          
      enctype="multipart/form-data"
      action="{/*/req:context-path}{/*/req:webapp-path}/xupdate-sandbox.html">
      <fieldset>
        <label for="xml" style="width: 400px; float: left; margin: 0 20px 0 0;">
          <b>XML document</b>
          <textarea name="xml" id="xml" style="width: 400px; height: 200px; border: 1px solid #000; padding: 5px;">
            <xsl:choose>
              <xsl:when test="normalize-space($params[@name='xml'])">
                <xsl:value-of select="replace(replace($params[@name='xml'],'\s+$',''),'^\s+','')"/>
              </xsl:when>
              <xsl:otherwise><![CDATA[<root>
  <a>
    <b>Hello</b>
    <c/>
    <d/>
  </a>
</root>]]></xsl:otherwise>
            </xsl:choose>
          </textarea>
        </label>
        <label for="xquery" style="width: 400px; float: left; margin: 0 20px 0 0;">
          <b>XUpdate query</b>
          <textarea name="xquery" id="xquery" style="width: 400px; height: 200px; border: 1px solid #000; padding: 5px;">
            <xsl:choose>
              <xsl:when test="normalize-space($params[@name='xquery'])">
                <xsl:value-of select="replace(replace($params[@name='xquery'],'\s+$',''),'^\s+','')"/>
              </xsl:when>
              <xsl:otherwise><![CDATA[xquery version "1.0";
(
  delete node /root/a/c,
  insert node ' World!' as last into //b,
  insert node (attribute { 'x' } { 'y' }, 'text') into //d
)]]></xsl:otherwise>  
            </xsl:choose>
          </textarea>
        </label>
        <br/>
        <input type="submit" value="Execute XUpdate query"/>
      </fieldset>        
    </form>
    
    <xsl:if test="normalize-space($params[@name='xml']/req:value) and normalize-space($params[@name='xquery']/req:value)">
      
      <pre class="prettyprint lang-xml linenums">
        <xsl:sequence select="
          ser:serialize(
            xquery:xupdate(
              util:parse($params[@name='xml']/req:value[1]), 
              $params[@name='xquery']/req:value[1]
            ),
            $output-parameters
          )"/>
      </pre>
      
    </xsl:if>
    
    
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">xupdate-sandbox</xsl:variable>
  
</xsl:stylesheet>