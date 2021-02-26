var contextResult = 
  CodeMirror.fromTextArea(document.getElementById('context-result'), {
    mode: "xml",
    readOnly: true,
    lineNumbers: true,
    lineWrapping: true,
    extraKeys: {"Ctrl-Q": function(cm){ cm.foldCode(cm.getCursor()); }},
    foldGutter: true,
    gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
  });

var contextGrid = {     
  name: 'contextGrid', 
  columns: [                
      { field: 'label', text: 'Name', size: '30%' },
      { field: 'value', text: 'Value', size: '70%' }
  ],
  onClick: function(event) {
    var expandedValue = this.get(event.recid).expandedValue;
    if (expandedValue !== undefined) {
      showSerializedSequence(expandedValue, contextResult);
    } else {
      contextResult.setValue(this.get(event.recid).value)
    }
  }  
};
  
var contextLayout = $().w2layout({
  name: 'contextLayout',
  panels: [
    { type: 'left', size: '50%', resizable: true, style: pstyle, content: $().w2grid(contextGrid) },
    { type: 'main', style: pstyle, content: $('#context-result-container')[0] }
  ],
  onResize: function(event) {
    event.onComplete = function () {
      if (contextResult !== undefined) {
        contextResult.setSize("100%", $('#context-result-container').height());
        contextResult.refresh();
      }
    }
  }
});