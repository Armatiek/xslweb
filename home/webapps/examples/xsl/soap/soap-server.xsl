<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:tem="http://tempuri.org/"
  xmlns:soap-env="http://www.w3.org/2003/05/soap-envelope"
  exclude-result-prefixes="req resp tem"
  version="2.0">
  
  <xsl:output indent="yes" method="xml"/>
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:headers>
        <resp:header name="Content-Type">text/xml;charset=UTF-8"</resp:header>
      </resp:headers>
      <resp:body>
        <soap:Envelope 
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
          xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
          xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Body>
            <AddResponse xmlns="http://tempuri.org/">
              <AddResult>
                <xsl:variable name="add-elem" select="/req:request/req:body/soap-env:Envelope/soap-env:Body/tem:Add" as="element()"/>
                <xsl:value-of select="xs:integer($add-elem/tem:intA) + xs:integer($add-elem/tem:intB)"/>
              </AddResult>
            </AddResponse>
          </soap:Body>
        </soap:Envelope>
      </resp:body>
    </resp:response>
  </xsl:template>
  
</xsl:stylesheet>