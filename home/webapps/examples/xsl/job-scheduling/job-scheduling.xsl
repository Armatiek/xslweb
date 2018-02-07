<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Example 16: Job scheduling</xsl:template>
  
  <xsl:template name="tab-contents-1">
    <p>To see the job scheduling in action, uncomment the job element from the <i>webapp.xml</i> of this 
      examples webapp. The pipeline request <i>execute-writetime-job.html</i> will then be called every
    60 seconds.</p>
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">job-scheduling</xsl:variable>
  
</xsl:stylesheet>