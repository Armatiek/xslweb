<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" 
  xmlns:array="http://www.w3.org/2005/xpath-functions/array"
  xmlns:map="http://www.w3.org/2005/xpath-functions/map"
  xmlns:dbg="http://www.armatiek.com/xslweb/debug" 
  xmlns:local="urn:local"
  expand-text="yes"
  exclude-result-prefixes="local array map xs"
  version="3.1">
  
  <xsl:param name="sequence" as="item()*"/>
  <xsl:param name="display-mode" as="xs:string?"/>
  <xsl:param name="max-sequence-count" select="256" as="xs:integer"/>
  <xsl:param name="max-string-length" select="10000000" as="xs:integer"/>
 
  <xsl:output method="text"/>
  
  <xsl:mode name="expanded" on-no-match="shallow-copy"/>
  
  <xsl:variable name="output-parameters" as="element()">
    <output:serialization-parameters xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization">
      <output:indent value="yes"/>
      <output:omit-xml-declaration value="yes"/>
    </output:serialization-parameters>
  </xsl:variable>
  
  <xsl:template name="serialize-sequence">
    <xsl:variable name="result" as="element()">
      <xsl:call-template name="serialize">
        <xsl:with-param name="sequence" select="$sequence" as="item()*"/>
      </xsl:call-template>  
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$display-mode = 'compact'">
        <xsl:apply-templates select="$result" mode="compact"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="result-xml" as="element()">
          <xsl:apply-templates select="$result" mode="expanded"/>
        </xsl:variable>
        <xsl:variable name="result-text" select="serialize($result-xml, $output-parameters)" as="xs:string"/>
        <xsl:choose>
          <xsl:when test="string-length($result-text) gt $max-string-length">
            <xsl:sequence select="substring($result-text, 1, $max-string-length) || ' ...'"/>
          </xsl:when>
          <xsl:otherwise>
             <xsl:sequence select="$result-text"/>     
          </xsl:otherwise>
        </xsl:choose>  
      </xsl:otherwise>
    </xsl:choose>   
  </xsl:template>
  
  <xsl:template name="serialize">
    <xsl:param name="sequence" as="item()*"/>
    <xsl:variable name="count" select="count($sequence)" as="xs:integer"/>
    <xsl:choose>
      <xsl:when test="$count = 0">
        <dbg:empty-sequence/>
      </xsl:when>
      <xsl:otherwise>
        <dbg:sequence count="{$count}">
          <xsl:for-each select="$sequence">
            <xsl:choose>
              <xsl:when test=". instance of node()">
                <xsl:element name="{local:node-kind(.)}" namespace="http://www.armatiek.com/xslweb/debug">
                  <xsl:sequence select="."/>
                </xsl:element>
              </xsl:when>
              <xsl:when test=". instance of xs:anyAtomicType">
                <xsl:element name="{local:atomic-type(.)}" namespace="http://www.armatiek.com/xslweb/debug">
                  <xsl:sequence select="."/>
                </xsl:element>
              </xsl:when>
              <xsl:when test=". instance of map(*)">
                <dbg:map count="{map:size(.)}">
                  <xsl:variable name="map" select="." as="map(*)"/>
                  <xsl:for-each select="map:keys($map)">
                    <dbg:map-entry>
                      <dbg:map-key>
                        <xsl:call-template name="serialize">
                          <xsl:with-param name="sequence" select="."/>
                        </xsl:call-template>
                      </dbg:map-key>
                      <dbg:map-value>
                        <xsl:call-template name="serialize">
                          <xsl:with-param name="sequence" select="map:get($map, .)"/>
                        </xsl:call-template>
                      </dbg:map-value>
                    </dbg:map-entry>
                  </xsl:for-each>  
                </dbg:map>
              </xsl:when>
              <xsl:when test=". instance of function(*)">
                <dbg:function name="{function-name(.)}" arity="{function-arity(.)}"/>
              </xsl:when>
              <xsl:when test=". instance of array(*)">
                <dbg:array count="{array:size(.)}">
                  <xsl:variable name="array" select="." as="array(*)"/>
                  <xsl:for-each select="1 to array:size($array)">
                    <dbg:array-member>
                      <xsl:call-template name="serialize">
                        <xsl:with-param name="sequence" select="array:get($array, .)"/>
                      </xsl:call-template>
                    </dbg:array-member>
                  </xsl:for-each>  
                </dbg:array>  
              </xsl:when>
            </xsl:choose>  
          </xsl:for-each>
        </dbg:sequence>
      </xsl:otherwise>
    </xsl:choose>  
  </xsl:template>
  
  <xsl:function name="local:atomic-type" as="xs:string*">
    <xsl:param name="values" as="xs:anyAtomicType*"/>
    <xsl:sequence select="
      for $val in $values
      return
      (if ($val instance of xs:untypedAtomic) then 'dbg:untypedAtomic'
      else if ($val instance of xs:anyURI) then 'dbg:anyURI'
      else if ($val instance of xs:string) then 'dbg:string'
      else if ($val instance of xs:QName) then 'dbg:QName'
      else if ($val instance of xs:boolean) then 'dbg:boolean'
      else if ($val instance of xs:base64Binary) then 'dbg:base64Binary'
      else if ($val instance of xs:hexBinary) then 'dbg:hexBinary'
      else if ($val instance of xs:integer) then 'dbg:integer'
      else if ($val instance of xs:decimal) then 'dbg:decimal'
      else if ($val instance of xs:float) then 'dbg:float'
      else if ($val instance of xs:double) then 'dbg:double'
      else if ($val instance of xs:date) then 'dbg:date'
      else if ($val instance of xs:time) then 'dbg:time'
      else if ($val instance of xs:dateTime) then 'dbg:dateTime'
      else if ($val instance of xs:dayTimeDuration) then 'dbg:dayTimeDuration'
      else if ($val instance of xs:yearMonthDuration) then 'dbg:yearMonthDuration'
      else if ($val instance of xs:duration) then 'dbg:duration'
      else if ($val instance of xs:gMonth) then 'dbg:gMonth'
      else if ($val instance of xs:gYear) then 'dbg:gYear'
      else if ($val instance of xs:gYearMonth) then 'dbg:gYearMonth'
      else if ($val instance of xs:gDay) then 'dbg:gDay'
      else if ($val instance of xs:gMonthDay) then 'dbg:gMonthDay'
      else 'dbg:unknown-atomic-type')"/>
  </xsl:function>
  
  <xsl:function name="local:node-kind" as="xs:string*">
    <xsl:param name="nodes" as="node()*"/>
    <xsl:sequence select="
      for $node in $nodes
      return
      if ($node instance of element()) then 'dbg:element'
      else if ($node instance of attribute()) then 'dbg:attribute'
      else if ($node instance of text()) then 'dbg:text'
      else if ($node instance of document-node()) then 'dbg:document-node'
      else if ($node instance of comment()) then 'dbg:comment'
      else if ($node instance of processing-instruction()) then 'dbg:processing-instruction'
      else 'dbg:unknown-node-type'"/>
  </xsl:function>
  
  <!-- Mode "compact": -->
  <xsl:template match="dbg:*[position() = $max-sequence-count]" mode="compact" priority="2.0">
    <xsl:next-match/>
    <xsl:text> ... </xsl:text>
  </xsl:template>
  
  <xsl:template match="dbg:*[position() gt $max-sequence-count]" mode="compact" priority="2.0"/>
    
  <xsl:template match="dbg:sequence[not(*)]" mode="compact">
    <xsl:text>()</xsl:text>
  </xsl:template>
  
  <xsl:template match="dbg:sequence[count(*) = 1]" mode="compact">
    <xsl:apply-templates select="*" mode="#current"/>
  </xsl:template>
  
  <xsl:template match="dbg:sequence[count(*) gt 1]" mode="compact">
    <xsl:text>(</xsl:text>
    <xsl:apply-templates select="*" mode="#current"/>
    <xsl:text>)</xsl:text>
  </xsl:template>
  
  <xsl:template match="dbg:array[count(*) gt 1]" mode="compact">
    <xsl:text>[</xsl:text>
    <xsl:apply-templates select="*" mode="#current"/>
    <xsl:text>]</xsl:text>
  </xsl:template>
  
  <xsl:template match="dbg:map" mode="compact">
    <xsl:value-of select="'&#123;'"/>
    <xsl:apply-templates select="*" mode="#current"/>
    <xsl:value-of select="'&#125;'"/>
  </xsl:template>
  
  <xsl:template match="dbg:map-key" mode="compact">
    <xsl:next-match/>
    <xsl:text> : </xsl:text>
  </xsl:template>
  
  <xsl:template match="(dbg:sequence|dbg:array|dbg:map)/dbg:*" mode="compact">
    <xsl:next-match/>
    <xsl:if test="not(position() = last())">, </xsl:if>
  </xsl:template>
  
  <xsl:template match="dbg:element|dbg:attribute|dbg:text|dbg:document-node|dbg:comment|dbg:processing-instruction|dbg:function" mode="compact">
    <xsl:value-of select="'≪' || local-name() || '≫'"/>  
  </xsl:template>
  
  <xsl:template match="dbg:string|dbg:anyURI|xs:QName" mode="compact">
    <xsl:value-of select="'&quot;' || . || '&quot;'"/>
  </xsl:template>
  
  <xsl:template match="dbg:untypedAtomic|dbg:boolean|dbg:base64Binary|dbg:hexBinary|dbg:integer|dbg:decimal|dbg:float|dbg:double|
    dbg:date|dbg:time|dbg:dateTime|dbg:dayTimeDuration|dbg:yearMonthDuration|dbg:duration|dbg:gMonth|dbg:gYear|
    dbg:gYearMonth|dbg:gDay|dbg:gMonthDay" mode="compact">
    <xsl:value-of select="."/>
  </xsl:template>
    
  <!-- Mode "expanded": -->
  <xsl:template match="dbg:*[position() = $max-sequence-count]" mode="expanded" priority="2.0">
    <xsl:next-match/>
    <dbg:truncated/>
  </xsl:template>
  
  <xsl:template match="dbg:*[position() gt $max-sequence-count]" mode="expanded" priority="2.0"/>
  
  <xsl:template match="dbg:sequence[count(*) = 1]" mode="expanded">
    <xsl:apply-templates select="*" mode="#current"/>
  </xsl:template>
 
</xsl:stylesheet>