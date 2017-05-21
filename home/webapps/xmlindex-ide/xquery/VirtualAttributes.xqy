xquery version "3.0";

declare namespace va="http://www.armatiek.nl/xmlindex/virtualattribute";

import module namespace functx = "http://www.functx.com" at "../../../common/xquery/lib/functx/functx-1.0.xq";

declare function va:function-test($elem as element()) as xs:string {
  $elem/@label-id || $elem/@versie-id
};

declare function va:function-ft-test($elem as element()) as xs:string {
  string-join($elem//text(), ' ')
};

declare function va:toestand-expression-id($elem as element()) as xs:string {
  concat($elem/@bwb-id, '_', $elem/@inwerkingtreding, '_', functx:substring-after-last($elem/@bwb-ng-vast-deel, '/'))
};

declare function va:expression-bwb-id($elem as element()) as xs:string {
  xs:string($elem/root()/work/@label)
};

declare function va:expression-inwerkingtredingsdatum($elem as element()) as xs:date {
  xs:date($elem/metadata/datum_inwerkingtreding)
};

declare function va:expression-einddatum($elem as element()) as xs:date {
  xs:date($elem/metadata/einddatum)
};

declare function va:expression-zichtdatum-start($elem as element()) as xs:date {
  xs:date($elem/metadata/zichtdatum_start)
};

declare function va:expression-zichtdatum-eind($elem as element()) as xs:date {
  xs:date($elem/metadata/zichtdatum_eind)
};

declare function va:citeertitel($elem as element()) as xs:string {
  string-join($elem/wetgeving/citeertitel//text(), ' ')
};

()