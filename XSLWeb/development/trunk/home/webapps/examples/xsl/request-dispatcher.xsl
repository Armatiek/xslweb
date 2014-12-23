<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:pipeline="http://www.armatiek.com/xslweb/pipeline"
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  xmlns:auth="http://www.armatiek.com/xslweb/auth"
  xmlns:err="http://expath.org/ns/error"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:include href="../../../xsl/system/authentication/basic-authentication.xsl"/>
  
  <xsl:param name="config:development-mode" as="xs:string"/>
      
  <!-- Authentication functions: -->
  <xsl:function name="auth:must-authenticate" as="xs:boolean">    
    <xsl:param name="request" as="document-node()"/>       
    <xsl:value-of select="starts-with($request/*/req:path, '/authentication')"/>
  </xsl:function>
  
  <xsl:function name="auth:get-realm" as="xs:string">
    <xsl:text>XSLWeb examples realm</xsl:text>
  </xsl:function>
  
  <xsl:function name="auth:login" as="element()?">
    <xsl:param name="username" as="xs:string"/>
    <xsl:param name="password" as="xs:string"/>
    <xsl:if test="$username = 'guest' and $password = 'secret'">
      <authentication>
        <ID>
          <xsl:value-of select="$username"/>
        </ID>
        <!--
        <role>rolename</role>
        -->
        <data>    
          <email>email@email.com</email>
          <tel>1234567</tel>
          <mydata1/>
          <mydata2/>
        </data>
      </authentication>  
    </xsl:if>
  </xsl:function>
  
  <xsl:template match="/">
    <pipeline:pipeline>
      <xsl:apply-templates/>
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/']">    
    <pipeline:transformer name="index" xsl-path="index.xsl"/>              
  </xsl:template>
      
  <xsl:template match="/req:request[req:path = '/hello-world.html']">
    <xsl:variable name="lang-value" select="req:parameters/req:parameter[@name='lang']/@value" as="xs:string?"/>    
    <xsl:variable name="lang" select="if ($lang-value) then $lang-value else 'en'" as="xs:string"/>    
    <pipeline:transformer name="hello-world" xsl-path="{concat('hello-world/hello-world-', $lang, '.xsl')}"/>              
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/static.html']">    
    <pipeline:transformer name="static" xsl-path="static/static.xsl"/>              
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/expath-file.html']">    
    <pipeline:transformer name="expath-file" xsl-path="expath-file/expath-file.xsl"/>              
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/expath-http.html']">    
    <pipeline:transformer name="expath-file" xsl-path="expath-http/expath-http.xsl"/>              
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/upload.html']">    
    <pipeline:transformer name="upload-form" xsl-path="upload/upload-form.xsl"/>              
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/upload/upload-save.html']">    
    <pipeline:transformer name="upload-save" xsl-path="upload/upload-save.xsl"/>              
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/execute-writetime-job.html']">    
    <pipeline:transformer name="execute-writetime-job" xsl-path="job-scheduling/writetime-job.xsl"/>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/email.html']">    
    <pipeline:transformer name="email-form" xsl-path="email/email-form.xsl"/>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/email/email-send.html']">    
    <pipeline:transformer name="email-send" xsl-path="email/email-send.xsl"/>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/attributes.html']">    
    <pipeline:transformer name="email-send" xsl-path="attributes/attributes.xsl"/>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/custom-extension-function.html']">    
    <pipeline:transformer name="custom-extension-function" xsl-path="custom-extension-function/custom-extension-function.xsl"/>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/authentication/authentication.html']">    
    <pipeline:transformer name="authentication" xsl-path="authentication/authentication.xsl"/>
  </xsl:template>
  
</xsl:stylesheet>