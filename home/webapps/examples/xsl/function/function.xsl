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
  
  <xsl:variable name="code-units" as="xs:string+">
    <xsl:text>
    <![CDATA[
    import nl.armatiek.xslweb.saxon.functions.function.ExtensionFunction;
    import net.sf.saxon.expr.XPathContext;
    import org.apache.commons.text.similarity.LongestCommonSubsequence;

    public class MyExtensionFunctions {

      @ExtensionFunction(uri="http://www.armatiek.com/xslweb/functions/tests", name="lcs", hasSideEffects=false)  
      public String lcs(String left, String right) {
        LongestCommonSubsequence lcs = new LongestCommonSubsequence();
        return lcs.longestCommonSubsequence(left, right).toString();
      }
      
      @ExtensionFunction(uri="http://www.armatiek.com/xslweb/functions/tests", name="sort-floats", hasSideEffects=false)
      public float[] sortFloats(float[] a, int fromIndex, int toIndex) {
        java.util.Arrays.sort(a, fromIndex, toIndex);
        return a;
      }
      
      @ExtensionFunction(uri="http://www.armatiek.com/xslweb/functions/tests", name="implicit-objects", hasSideEffects=false)
      public void implicitObjects(XPathContext context, String text) {
        System.out.println(text + ", your Saxon edition is: " + context.getConfiguration().getEditionCode());
      }
      
    }
    ]]>  
    </xsl:text>
    <!--
    <xsl:text>
    <![CDATA[
    
    public class SecundaryClass {

      public static String test() {
        return "TEST";
      }
      
    }
    ]]>
    </xsl:text>
    -->
  </xsl:variable>
 
  <xsl:template name="tab-contents-1">
    
    <!-- Register the extension functions in the Java code: -->
    <xsl:variable name="result" select="function:register($code-units)" as="element(function:diagnostics)?"/>
    <xsl:if test="$result/function:diagnostic/@kind = 'ERROR'">
      <p>Error registering/compiling extension function class:</p>
      <pre class="prettyprint lang-xml linenums">
        <xsl:sequence select="serialize($result, $output-parameters)"/>
      </pre>
    </xsl:if>
    
    <!-- Call the functions: -->
    <xsl:try>
      <p>
        Example 1: Calculates the longest common subsequence of two simple Strings<br/><br/>
        <xsl:sequence select="function:call(QName($function-ns-uri, 'lcs'), 'ABACCD', 'ACDF')"/>
        <hr/>
      </p>
      <p>
        Example 2: Sorts the specified range of a float array into ascending order<br/><br/>
        <xsl:sequence select="function:call(QName($function-ns-uri, 'sort-floats'), (1.2, 1.3, 1.5), 1, 2)"/>
        <hr/>
      </p>
      <p>
        Example 3: Void return value, use of implicit object XPathContext<br/><br/>
        <xsl:sequence select="function:call(QName($function-ns-uri, 'implicit-objects'), 'Hello World')"/>
        <hr/>
      </p>
      <xsl:catch>
        <p>
          <xsl:value-of select="$err:description || ' (' || $err:code || ' line: ' || $err:line-number || ', column: ' || $err:column-number || ')'"/>  
        </p>
      </xsl:catch>
    </xsl:try>
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