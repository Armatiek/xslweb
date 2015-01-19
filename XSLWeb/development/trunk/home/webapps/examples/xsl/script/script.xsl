<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:script="http://www.armatiek.com/xslweb/functions/script"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Example 20: Script extension function</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <xsl:variable name="script" as="xs:string">
      <![CDATA[
    
      function atomicValuesFunction(atomicString) { 
        return atomicString.length;
      }
      
      function arrayValuesFunction(arrayOfStrings) { 
        var arrayOfInteger = new Array();
        for (i = 0; i < arrayOfStrings.length; i++) {
          arrayOfInteger.push(arrayOfStrings[i].length());
        }
        return arrayOfInteger;
      }
      
      function format() {
        var importedClasses = new JavaImporter(java.text.DecimalFormat);
        with (importedClasses) {
          var decimalFormat = new DecimalFormat("###,###.###");
          return decimalFormat.format(123456.789);
        }
      }
      ]]>
    </xsl:variable>
    <p>
      <xsl:sequence select="script:invoke($script, 'atomicValuesFunction', 'France')"/>  
    </p>
    
    <p>
      <xsl:sequence select="script:invoke($script, 'arrayValuesFunction', ('France', 'Germany', 'Holland'))"/>  
    </p>
    
    <p>
      <xsl:sequence select="script:invoke($script, 'format')"/>  
    </p>
    
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">script</xsl:variable>
  
</xsl:stylesheet>