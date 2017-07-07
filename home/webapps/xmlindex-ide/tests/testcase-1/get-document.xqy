xquery version "3.0";

declare namespace xix="http://www.armatiek.nl/xmlindex/functions";

let $uri as xs:string := "testcase-1.xml"

return 
  xix:document($uri)