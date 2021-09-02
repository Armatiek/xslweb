<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:email="http://www.armatiek.com/xslweb/functions/email"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:param name="email:hostname" as="xs:string"/>    
  <xsl:param name="email:port" as="xs:integer"/>  
  <xsl:param name="email:username" as="xs:string"/>  
  <xsl:param name="email:password" as="xs:string"/>  
  <xsl:param name="email:use-ssl" as="xs:boolean"/>
  <xsl:param name="email:start-tls-enabled" as="xs:boolean"/>
  <xsl:param name="email:start-tls-required" as="xs:boolean"/>
  <xsl:param name="email:ssl-check-server-identity" as="xs:boolean"/>
  
  <xsl:template name="title" as="xs:string">Example 12: E-mail extension function and webapp parameters</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <xsl:variable name="email" as="element()">
      <email:email>
        <email:hostname>
          <xsl:value-of select="$email:hostname"/>
        </email:hostname>
        <email:port>
          <xsl:value-of select="$email:port"/>
        </email:port>
        <email:username>
          <xsl:value-of select="$email:username"/>
        </email:username>
        <email:password>
          <xsl:value-of select="$email:password"/>
        </email:password>
        <email:use-ssl>
          <xsl:value-of select="$email:use-ssl"/>
        </email:use-ssl>  
        <email:start-tls-enabled>
          <xsl:value-of select="$email:start-tls-enabled"/>
        </email:start-tls-enabled>
        <email:start-tls-required>
          <xsl:value-of select="$email:start-tls-required"/>
        </email:start-tls-required>
        <email:ssl-check-server-identity>
          <xsl:value-of select="$email:ssl-check-server-identity"/>
        </email:ssl-check-server-identity>
        <xsl:variable name="params" select="/*/req:parameters/req:parameter" as="element()*"/>
        <email:from>
          <email:email>
            <xsl:value-of select="$params[@name='from']/req:value"/>
          </email:email>
          <!--
          <email:name/>
          <email:charset/>
          -->
        </email:from>          
        <xsl:for-each select="$params[@name='to']/req:value[normalize-space()]">
          <email:to>
            <email:email>
              <xsl:value-of select="."/>
            </email:email>
            <!--
            <email:name/>
            <email:charset/>
            -->
          </email:to>  
        </xsl:for-each>
        <xsl:for-each select="$params[@name='cc']/req:value[normalize-space()]">
          <email:cc>
            <email:email>
              <xsl:value-of select="."/>
            </email:email>
            <!--
            <email:name/>
            <email:charset/>
            -->
          </email:cc>  
        </xsl:for-each>
        <xsl:for-each select="$params[@name='bcc']/req:value[normalize-space()]">
          <email:bcc>
            <email:email>
              <xsl:value-of select="."/>
            </email:email>
            <!--
            <email:name/>
            <email:charset/>
            -->
          </email:bcc>  
        </xsl:for-each>
        <email:subject>
          <xsl:value-of select="$params[@name='subject']/req:value"/>
        </email:subject>
        <email:message>
          <email:text>
            <xsl:value-of select="$params[@name='body']/req:value"/>  
          </email:text> 
          <email:html>
            <xhtml:html>
              <xhtml:h2>This is the <xhtml:i>HTML</xhtml:i> version of the message body:</xhtml:h2>
              <xhtml:p><xhtml:i><xsl:value-of select="$params[@name='body']/req:value"/></xhtml:i></xhtml:p>  
            </xhtml:html>                
          </email:html>
        </email:message>            
        <xsl:for-each select="/*/req:file-uploads/req:file-upload">
          <email:attachment>
            <email:file-path>
              <xsl:value-of select="req:file-path"/>
            </email:file-path>
            <email:name>
              <xsl:value-of select="req:file-name"/>
            </email:name>
            <email:description>This is the description of the attachment</email:description>  
          </email:attachment>                                                        
        </xsl:for-each>                        
      </email:email>
    </xsl:variable>        
    <xsl:sequence select="email:send-mail($email)"/>
    <p>The mail has been sent successfully</p>
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string*">email-send</xsl:variable>
  
</xsl:stylesheet>