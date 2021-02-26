var stackResult = 
  CodeMirror.fromTextArea(document.getElementById('stack-result'), {
    mode: "xml",
    readOnly: true
  });
  
var stackGrid = {     
  name: 'stackGrid', 
  columns: [                
      { field: 'container', text: 'Container', size: '30%' },
      { field: 'path', text: 'Path', size: '30%' },
      { field: 'line', text: 'Line', size: '7%' },
      { field: 'mode', text: 'Mode', size: '8%' },
      { field: 'context', text: 'Context item', size: '25%' }
  ],
  onClick: function(event) {
    var record = this.get(event.recid);
    var contextExpanded = record.contextExpanded;
    if (contextExpanded !== undefined) {
      showSerializedSequence(contextExpanded, stackResult);
    } else {
      stackResult.setValue(this.get(event.recid).context)
    }
    openOrSelectFile(record.path, record.line, -1);
  }  
};
  
var stackLayout = $().w2layout({
  name: 'stackLayout',
  panels: [
    { type: 'left', size: '50%', resizable: true, style: pstyle, content: $().w2grid(stackGrid) },
    { type: 'main', style: pstyle, content: $('#stack-result-container')[0] }
  ],
  onResize: function(event) {
    event.onComplete = function () {
      if (stackResult !== undefined) {
        stackResult.setSize("100%", $('#stack-result-container').height());
        stackResult.refresh();
      }
    }
  }
});