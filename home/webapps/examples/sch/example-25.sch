<?xml version="1.0" encoding="UTF-8"?>
<schema 
  xmlns="http://purl.oclc.org/dsdl/schematron"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  
  xmlns:xs="http://www.w3.org/2001/XMLSchema" 
  queryBinding="xslt2">
  
  <title>Example 25: Schematron validation</title>
  
  <pattern>
    <rule context="a|b|c|d">
      <assert test="@*[local-name() = local-name(..)] = local-name()">The value of the attribute @<value-of select="local-name()"/> of element <value-of select="local-name()"/> is not equal to <value-of select="local-name()"/>.</assert>          
    </rule>
  </pattern>
  
</schema>