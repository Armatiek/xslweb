var variablesResult = 
  CodeMirror.fromTextArea(document.getElementById('preview-result'), {
    mode: "xml",
    readOnly: true
});

var variablesGrid = {     
  name: 'variablesGrid', 
  columns: [                
      { field: 'label', text: 'Name', size: '30%' },
      { field: 'value', text: 'Value', size: '70%' }
  ],
  onClick: function(event) {
    var expandedValue = this.get(event.recid).expandedValue;
    if (expandedValue !== undefined) {
      showSerializedSequence(expandedValue, variablesResult);
    } else {
      variablesResult.setValue(this.get(event.recid).value)
    }
  }  
};
  
var variablesLayout = $().w2layout({
  name: 'variablesLayout',
  panels: [
    { type: 'left', size: '50%', resizable: true, style: pstyle, content: $().w2grid(variablesGrid) },
    { type: 'main', style: pstyle, content: $('#preview-result-container')[0] }
  ],
  onResize: function(event) {
    event.onComplete = function () {
      if (variablesResult !== undefined) {
        variablesResult.setSize("100%", $('#preview-result-container').height());
        variablesResult.refresh();
      }
    }
  }
});