xquery version "3.1";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace va="http://www.armatiek.nl/xmlindex/virtualattribute";
declare namespace fx="http://www.armatiek.nl/xmlindex/functions";
declare namespace local="urn:local";

declare variable $ns-xs as xs:string := 'http://www.w3.org/2001/XMLSchema';
declare variable $ns-va as xs:string := 'http://www.armatiek.nl/xmlindex/virtualattribute';
declare variable $element-type as xs:integer := 1;
declare variable $attribute-type as xs:integer := 2;

declare function local:configure-typed-value-defs() as xs:boolean* {
  (
   local:configure-typed-value-def($attribute-type, QName('', 'inwerking'), QName($ns-xs, 'xs:date'))
  )
};

declare function local:configure-virtual-attribute-defs() as xs:boolean* {
  let $analyzer as element(analyzer) :=
    <analyzer>
      <tokenizer class="standard"/>
      <filter class="standard"/>
      <filter class="lowercase"/>
      <filter class="stop" ignoreCase="true"/>
    </analyzer>    
  return
    (
      local:configure-virtual-attribute-def(QName('', 'toestand'), 'expression-id', 
        QName($ns-va, 'toestand-expression-id'), QName($ns-xs, 'xs:string'), (), ()),
      local:configure-virtual-attribute-def(QName('', 'expression'), 'bwb-id', 
        QName($ns-va, 'expression-bwb-id'), QName($ns-xs, 'xs:string'), (), ()),
      local:configure-virtual-attribute-def(QName('', 'expression'), 'inwerkingtredingsdatum', 
        QName($ns-va, 'expression-inwerkingtredingsdatum'), QName($ns-xs, 'xs:date'), (), ()),
      local:configure-virtual-attribute-def(QName('', 'expression'), 'einddatum', 
        QName($ns-va, 'expression-einddatum'), QName($ns-xs, 'xs:date'), (), ()),
      local:configure-virtual-attribute-def(QName('', 'expression'), 'zichtdatum-start', 
        QName($ns-va, 'expression-zichtdatum-start'), QName($ns-xs, 'xs:date'), (), ()),
      local:configure-virtual-attribute-def(QName('', 'expression'), 'zichtdatum-eind', 
        QName($ns-va, 'expression-zichtdatum-eind'), QName($ns-xs, 'xs:date'), (), ()),
      local:configure-virtual-attribute-def(QName('', 'toestand'), 'citeertitel', 
        QName($ns-va, 'citeertitel'), QName($ns-xs, 'xs:string'), $analyzer, $analyzer)
    )
};

declare function local:configure-pluggable-indexes() as xs:boolean* {
  let $params as map(xs:string, xs:string) := 
    map { 
      'fieldName' : 'shape', 
      'prefixTreeMaxLevels' : '11' }
  return
    (
      local:configure-pluggable-index('nl.armatiek.xmlindex.extensions.SpatialIndex', $params)
    )
};

declare function local:configure-typed-value-def(
    $node-type as xs:integer, 
    $node-name as xs:QName, 
    $item-type as xs:QName) as xs:boolean {
  if (not(fx:typed-value-def-exists($node-type, $node-name))) then
    fx:add-typed-value-def($node-type, $node-name, $item-type)
  else
    false()
};

declare function local:configure-virtual-attribute-def(
    $element-name as xs:QName, 
    $virtual-attribute-name as xs:string, 
    $function-name as xs:QName, 
    $item-type as xs:QName, 
    $index-analyzer as element(analyzer)?, 
    $query-analyzer as element(analyzer)?) as xs:boolean {
  if (not(fx:virtual-attribute-def-exists($virtual-attribute-name))) then
    fx:add-virtual-attribute-def($element-name, $virtual-attribute-name, $function-name, 
      $item-type, $index-analyzer, $query-analyzer)
  else
    false()
};

declare function local:configure-pluggable-index(
    $class-name as xs:string, 
    $params as map(xs:string, xs:string)?) as xs:boolean {
  if (not(fx:pluggable-index-exists($class-name))) then
    fx:add-pluggable-index($class-name, $params)
  else
    false()
};

(
  local:configure-typed-value-defs(),
  local:configure-virtual-attribute-defs(),
  local:configure-pluggable-indexes()
)