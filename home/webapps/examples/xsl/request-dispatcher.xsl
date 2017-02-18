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
  
  <xsl:include href="../../../common/xsl/system/authentication/basic-authentication.xsl"/>
  
  <xsl:param name="config:development-mode" as="xs:boolean"/>

  <!-- Examples index page: -->
  <xsl:template name="index" match="/req:request[req:path = '/']">    
    <pipeline:pipeline>
      <pipeline:transformer name="index" xsl-path="index.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
      
  <!-- Examples 1: Hello World -->
  <xsl:template name="hello-world" match="/req:request[req:path = '/hello-world.html']">   
    <pipeline:pipeline>
      <pipeline:transformer name="hello-world" xsl-path="hello-world/hello-world.xsl" log="true"/>
    </pipeline:pipeline>              
  </xsl:template>
      
  <!-- Examples 2: Hello world (with dynamic generated pipeline) German or French -->
  <xsl:template name="hello-world-dynamic" match="/req:request[req:path = '/hello-world-dynamic.html']">
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
  
  <!-- Example 3: Pipeline of multiple transformations -->
  <xsl:template name="pipeline" match="/req:request[req:path = '/pipeline.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="pipeline-step1" xsl-path="pipeline/pipeline-step1.xsl" log="true"/>  
      <pipeline:transformer name="pipeline-step2" xsl-path="pipeline/pipeline-step2.xsl" log="true"/>  
      <pipeline:transformer name="pipeline-step3" xsl-path="pipeline/pipeline-step3.xsl" log="true"/>  
      <pipeline:transformer name="pipeline-step4" xsl-path="pipeline/pipeline-step4.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 4: Serving static files (i.e. images, javascript and css files) -->
  <xsl:template name="static" match="/req:request[req:path = '/static.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="static" xsl-path="static/static.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 5: HTTP Reponse Headers using extension functions or using Response XML -->
  <xsl:template name="headers-extension-function" match="/req:request[req:path = '/headers-extension-function.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="headers-extension-function" xsl-path="headers/headers-extension-function.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template name="headers-response" match="/req:request[req:path = '/headers-response.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="headers-response" xsl-path="headers/headers-response.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 6: Cookies using extension function or using Response XML -->
  <xsl:template name="cookies-extension-function" match="/req:request[req:path = '/cookies-extension-function.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="cookies-extension-function" xsl-path="cookies/cookies-extension-function.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template name="cookies-response" match="/req:request[req:path = '/cookies-response.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="cookies-response" xsl-path="cookies/cookies-response.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 7: Session -->
  
  <!-- Example 8: File upload -->
  <xsl:template name="upload-form" match="/req:request[req:path = '/upload.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="upload-form" xsl-path="upload/upload-form.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template name="upload-save" match="/req:request[req:path = '/upload-save.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="upload-save" xsl-path="upload/upload-save.xsl" log="true"/>   
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 9: User authentication (credentials: guest/secret) -->
  <xsl:template name="authentication" match="/req:request[req:path = '/authentication/authentication.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="authentication" xsl-path="authentication/authentication.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 10: File handling with EXPath extension functions -->
  <xsl:template name="expath-file" match="/req:request[req:path = '/expath-file.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="expath-file" xsl-path="expath-file/expath-file.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 11: HTTP Client using EXPath extension functions -->
  <xsl:template name="expath-http" match="/req:request[req:path = '/expath-http.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="expath-http" xsl-path="expath-http/expath-http.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 12: E-Mail extension function -->
  <xsl:template name="email-form" match="/req:request[req:path = '/email.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="email-form" xsl-path="email/email-form.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template name="email-send" match="/req:request[req:path = '/email-send.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="email-send" xsl-path="email/email-send.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 13: Session/Webapp/Context attributes -->
  <xsl:template name="attributes" match="/req:request[req:path = '/attributes.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="attributes" xsl-path="attributes/attributes.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 13: Response caching -->
  <xsl:template name="responsecaching" match="/req:request[req:path = '/responsecaching.html']">    
    <pipeline:pipeline 
      cache="true" 
      cache-key="{concat(/*/req:method, /*/req:request-URI, /*/req:query-string)}" 
      cache-time-to-live="320"
      cache-time-to-idle="320"
      cache-scope="webapp"
      cache-headers="false">
      <pipeline:transformer name="responsecaching" xsl-path="caching/responsecaching.xsl" log="true"/>
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 14: Caching extension functions -->
  <xsl:template name="caching-extension-functions" match="/req:request[req:path = '/caching-extension-functions.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="caching-extension-functions" xsl-path="caching/caching-extension-functions.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 14: Logging -->
  <xsl:template name="log" match="/req:request[req:path = '/log.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="log" xsl-path="log/log.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 16: Job Scheduling -->
  <xsl:template name="job-scheduling" match="/req:request[req:path = '/job-scheduling.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="job-scheduling" xsl-path="job-scheduling/job-scheduling.xsl" log="true"/>       
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/execute-writetime-job.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="execute-writetime-job" xsl-path="job-scheduling/writetime-job.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 17: Nested pipeline -->
  <xsl:template name="nested-pipeline" match="/req:request[req:path = '/nestingpipeline.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="nested-pipeline" xsl-path="nestedpipeline/nestingpipeline.xsl" log="true"/>       
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/nestedpipeline.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="nested-pipeline" xsl-path="nestedpipeline/nestedpipeline.xsl" log="true"/>       
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 18: JSON -->
  <xsl:template match="/req:request[req:path = '/json-serialization.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="json-serialization" xsl-path="json/json-serialization.xsl" log="true"/>
      <pipeline:json-serializer 
        name="json" 
        log="true"
        auto-array="false"
        pretty-print="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template name="json-extension-functions" match="/req:request[req:path = '/json-extension-functions.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="json-extension-functions" xsl-path="json/json-extension-functions.xsl" log="true"/>       
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 19: Custom XPath extension function: -->
  <xsl:template name="custom-extension-function" match="/req:request[req:path = '/custom-extension-function.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="custom-extension-function" xsl-path="custom-extension-function/custom-extension-function.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 20: SOAP client/SOAP server -->
  <xsl:template name="soap-client" match="/req:request[req:path = '/soap-client.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="soap-client" xsl-path="soap/soap-client.xsl" log="true"/>       
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 21: Script extension function -->
  <xsl:template name="script" match="/req:request[req:path = '/script.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="script" xsl-path="script/script.xsl" log="true"/>       
    </pipeline:pipeline>
  </xsl:template>
  
  <xsl:template match="/req:request[req:path = '/Calculator']">    
    <pipeline:pipeline>
      <xsl:choose>
        <xsl:when test="/req:request/req:parameters/req:parameter[@name='wsdl']">
          <pipeline:transformer name="soap-wsdl" xsl-path="soap/soap-wsdl.xsl" log="true"/>      
        </xsl:when>
        <xsl:otherwise>
          <pipeline:transformer name="soap-server" xsl-path="soap/soap-server.xsl" log="true"/>  
        </xsl:otherwise>
      </xsl:choose>
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 22: Relational database access -->
  <xsl:template name="relational-database" match="/req:request[req:path = '/relational-database.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="relational-database" xsl-path="relational-database/relational-database.xsl" log="true"/>       
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 23: Zip serialization -->
  <xsl:template match="/req:request[req:path = '/zip-serialization.zip']">    
    <pipeline:pipeline>
      <pipeline:transformer name="zip-serialization" xsl-path="zip/zip-serialization.xsl" log="true"/>
      <pipeline:zip-serializer name="zip" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 24: Apache FOP/PDF serialization -->
  <xsl:template name="fop-serialization" match="/req:request[req:path = '/fop-serialization.pdf']">    
    <pipeline:pipeline>
      <pipeline:transformer name="fop-serialization" xsl-path="fop/fop-serialization.xsl" log="true"/>
      <pipeline:fop-serializer name="fop" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 25: XSLT 3.0 exception handling with Saxon PE/EE -->
  <xsl:template name="saxon-xslt3-pe" match="/req:request[req:path = '/saxon-xslt3-pe.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="saxon-xslt3-pe" xsl-path="saxon-xslt3-pe/saxon-xslt3-pe.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 26: XML Validation -->
  <xsl:template name="xml-validation" match="/req:request[req:path = '/xml-validation.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="generate-sample" xsl-path="xml-validation/generate-sample.xsl" log="true"/>   
      
      <!-- First validate using XML schema: -->
      <pipeline:schema-validator 
        name="schema-validator" 
        xsl-param-namespace="http://www.armatiek.com/xslweb/validation" 
        xsl-param-name="schema-validation-report">
        <pipeline:schema-paths>
          <pipeline:schema-path>example-26.xsd</pipeline:schema-path>  
        </pipeline:schema-paths>
      </pipeline:schema-validator>
      
      <!-- Then validate using Schematron: -->
      <pipeline:schematron-validator 
        name="schematron-validator" 
        schematron-path="example-26.sch" 
        xsl-param-namespace="http://www.armatiek.com/xslweb/validation" 
        xsl-param-name="schematron-validation-report"/>
      
      <pipeline:transformer name="validation-report" xsl-path="xml-validation/validation-report.xsl"/>
      
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 27: Resource serialization -->
  <xsl:template name="resource-serialization" match="/req:request[req:path = '/resource-serialization.jpg']">    
    <pipeline:pipeline>
      <pipeline:transformer name="resource-serialization" xsl-path="resource/resource-serialization.xsl" log="true"/>
      <pipeline:resource-serializer name="resource" log="false"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 28: XML Differencing file upload -->
  <xsl:template name="differencing-fileupload" match="/req:request[req:path = '/diff-fileupload.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="differencing-fileupload" xsl-path="diff/diff.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 29: XML Differencing sandbox -->
  <xsl:template name="differencing-sandbox" match="/req:request[req:path = '/diff-sandbox.html']">    
    <pipeline:pipeline>
      <pipeline:transformer name="differencing-sandbox" xsl-path="diff/diff-sandbox.xsl" log="true"/>  
    </pipeline:pipeline>
  </xsl:template>
  
  <!-- Example 30: XQuery example: Tour -->
  <xsl:template name="tour" match="/req:request[req:path = '/tour.html']">    
    <pipeline:pipeline>
      <pipeline:query name="tour" xquery-path="tour/tour.xq" log="true"/>  
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