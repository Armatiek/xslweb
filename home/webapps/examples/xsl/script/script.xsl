<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:java="http://saxon.sf.net/java-type"
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:script="http://www.armatiek.com/xslweb/functions/script"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Example 20: Script extension function</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <p>In this example the two functions in the following Javascript are executed
      using the <i>script:invoke</i> extension function. The first function shows 
    how to pass an atomic sequence of strings as array argument to the Javascript 
    function and return the lengths of the strings as an array of integers, the second
    function shows how to create a Java object and use a method from Javascript.</p>
    <xsl:variable name="script" as="xs:string">
      <![CDATA[function getLengthsOfStrings(context, webapp, request, response, arrayOfStrings) { 
        var arrayOfInteger = new Array();
        for (i = 0; i < arrayOfStrings.length; i++) {
          arrayOfInteger.push(arrayOfStrings[i].length());
        }
        return arrayOfInteger;
      }
      
      function getDevelopmentModeFromWebApp(context, webapp, request, response) {         
        return webapp.getDevelopmentMode();
      }
      
      function format() {
        var importedClasses = new JavaImporter(java.text.DecimalFormat);
        with (importedClasses) {
          var decimalFormat = new DecimalFormat("###,###.###");
          return decimalFormat.format(123456.789);
        }
      }]]>
    </xsl:variable>
    <pre class="prettyprint lang-js linenums">
      <xsl:sequence select="$script"/>
    </pre>       
    <p>Output of function call <i>getLengthsOfStrings</i>:</p>
    <!-- Create/get 'nashorn' or 'graal.js' scripting engine: -->
    <xsl:variable name="engine-name" select="'nashorn'" as="xs:string"/>
    <xsl:variable name="bindings" as="map(xs:string, item())?">
      <xsl:choose>
        <xsl:when test="$engine-name = 'graal.js'">
          <xsl:map>
            <xsl:map-entry key="'polyglot.js.nashorn-compat'" select="true()"/>
          </xsl:map>
        </xsl:when>
        <xsl:otherwise/>
      </xsl:choose>
    </xsl:variable>
    
    <xsl:variable name="script-engine" select="script:get-script-engine('my-script-engine', $engine-name, $bindings)" as="java:javax.script.ScriptEngine"/>
    <xsl:sequence select="script:evaluate($script-engine, $script)"/>
    <p>
      <xsl:sequence select="script:invoke-function($script-engine, 'getLengthsOfStrings', ('France', 'Germany', 'Spain'))"/>  
    </p>
    <p>Output of function call <i>getDevelopmentModeFromWebApp</i>:</p>
    <p>
      <xsl:sequence select="script:invoke-function($script-engine, 'getDevelopmentModeFromWebApp')"/>  
    </p>
    <p>Output of function call <i>format</i>:</p>
    <p>
      <xsl:sequence select="script:invoke-function($script-engine, 'format')"/>  
    </p>
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">script</xsl:variable>
  
</xsl:stylesheet>