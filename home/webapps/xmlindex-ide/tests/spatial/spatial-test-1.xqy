xquery version "3.0";

declare namespace geo ="http://www.armatiek.nl/xmlindex/functions/spatial";
declare namespace gml = "http://www.opengis.net/gml/3.2";
declare namespace kad = "http://www.kadaster.nl/kad/pdok";

let $amsterdam-gml-rd as element(gml:Point) := 
  <gml:Point gml:id="a1" srsName="EPSG:28992" srsDimension="2">
    <gml:pos>121344 487316</gml:pos>
  </gml:Point>
  
(: Supported spatial operators: :)
(: Contains, Intersects, IsDisjointTo, IsEqualTo, IsWithin, Overlaps :)
  
return
  <gemeente> {
    root()//*[geo:spatial-query(., 'Contains', $amsterdam-gml-rd)]/ancestor::gml:featureMember/kad:Gemeenten/kad:Gemeentenaam/text() 
  } </gemeente>