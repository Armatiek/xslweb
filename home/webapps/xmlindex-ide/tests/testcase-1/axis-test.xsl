<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:va="http://www.armatiek.nl/xmlindex/virtualattribute"
  xmlns:xi="http://www.armatiek.nl/xmlindex/functions"
  xmlns:xw="http://www.armatiek.com/xslweb/functions"
  xmlns:conf="http://www.armatiek.com/xslweb/configuration"
  xmlns:pf1="local:urn1"
  xmlns:pf2="local:urn2"
  xmlns:pf3="local:urn3"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:param name="conf:webapp-dir" as="xs:string"/>
  
  <xsl:include href="../../../../common/xsl/lib/xslweb/xslweb.xsl"/>
  
  <xsl:template match="/">
    <test>
      <xsl:variable name="d" select="xi:document('testcase-1.xml')//d[@id='1']" as="element()"/>
      
      <!-- ancestor -->
      <xsl:variable name="names" as="xs:string*">
        <xsl:for-each select="$d/ancestor::*">
          <xsl:value-of select="local-name(.)"/>
        </xsl:for-each>  
      </xsl:variable>
      <ancestor>
        <xsl:value-of select="string-join($names, ', ')"/>
      </ancestor>
      
      <!-- ancestor-or-self -->
      <xsl:variable name="names" as="xs:string*">
        <xsl:for-each select="$d/ancestor-or-self::*">
          <xsl:value-of select="local-name(.)"/>
        </xsl:for-each>  
      </xsl:variable>
      <ancestor-or-self>
        <xsl:value-of select="string-join($names, ', ')"/>
      </ancestor-or-self>

      <!-- attribute -->
      <xsl:variable name="names" as="xs:string*">
        <xsl:for-each select="$d/attribute::*">
          <xsl:value-of select="local-name(.)"/>
        </xsl:for-each>  
      </xsl:variable>
      <attribute>
        <xsl:value-of select="string-join($names, ', ')"/>
      </attribute>

      <!-- child -->
      <xsl:variable name="names" as="xs:string*">
        <xsl:for-each select="$d/child::*">
          <xsl:value-of select="local-name(.)"/>
        </xsl:for-each>  
      </xsl:variable>
      <child>
        <xsl:value-of select="string-join($names, ', ')"/>
      </child>

      <!-- descendant -->
      <xsl:variable name="names" as="xs:string*">
        <xsl:for-each select="$d/descendant::*">
          <xsl:value-of select="local-name(.)"/>
        </xsl:for-each>  
      </xsl:variable>
      <descendant>
        <xsl:value-of select="string-join($names, ', ')"/>
      </descendant>

      <!-- descendant-or-self -->
      <xsl:variable name="names" as="xs:string*">
        <xsl:for-each select="$d/descendant-or-self::*">
          <xsl:value-of select="local-name(.)"/>
        </xsl:for-each>  
      </xsl:variable>
      <descendant-or-self>
        <xsl:value-of select="string-join($names, ', ')"/>
      </descendant-or-self>

      <!-- following -->
      <xsl:variable name="names" as="xs:string*">
        <xsl:for-each select="$d/following::*">
          <xsl:value-of select="local-name(.)"/>
        </xsl:for-each>  
      </xsl:variable>
      <following>
        <xsl:value-of select="string-join($names, ', ')"/>
      </following>

      <!-- following-sibling -->
      <xsl:variable name="names" as="xs:string*">
        <xsl:for-each select="$d/following-sibling::*">
          <xsl:value-of select="local-name(.)"/>
        </xsl:for-each>  
      </xsl:variable>
      <following-sibling>
        <xsl:value-of select="string-join($names, ', ')"/>
      </following-sibling>

      <!-- parent -->
      <xsl:variable name="names" as="xs:string*">
        <xsl:for-each select="$d/parent::*">
          <xsl:value-of select="local-name(.)"/>
        </xsl:for-each>  
      </xsl:variable>
      <parent>
        <xsl:value-of select="string-join($names, ', ')"/>
      </parent>

      <!-- preceding -->
      <!--
      <xsl:variable name="names" as="xs:string*">
        <xsl:for-each select="$d/preceding::*">
          <xsl:value-of select="local-name(.)"/>
        </xsl:for-each>  
      </xsl:variable>
      <preceding>
        <xsl:value-of select="string-join($names, ', ')"/>
      </preceding>
      -->

      <!-- preceding-sibling -->
      <xsl:variable name="names" as="xs:string*">
        <xsl:for-each select="$d/preceding-sibling::*">
          <xsl:value-of select="local-name(.)"/>
        </xsl:for-each>  
      </xsl:variable>
      <preceding-sibling>
        <xsl:value-of select="string-join($names, ', ')"/>
      </preceding-sibling>

      <!-- self -->
      <xsl:variable name="names" as="xs:string*">
        <xsl:for-each select="$d/self::*">
          <xsl:value-of select="local-name(.)"/>
        </xsl:for-each>  
      </xsl:variable>
      <self>
        <xsl:value-of select="string-join($names, ', ')"/>
      </self>
    </test>
    
  </xsl:template>
  
</xsl:stylesheet>