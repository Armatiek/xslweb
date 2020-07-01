<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:dynfunc="http://www.armatiek.com/xslweb/functions/dynfunc"
  xmlns:err="http://www.w3.org/2005/xqt-errors"
  exclude-result-prefixes="#all"
  version="3.0">
 
  <xsl:template name="call-function">
    <xsl:try>
      <xsl:variable name="test-elem" as="element(test-elem)">
        <test-elem test-attr="This is an attribute">
          <text>Hello World</text>
          <comment><!-- This is a comment --></comment>
          <pi><?pi This is a pi?></pi>
        </test-elem>
      </xsl:variable>
      <xsl:sequence select="#function-call"/>
      <xsl:catch>
        <span style="color:red">
          <xsl:value-of select="$err:description || ' (' || $err:code || ', line: ' || $err:line-number || ', column: ' || $err:column-number || ')'"/>  
        </span>
      </xsl:catch>
    </xsl:try>
  </xsl:template>
  
</xsl:stylesheet>