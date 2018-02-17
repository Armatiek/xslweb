<xsl:stylesheet   
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"    
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  exclude-result-prefixes="#all"
  version="3.0">
 
  <xsl:output method="xml" indent="yes"/>
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:body>
        <xsl:sequence select="document('books.xml')"/>
      </resp:body>
    </resp:response>          
  </xsl:template>
  
</xsl:stylesheet>