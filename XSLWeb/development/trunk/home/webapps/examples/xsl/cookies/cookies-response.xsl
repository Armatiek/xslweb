<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Example 6: Cookies using Response XML</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <p>This example adds two cookies ("cookie-1" and "cookie-2") to the response using the Response XML. 
      The cookies are then stored in the browser of the client. If this page is requested again, the cookies will show up in the request XML.</p>
    <p>Important: Cookies don't work very well on the "localhost" domain. If you run XSLWeb on localhost, first add an entry:<br/><br/>
    127.0.0.1 localhost.com<br/><br/>
      to your local hosts file and access this page using that domain or click on <a href="{concat('http://localhost.com:', /*/req:local-port, /*/req:context-path, '/examples/cookies-response.html')}">this</a> link.
    </p>
  </xsl:template>
  
  <xsl:template name="cookies">
    <resp:cookies>
      
      <!-- First cookie: -->
      <resp:cookie>
        <resp:comment>Comment 1</resp:comment>
        <resp:domain>
          <xsl:value-of select="/*/req:server-name"/>
        </resp:domain>
        <resp:max-age>-1</resp:max-age>
        <resp:name>cookie-1</resp:name>
        <resp:path>
          <xsl:value-of select="/*/req:context-path"/>
        </resp:path>
        <resp:is-secure>false</resp:is-secure>
        <resp:value>cookie-1-value</resp:value>
        <resp:version>0</resp:version>
      </resp:cookie>
      
      <!-- Second cookie: -->
      <resp:cookie>
        <resp:comment>Comment 2</resp:comment>
        <resp:domain>
          <xsl:value-of select="/*/req:server-name"/>
        </resp:domain>
        <resp:max-age>-1</resp:max-age>
        <resp:name>cookie-2</resp:name>
        <resp:path>
          <xsl:value-of select="/*/req:context-path"/>
        </resp:path>
        <resp:is-secure>false</resp:is-secure>
        <resp:value>cookie-2-value</resp:value>
        <resp:version>0</resp:version>
      </resp:cookie>
      
    </resp:cookies>
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">cookies-response</xsl:variable>
  
</xsl:stylesheet>