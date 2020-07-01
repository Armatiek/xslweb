<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:webapp="http://www.armatiek.com/xslweb/webapp"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:dynfunc="http://www.armatiek.com/xslweb/functions/dynfunc"
  xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization"
  xmlns:map="http://www.w3.org/2005/xpath-functions/map"
  xmlns:err="http://www.w3.org/2005/xqt-errors"
  xmlns:functx="http://www.functx.com"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:param name="webapp:webapp"/>
  
  <xsl:import href="../common/example-page.xsl"/>
  <xsl:include href="../../../../common/xsl/lib/functx/functx-1.0.xsl"/>
  
  <xsl:mode name="xsl" on-no-match="shallow-copy"/>
  
  <xsl:template name="title" as="xs:string">Example 22: Dynamic/scripted extension functions, testbed</xsl:template>
  
  <xsl:variable name="code" select="/*/req:parameters/req:parameter[@name='code']/req:value" as="xs:string?"/>
  <xsl:variable name="call" select="/*/req:parameters/req:parameter[@name='call']/req:value" as="xs:string?"/>
  
  <xsl:template name="tab-contents-1">
    <form
      method="post"           
      name="callform"          
      enctype="multipart/form-data"
      action="{/*/req:context-path}{/*/req:webapp-path}/testbed.html">
    
      <div>
        <strong>Java code:</strong><br/>
        <xsl:variable name="default-code" as="xs:string"><![CDATA[import nl.armatiek.xslweb.saxon.functions.dynfunc.ExtensionFunction;

public class MyExtensionFunctions {
  
  @ExtensionFunction(uri="http://example.com/functions/test", name="add", hasSideEffects=false)
  public int add(int x, int y) {
    return x + y;
  }
  
}]]></xsl:variable>
        <textarea name="code" cols="80" rows="12"><xsl:value-of select="if ($code) then ($code) else $default-code"/></textarea>
      </div>
      
      <br/>
      
      <div>
        <strong>Function call:</strong><br/>
        <xsl:variable name="default-call" as="xs:string">dynfunc:call(QName('http://example.com/functions/test', 'add'), 1, 2)</xsl:variable>
        <input type="text" name="call" value="{if ($call) then $call else $default-call}" size="80"/>
      </div>
      
      <br/>
      
      <div>
        <input type="submit" value="Execute call"/>  
      </div>
      
      <br/>
      
      <strong>Result:</strong><br/><br/>
      
      <xsl:if test="$code and $call">
        <div>
          <!-- Register the extension functions in the Java code: -->
          
          <xsl:try>
            <xsl:variable name="result" select="dynfunc:register($code)" as="element(dynfunc:diagnostics)?"/>
            <xsl:choose>
              <!-- Check the registration result: -->
              <xsl:when test="$result/dynfunc:diagnostic/@kind = 'ERROR'">
                <p>Error registering/compiling extension function class:</p>
                <pre class="prettyprint lang-xml linenums">
                <xsl:sequence select="serialize($result, $output-parameters)"/>
              </pre>
              </xsl:when>
              <xsl:otherwise>
                <xsl:variable name="stylesheet-node" as="node()">
                  <xsl:apply-templates select="document('call-function.xsl')" mode="xsl"/>
                </xsl:variable>
                <xsl:variable name="stylesheet-params" as="map(xs:QName, item()*)">
                  <xsl:map>
                    <xsl:map-entry key="QName('http://www.armatiek.com/xslweb/webapp', 'webapp')" select="$webapp:webapp"/>
                  </xsl:map>
                </xsl:variable>
                <xsl:variable name="options" as="map(*)">
                  <xsl:map>
                    <!--
                    <xsl:map-entry key="'stylesheet-text'" select="replace(unparsed-text('call-function.xsl', 'UTF-8'), '#function-call', functx:escape-for-regex($call))"/>
                    -->
                    <xsl:map-entry key="'stylesheet-node'" select="$stylesheet-node"/>
                    <xsl:map-entry key="'initial-template'" select="QName('', 'call-function')"/>
                    <xsl:map-entry key="'stylesheet-params'" select="$stylesheet-params"/>
                  </xsl:map>
                </xsl:variable>
                <xsl:sequence select="transform($options)?output"/>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:catch>
              <span style="color:red">
                <xsl:value-of select="$err:description || ' (' || $err:code || ', line: ' || $err:line-number || ', column: ' || $err:column-number || ')'"/>  
              </span>
            </xsl:catch>
          </xsl:try>
        </div>
      </xsl:if>
    </form>
    
  </xsl:template>
  
  <xsl:template match="xsl:sequence[@select = '#function-call']" mode="xsl">
    <xsl:copy>
      <xsl:attribute name="select" select="$call"/>
    </xsl:copy>
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">function</xsl:variable>
  
  <xsl:variable name="output-parameters" as="node()">
    <output:serialization-parameters>
      <output:method value="xml"/>
      <output:indent value="yes"/>
      <output:omit-xml-declaration value="yes"/>
    </output:serialization-parameters>  
  </xsl:variable>
  
</xsl:stylesheet>