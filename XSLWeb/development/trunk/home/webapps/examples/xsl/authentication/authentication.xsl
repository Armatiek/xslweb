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
  
  <xsl:template name="title" as="xs:string">Example 9: User authentication (BASIC)</xsl:template>
  
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
    <p>In this example, authentication is implemented by following these steps:</p>
    <ul>
      <li>Include the stylesheet <i>basic-authentication.xsl</i> in the <i>request-dispatcher.xsl</i> stylesheet.</li>
      <li>Implement the function <i>auth:must-authenticate($request as element(request:request))): xs:boolean</i>. In 
        this function you can determine whether $request must be authenticated or not.</li>
      <li>Implement the function <i>auth:get-realm(): xs:string</i>. This function must return the authentication realm.</li>
      <li>Implement the function <i>auth:login($username as xs:string, $password as xs:string): element()?</i>.
      This function must authenticate $username with $password and returns an empty sequence if the authentication failed
      or an element() containing the user profile if it succeeded. This element must have the name <i>authentication</i> 
      and a subelement <i>ID</i>. The element <i>data</i> can be filled with arbitrary data you will need in subsequent requests.
      This element will be stored by XSLWeb in the user's session object under the name <i>xslweb-userprofile</i> so it 
      will be available in subsequent requests.</li>
    </ul>
    <p>This is your user profile stored in the session object:</p>
    <pre class="prettyprint lang-xml linenums">
      <xsl:sequence select="ser:serialize(session:get-attribute($session:attr-name-userprofile), $output-parameters)"/>
    </pre>
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">authentication</xsl:variable>
  
  <xsl:variable name="output-parameters" as="node()">
    <output:serialization-parameters>
      <output:method value="xml"/>
      <output:indent value="yes"/>
    </output:serialization-parameters>  
  </xsl:variable>
  
</xsl:stylesheet>