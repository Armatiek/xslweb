<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:function="http://www.armatiek.com/xslweb/functions/function"
  xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization"
  xmlns:err="http://www.w3.org/2005/xqt-errors"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Example 21: Dynamic functions extensions</xsl:template>
  
  <xsl:variable name="function-ns-uri" as="xs:string">http://www.armatiek.com/xslweb/functions/tests</xsl:variable>
  
  <xsl:template name="register-function">
    <xsl:param name="function-name" as="xs:QName"/>
    <xsl:param name="code-units" as="xs:string*"/>
    <xsl:variable name="result" select="function:register($function-name, $code-units)" as="element(function:diagnostics)?"/>
    <xsl:if test="$result/function:diagnostic/@kind = 'ERROR'">
      <p>Error registering function <xsl:value-of select="$function-name"/>:</p>
      <pre class="prettyprint lang-xml linenums">
        <xsl:sequence select="serialize($result, $output-parameters)"/>
      </pre>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="test-1">
    <xsl:try>
      <p>Calculates the longest common subsequence of two simple Strings</p>
      <xsl:variable name="function-name" select="QName($function-ns-uri, 'lcs')" as="xs:QName"/>
      <xsl:variable name="code-units" as="xs:string+">
        <xsl:text>
        <![CDATA[
        import org.apache.commons.text.similarity.LongestCommonSubsequence;
    
        public class LongestCommonSubsequenceFunction {
    
          public String call(String left, String right) {
            LongestCommonSubsequence lcs = new LongestCommonSubsequence();
            return lcs.longestCommonSubsequence(left, right).toString();
          }
          
        }
        ]]>  
        </xsl:text>
      </xsl:variable>
      <xsl:call-template name="register-function">
        <xsl:with-param name="function-name" select="$function-name" as="xs:QName"/>
        <xsl:with-param name="code-units" select="$code-units" as="xs:string+"/>
      </xsl:call-template>
      <p>
        <xsl:sequence select="function:call($function-name, 'ABACCD', 'ACDF')"/>
      </p>
      <xsl:catch>
        <p>
          <xsl:value-of select="$err:description || ' (' || $err:code || ' line: ' || $err:line-number || ', column: ' || $err:column-number || ')'"/>  
        </p>
      </xsl:catch>
    </xsl:try>
  </xsl:template>
  
  <xsl:template name="test-2">
    <xsl:try>
      <p>Sorts the specified range of a float array into ascending order.</p>
      <xsl:variable name="function-name" select="QName($function-ns-uri, 'sort-floats')" as="xs:QName"/>
      <xsl:variable name="code-units" as="xs:string+">
        <xsl:text>
        <![CDATA[
        public class SortFloatsFunction {
  
          public float[] call(float[] a, int fromIndex, int toIndex) {
            java.util.Arrays.sort(a, fromIndex, toIndex);
            return a;
          }
        
        }
        ]]>  
        </xsl:text>
      </xsl:variable>
      <xsl:call-template name="register-function">
        <xsl:with-param name="function-name" select="$function-name" as="xs:QName"/>
        <xsl:with-param name="code-units" select="$code-units" as="xs:string+"/>
      </xsl:call-template>
      <p>
        <xsl:sequence select="function:call($function-name, (1.2, 1.3, 1.5), 1, 2)"/>
      </p>
      <xsl:catch>
        <p>
          <xsl:value-of select="$err:description || ' (' || $err:code || ' line: ' || $err:line-number || ', column: ' || $err:column-number || ')'"/>  
        </p>
      </xsl:catch>
    </xsl:try>
  </xsl:template>
  
  <xsl:template name="test-3">
    <xsl:try>
      <p>Void return value, use of implicit object XPathContext</p>
      <xsl:variable name="function-name" select="QName($function-ns-uri, 'void')" as="xs:QName"/>
      <xsl:variable name="code-units" as="xs:string+">
        <xsl:text>
        <![CDATA[
        import net.sf.saxon.expr.XPathContext;
        
        public class VoidFunction {
  
          public void call(XPathContext context, String text) {
            System.out.println(text + ", your Saxon edition is: " + context.getConfiguration().getEditionCode());
          }
        
        }
        ]]>  
        </xsl:text>
      </xsl:variable>
      <xsl:call-template name="register-function">
        <xsl:with-param name="function-name" select="$function-name" as="xs:QName"/>
        <xsl:with-param name="code-units" select="$code-units" as="xs:string+"/>
      </xsl:call-template>
      <p>
        <xsl:sequence select="function:call($function-name, 'Hello World')"/>
      </p>
      <xsl:catch>
        <p>
          <xsl:value-of select="$err:description || ' (' || $err:code || ' line: ' || $err:line-number || ', column: ' || $err:column-number || ')'"/>  
        </p>
      </xsl:catch>
    </xsl:try>
  </xsl:template>
  
  <xsl:template name="tab-contents-1">
    <xsl:call-template name="test-1"/>
    <xsl:call-template name="test-2"/>
    <xsl:call-template name="test-3"/>
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