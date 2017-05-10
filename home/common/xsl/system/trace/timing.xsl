<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xs="http://www.w3.org/2001/XMLSchema" 
  exclude-result-prefixes="xs" 
  version="2.0">
  
  <xsl:template match="/*">
    
    <!--
    <h1>Analysis of Stylesheet Execution Time</h1>
    -->
    <p>Total time: <xsl:value-of select="format-number(@t-total, '#0.000')"/> milliseconds</p>
    <h2>Time spent in each template or function:</h2>
    <p>The table below is ordered by the total net time spent in the template or function. 
      Gross time means the time including called templates and functions; net time means 
      time excluding time spent in called templates and functions.</p>
    
    <table border="border" cellpadding="10">
      <thead>
        <tr>
          <th>file</th>
          <th>line</th>
          <th>instruction</th>
          <th>count</th>
          <th>average time (gross)</th>
          <th>total time (gross)</th>
          <th>average time (net)</th>
          <th>total time (net)</th>
        </tr>
      </thead>
      <tbody>
        <xsl:for-each select="fn">
          <xsl:sort select="@file"/>
          <xsl:sort select="@line"/>
          <xsl:sort select="@name"/>
          <xsl:sort select="@match"/>
          <xsl:sort select="number(@t-sum-net)" order="descending"/>
          <tr>
            <td>
              <xsl:value-of select="@file"/>
            </td>
            <td>
              <xsl:value-of select="@line"/>
            </td>
            <td>
              <xsl:value-of select="@construct, @name, @match"/>
            </td>
            <td align="right">
              <xsl:value-of select="@count"/>
            </td>
            <td align="right">
              <xsl:value-of select="format-number(@t-avg, '#0.000')"/>
            </td>
            <td align="right">
              <xsl:value-of select="format-number(@t-sum, '#0.000')"/>
            </td>
            <td align="right">
              <xsl:value-of select="format-number(@t-avg-net, '#0.000')"/>
            </td>
            <td align="right">
              <xsl:value-of select="format-number(@t-sum-net, '#0.000')"/>
            </td>
          </tr>
        </xsl:for-each>
      </tbody>
    </table>
    
  </xsl:template>
  
</xsl:stylesheet>