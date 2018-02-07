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
  xmlns:functx="http://www.functx.com"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:include href="../../../../common/xsl/lib/functx/functx-1.0.xsl"/>
  
  <xsl:template name="title" as="xs:string">Example 21: SOAP Client/Server</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <p>In this example the HTTP Client EXPath extension function is used to call a SOAP webservice that is
    developed in XSLWeb.</p>
    
    <xsl:variable name="output-parameters" as="element()">
      <output:serialization-parameters>
        <output:method value="xml"/>
        <output:indent value="yes"/>
        <output:omit-xml-declaration value="yes"/>
      </output:serialization-parameters>
    </xsl:variable>
    
    <xsl:variable name="soap-request" as="element()">
      <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:tem="http://tempuri.org/">
        <soap:Header/>
        <soap:Body>
          <tem:Add>
            <tem:intA>7</tem:intA>
            <tem:intB>2</tem:intB>
          </tem:Add>
        </soap:Body>
      </soap:Envelope>
    </xsl:variable>
    
    <xsl:variable name="soap-request" as="element()">
      <http:request
        href="{concat(functx:substring-before-last(/req:request/req:request-url, '/examples'), '/examples/Calculator')}"
        method="POST"> 
        <http:header name="SOAPAction" value="http://tempuri.org/Add"/>
        <http:body media-type="application/xml">
          <xsl:sequence select="$soap-request"/>
        </http:body>            
      </http:request>
    </xsl:variable>
    
    <h3>SOAP Request:</h3>
    <pre class="prettyprint lang-xml linenums">
      <xsl:sequence select="ser:serialize($soap-request, $output-parameters)"/>
    </pre>
    
    <xsl:variable name="soap-response" select="http:send-request($soap-request)" as="item()+"/>
    
    <h3>SOAP Response:</h3>
    <pre class="prettyprint lang-xml linenums">
      <xsl:sequence select="ser:serialize($soap-response, $output-parameters)"/>
    </pre>
    
  </xsl:template>
   
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">soap-client</xsl:variable>
  
</xsl:stylesheet>