<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet   
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"    
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  xmlns:fo="http://www.w3.org/1999/XSL/Format"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:param name="config:webapp-dir" as="xs:string"/>
  
  <xsl:output method="xml"/>
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:body>
        <xsl:call-template name="body"/>
      </resp:body>
    </resp:response>          
  </xsl:template>
  
  <xsl:template name="body">   
    <fo:root>            
      <fo:layout-master-set>  
        <fo:simple-page-master master-name="simple"
          page-height="29.7cm"
          page-width="21cm"
          margin-top="1cm"
          margin-bottom="2cm"
          margin-left="2.5cm"
          margin-right="2.5cm">
          <fo:region-body margin-top="3cm"/>
          <fo:region-before extent="3cm"/>
          <fo:region-after extent="1.5cm"/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:declarations>
        <x:xmpmeta xmlns:x="adobe:ns:meta/">
          <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
            <rdf:Description rdf:about=""
              xmlns:dc="http://purl.org/dc/elements/1.1/">
              <!-- Dublin Core properties go here -->
              <dc:title>Document title</dc:title>
              <dc:creator>Document author</dc:creator>
              <dc:description>Document subject</dc:description>
            </rdf:Description>
            <rdf:Description rdf:about="" xmlns:xmp="http://ns.adobe.com/xap/1.0/">              
              <xmp:CreatorTool>Tool used to make the PDF</xmp:CreatorTool>
            </rdf:Description>
          </rdf:RDF>
        </x:xmpmeta>
      </fo:declarations>      
      <fo:page-sequence master-reference="simple">              
        <fo:flow flow-name="xsl-region-body">                    
          <fo:block font-size="18pt"
            font-family="sans-serif"
            line-height="24pt"
            space-after.optimum="15pt"
            background-color="blue"
            color="white"
            text-align="center"
            padding-top="3pt">
            Extensible Markup Language (XML) 1.0
          </fo:block>                              
          <fo:block font-size="12pt"
            font-family="sans-serif"
            line-height="15pt"
            space-after.optimum="3pt"
            text-align="justify">
            The Extensible Markup Language (XML) is a subset of SGML that is completely described in this document. Its goal is to
            enable generic SGML to be served, received, and processed on the Web in the way that is now possible with HTML. XML
            has been designed for ease of implementation and for interoperability with both SGML and HTML.
          </fo:block>
          <fo:block font-size="12pt"
            font-family="sans-serif"
            line-height="15pt"
            space-after.optimum="3pt"
            text-align="justify">
            The Extensible Markup Language (XML) is a subset of SGML that is completely described in this document. Its goal is to
            enable generic SGML to be served, received, and processed on the Web in the way that is now possible with HTML. XML
            has been designed for ease of implementation and for interoperability with both SGML and HTML.
          </fo:block>          
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
  
</xsl:stylesheet>