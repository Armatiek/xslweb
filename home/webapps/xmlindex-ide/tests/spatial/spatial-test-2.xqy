xquery version "3.0";

declare namespace geo ="http://www.armatiek.nl/xmlindex/functions/spatial";
declare namespace gml = "http://www.opengis.net/gml/3.2";
declare namespace kad = "http://www.kadaster.nl/kad/pdok";

let $gml-noord-holland as document-node() := doc('Noord-Holland.gml')

(: Supported spatial operators: :)
(: Contains, Intersects, IsDisjointTo, IsEqualTo, IsWithin, Overlaps :)

return
  <Gemeenten-In-Noord-Holland> {
    for $gemeente in root()//*[geo:spatial-query(., 'IsWithin', $gml-noord-holland)]/ancestor::gml:featureMember/kad:Gemeenten/kad:Gemeentenaam/text() 
    order by $gemeente
    return <Gemeente> { $gemeente } </Gemeente>
  } </Gemeenten-In-Noord-Holland>