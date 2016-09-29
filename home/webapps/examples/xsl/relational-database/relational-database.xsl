<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:sql="http://www.armatiek.com/xslweb/functions/sql"
  xmlns:local="urn:local"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Example 22: Relational database access</xsl:template>
  
  <xsl:template name="sql:process-row">
    <xsl:param name="result-set"/>    
    <xsl:variable name="row" select="sql:get-next-row($result-set)"/>    
    <xsl:if test="exists($row)">
      <tr>        
        <td><xsl:value-of select="$row[1]"/></td>
        <td><xsl:value-of select="$row[2]"/></td>
        <td><xsl:value-of select="$row[3]"/></td>
        <td><xsl:value-of select="$row[4]"/></td>
        <td><xsl:value-of select="$row[5]"/></td>
        <td><xsl:value-of select="$row[6]"/></td>
        <td><xsl:value-of select="$row[7]"/></td>
        <td><xsl:value-of select="$row[8]"/></td>
        <td><xsl:value-of select="$row[9]"/></td>
        <td><xsl:value-of select="format-dateTime($row[10], '[MNn] [D], [Y]', 'en', (), ())"/></td>
        <td><xsl:value-of select="if ($row[11]) then 'X' else '-'"/></td>
      </tr>
      <xsl:call-template name="sql:process-row">
        <xsl:with-param name="result-set" select="$result-set"/>
      </xsl:call-template>
    </xsl:if>    
  </xsl:template>
      
  <xsl:template name="tab-contents-1">
    
    <p>In this example TODO</p>
    
    <xsl:variable name="connection" select="sql:get-connection('datasource-worldcup')"/>
    
    <xsl:variable name="result-set-1" select="sql:execute-query($connection, 'select * from players')"/>
    
    <h3>"Lazy" reading of data from resultset using <i>sql:get-next-row($result-set)</i>:</h3>
    
    <table border="1">
      <tr>        
        <th>ID</th>
        <th>Competition</th>
        <th>Year</th>
        <th>Country</th>
        <th>Number</th>
        <th>Position</th>
        <th>Fullname</th>
        <th>Club name</th>
        <th>Club country</th>
        <th>Date of birth</th>
        <th>Captain</th>                
      </tr>      
      <xsl:call-template name="sql:process-row">
        <xsl:with-param name="result-set" select="$result-set-1"/>
      </xsl:call-template>  
    </table>
    
    <xsl:sequence select="sql:close($result-set-1)"/>
    
    <br/>
    
    <h3>Read data from resultset using <i>sql:resultset-to-node($result-set)</i>:</h3>
    
    <xsl:variable name="result-set-2" select="sql:execute-query($connection, 'select * from players')"/>
    
    <table border="1">
      <tr>        
        <th>ID</th>
        <th>Competition</th>
        <th>Year</th>
        <th>Country</th>
        <th>Number</th>
        <th>Position</th>
        <th>Fullname</th>
        <th>Club name</th>
        <th>Club country</th>
        <th>Date of birth</th>
        <th>Captain</th>                
      </tr>      
      <xsl:apply-templates select="sql:resultset-to-node($result-set-2)"/>  
    </table>
                                     
    <xsl:sequence select="sql:close($result-set-2)"/>
    
    <xsl:sequence select="sql:close($connection)"/>
            
  </xsl:template>
  
  <xsl:template match="row">
    <tr>
      <xsl:apply-templates/>
    </tr>
  </xsl:template>
  
  <xsl:template match="col">
    <td>
      <xsl:apply-templates/>
    </td>
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">relational-database</xsl:variable>
  
</xsl:stylesheet>