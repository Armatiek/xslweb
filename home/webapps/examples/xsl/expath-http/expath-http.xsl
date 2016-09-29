<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"  
  xmlns:http="http://expath.org/ns/http-client"
  xmlns:ser="http://www.armatiek.com/xslweb/functions/serialize"
  xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Example 11: HTTP Client using EXPath extension functions</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <p>This example shows the use of the EXPath HTTP Client extension function calling two REST Web services. 
      The first request adds a product to a database using a POST request, the second deletes the same product using a DELETE request.</p>
    
    <xsl:variable name="output-parameters" as="element()">
      <output:serialization-parameters>
        <output:method value="xml"/>
        <output:indent value="yes"/>
      </output:serialization-parameters>
    </xsl:variable>
    
    <xsl:variable name="request-post" as="element()">
      <http:request
        href="http://www.thomas-bayer.com/sqlrest/PRODUCT"
        method="POST">
        <http:body media-type="application/xml">
          <resource xmlns="">
            <ID>99</ID>
            <NAME>XSLWeb</NAME>
            <PRICE>999</PRICE>
          </resource>
        </http:body>            
      </http:request>
    </xsl:variable>
    
    <xsl:variable name="request-delete" as="element()">
      <http:request
        href="http://www.thomas-bayer.com/sqlrest/PRODUCT/99"
        method="DELETE"/>
    </xsl:variable>
    
    <h3>POST Request (add product to database):</h3>
    <pre class="prettyprint lang-xml linenums">
      <xsl:sequence select="ser:serialize($request-post, $output-parameters)"/>
    </pre>
    
    <!-- Execute POST request: -->
    <xsl:variable name="response-post" select="http:send-request($request-post)" as="item()+"/>
    
    <h3>POST Response:</h3>
    <pre class="prettyprint lang-xml linenums">
      <xsl:sequence select="ser:serialize($response-post, $output-parameters)"/>
    </pre>
    
    <h3>DELETE Request (delete product from database):</h3>
    <pre class="prettyprint lang-xml linenums">
      <xsl:sequence select="ser:serialize($request-delete, $output-parameters)"/>
    </pre>
    
    <!-- Execute DELETE request: -->
    <xsl:variable name="response-delete" select="http:send-request($request-delete)" as="item()+"/>
    
    <h3>DELETE Response:</h3>
    <pre class="prettyprint lang-xml linenums">
      <xsl:sequence select="ser:serialize($response-delete, $output-parameters)"/>
    </pre>
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">expath-http</xsl:variable>
  
</xsl:stylesheet>