xquery version "3.1";

declare namespace va="http://www.armatiek.nl/xmlindex/virtualattribute";
declare namespace fx="http://www.armatiek.nl/xmlindex/functions";
declare namespace local="urn:local";

declare function local:get-QName($eq-name as xs:string) as xs:QName {
  if (not(contains($eq-name, '{'))) then
    QName('', $eq-name)
  else
    QName(
      substring-before(substring($eq-name, 3), '}'), 
      substring-after($eq-name, '}'))
};

declare function local:remove-typed-value-defs($configuration as element(index-configuration)) as xs:boolean* {
  for $a in $configuration/typed-value-config/typed-value-def return
    fx:remove-typed-value-def($a/node-type cast as xs:integer, local:get-QName($a/node-name))  
};

declare function local:remove-virtual-attribute-defs($configuration as element(index-configuration)) as xs:boolean* {
  for $a in $configuration/virtual-attribute-config/virtual-attribute-def/virtual-attribute-name return
    fx:remove-virtual-attribute-def($a)
};

declare function local:remove-pluggable-indexes($configuration as element(index-configuration)) as xs:boolean* {
  for $a in $configuration/pluggable-index-config/class-name return
    fx:remove-pluggable-index($a)
};

let $configuration as element(index-configuration) := fx:get-configuration()

return
  (
    $configuration,
    local:remove-typed-value-defs($configuration),
    local:remove-virtual-attribute-defs($configuration),
    local:remove-pluggable-indexes($configuration)
  )