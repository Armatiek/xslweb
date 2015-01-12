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
  
  <xsl:param name="config:development-mode" as="xs:boolean"/>

  <!-- Examples index page: -->
  <xsl:template name="index" match="/req:request[req:path = '/']">    
    <pipeline:pipeline>
      <pipeline:transformer name="index" xsl-path="index.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
      
  <xsl:template match="/req:request[req:path = '/hello-world.html']">   
    <pipeline:pipeline>
      <pipeline:transformer name="hello-world" xsl-path="hello-world/hello-world.xsl" log="true"/>
    </pipeline:pipeline>              
  </xsl:template>
      
  <xsl:template match="/req:request[req:path = '/hello-world-dynamic.html']">
    <xsl:variable name="lang" select="req:parameters/req:parameter[@name='lang']/req:value[1]" as="xs:string?"/>           
    <pipeline:pipeline>
      <xsl:choose>
        <xsl:when test="$lang = 'fr'">
          <pipeline:transformer name="hello-world-fr" xsl-path="hello-world/hello-world-fr.xsl" log="true"/>    
        </xsl:when>
        <xsl:when test="$lang = 'de'">
          <pipeline:transformer name="hello-world-de" xsl-path="hello-world/hello-world-de.xsl" log="true"/>          
        </xsl:when>        
      </xsl:choose>            
    </pipeline:pipeline>              
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/pipeline.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="pipeline-step1" xsl-path="pipeline/pipeline-step1.xsl" log="true"/>  
      <pipeline:transformer name="pipeline-step2" xsl-path="pipeline/pipeline-step2.xsl" log="true"/>  
      <pipeline:transformer name="pipeline-step3" xsl-path="pipeline/pipeline-step3.xsl" log="true"/>  
      <pipeline:transformer name="pipeline-step4" xsl-path="pipeline/pipeline-step4.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/static.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="static" xsl-path="static/static.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/expath-file.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="expath-file" xsl-path="expath-file/expath-file.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/expath-http.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="expath-http" xsl-path="expath-http/expath-http.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/upload.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="upload-form" xsl-path="upload/upload-form.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/headers-extension-function.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="headers-extension-function" xsl-path="headers/headers-extension-function.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/headers-response.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="headers-response" xsl-path="headers/headers-response.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/cookies-extension-function.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="cookies-extension-function" xsl-path="cookies/cookies-extension-function.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/cookies-response.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="cookies-response" xsl-path="cookies/cookies-response.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/upload/upload-save.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="upload-save" xsl-path="upload/upload-save.xsl" log="true"/>   
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/execute-writetime-job.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="execute-writetime-job" xsl-path="job-scheduling/writetime-job.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/email.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="email-form" xsl-path="email/email-form.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/email/email-send.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="email-send" xsl-path="email/email-send.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/attributes.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="email-send" xsl-path="attributes/attributes.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/custom-extension-function.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="custom-extension-function" xsl-path="custom-extension-function/custom-extension-function.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/authentication/authentication.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="authentication" xsl-path="authentication/authentication.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/log/log.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="log" xsl-path="log/log.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/json/json.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="json" xsl-path="json/json.xsl" log="true"/>
      <pipeline:json-serializer name="json" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/cache/cache.html']">    
    <pipeline:pipeline 
      cache="true" 
      cache-key="{concat(/*/req:method, /*/req:request-URI, /*/req:query-string)}" 
      cache-time-to-live="320"
      cache-time-to-idle="320"
      cache-scope="webapp"
      cache-headers="false">
      <pipeline:transformer name="cache" xsl-path="cache/cache.xsl" log="true"/>
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/nestedpipeline/pipeline.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="pipeline" xsl-path="nestedpipeline/pipeline.xsl" log="true"/>       
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/nestedpipeline/nestedpipeline.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="nested-pipeline" xsl-path="nestedpipeline/nestedpipeline.xsl" log="true"/>       
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Authentication functions called from basic-authentication.xsl: -->
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
        <data>    
          <email>my.email@email.com</email>
          <tel>1234567</tel>
          <mydata1/>
          <mydata2/>
        </data>
      </authentication>  
    </xsl:if>
  </xsl:function>
  
  <xsl:template match="text()"/>
  
</xsl:stylesheet>