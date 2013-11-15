<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://expath.org/ns/repo/packages"
                exclude-result-prefixes="p"
                version="1.0">

   <xsl:output indent="yes"/>

   <xsl:param name="name"    select="/.."/>
   <xsl:param name="dir"     select="/.."/>
   <xsl:param name="version" select="/.."/>

   <xsl:template match="node()">
      <xsl:copy>
         <xsl:apply-templates/>
      </xsl:copy>
   </xsl:template>

   <xsl:template match="*">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates/>
      </xsl:copy>
   </xsl:template>

   <xsl:template match="p:package">
      <xsl:if test="not(@dir = $dir)">
         <xsl:if test="not(@name = $name and @version = $version)">
            <xsl:copy>
               <xsl:copy-of select="@*"/>
               <xsl:apply-templates/>
            </xsl:copy>
         </xsl:if>
      </xsl:if>
   </xsl:template>

   <xsl:template match="p:packages">
      <xsl:copy>
         <xsl:apply-templates select="*"/>
         <package xmlns="http://expath.org/ns/repo/packages"
                  name="{ $name }" dir="{ $dir }" version="{ $version }"/>
      </xsl:copy>
   </xsl:template>

</xsl:stylesheet>
