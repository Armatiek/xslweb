<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response" 
  xmlns:session="http://www.armatiek.com/xslweb/session"
  xmlns:ser="http://www.armatiek.com/xslweb/functions/serialize"
  xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization" 
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Authentication Example</xsl:template>
  
  <xsl:variable name="session:attr-name-userprofile" as="xs:string">xslweb-userprofile</xsl:variable>
  
  <xsl:template name="headers">
    <resp:headers>               
      <resp:header name="Expires">0</resp:header>
      <resp:header name="Pragma">no-cache</resp:header>
      <resp:header name="Cache-Control">no-store, no-cache, must-revalidate</resp:header>        
    </resp:headers> 
  </xsl:template>
  
  <xsl:template name="tab-contents-1">
    <p>You are authenticated!</p>
    <p>TODO</p>
    
    <p>This is your user profile stored in the session object:</p>
    <pre class="prettyprint lang-xml linenums">
      <xsl:sequence select="ser:serialize(session:get-attribute($session:attr-name-userprofile), $output-parameters)"/>
    </pre>
  </xsl:template>
  
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="dispatcher-match" as="xs:string">authentication.html</xsl:variable>
  
  <xsl:variable name="output-parameters" as="node()">
    <output:serialization-parameters>
      <output:method value="xml"/>
      <output:indent value="yes"/>
    </output:serialization-parameters>  
  </xsl:variable>
  
</xsl:stylesheet>