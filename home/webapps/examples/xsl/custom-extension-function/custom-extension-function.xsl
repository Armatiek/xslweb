<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:ext="http://www.armatiek.com/xslweb/functions/custom"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Example 19: Custom extension function</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <p>This example calls the custom XPath extension function <i>ext:hello-world(xs:string)</i> (you could have
      written) which returns the base64 encoding of the string argument. The extension function is part of the 
      custom Java library <i>HelloWorld.jar</i> in the directory <i>&lt;&lt;webapp-home&gt;&gt;/lib.</i>
    </p>
    <p>
      Output:<br/><br/>
      <xsl:value-of select="ext:hello-world('Output of extension function')"/>  
    </p>
    <p>
      See also: <a target="_blank" href="http://www.saxonica.com/documentation/#!extensibility/integratedfunctions/ext-full-J">http://www.saxonica.com/documentation/#!extensibility/integratedfunctions/ext-full-J</a>
    </p>
    <p>The Java source code of this extension function:</p>
    <pre class="prettyprint lang-java linenums">
      <xsl:value-of select="unparsed-text('HelloWorld.java')"/>
    </pre>
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">custom-extension-function</xsl:variable>
  
</xsl:stylesheet>