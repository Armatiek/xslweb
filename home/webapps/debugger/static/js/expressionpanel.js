var expressionGrid = {     
  name: 'expressionGrid', 
  columns: [                
      { field: 'label', text: 'Name', size: '30%' },
      { field: 'value', text: 'Value', size: '70%' }
  ] 
};

var expressionLayout = $().w2layout({
  name: 'expressionLayout',
  panels: [
    { type: 'main', size: '100%', resizable: true, style: pstyle, content: $().w2grid(expressionGrid) }]
});