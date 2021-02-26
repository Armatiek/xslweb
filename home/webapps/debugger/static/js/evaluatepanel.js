var evaluateEditor = 
  CodeMirror.fromTextArea(document.getElementById('evaluate-editor'), {
    mode: "xquery"
  });
    
var evaluateResult = 
  CodeMirror.fromTextArea(document.getElementById('evaluate-result'), {
    mode: "xml",
    readOnly: true,
    lineNumbers: true,
    lineWrapping: true,
    extraKeys: {"Ctrl-Q": function(cm){ cm.foldCode(cm.getCursor()); }},
    foldGutter: true,
    gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
  });

var evaluateLayout = $().w2layout({
  name: 'evaluateLayout',
  panels: [
    { type: 'left', size: '50%', resizable: true, style: pstyle, content: $('#evaluate-editor-container')[0], 
        toolbar: {
          items:[{ type: 'button', id: 'evaluate-btn', text: 'Evaluate', icon: 'fas fa-play' }],
          onClick: function (event) {
            switch (event.target) {
              case "evaluate-btn":
                $.get("dbg-servlet/evaluate-xpath?expression=" + encodeURIComponent(evaluateEditor.getValue()), function(data) {
                  if (data.code == 200) {
                    evaluateResult.setValue(data.message);
                  } else {
                    showMessage('Error evaluating XPath', data.message);
                  }
                });
                break;
              }
            }
          }
        },
    { type: 'main', style: pstyle, content: $('#evaluate-result-container')[0]/*,
        toolbar: {
          items:[{ type: 'button', id: 'copy-evaluate-result-btn', text: 'Copy', icon: 'fas fa-copy' }],
          onClick: function (event) {
            switch (event.target) {
              case "copy-evaluate-result-btn":
              
                break;
              }
            }
          } */
        }
  ],
  onResize: function(event) {
    event.onComplete = function () {
      if (evaluateEditor !== undefined) {
        evaluateEditor.setSize("100%", $('#evaluate-editor-container').height());
        evaluateEditor.refresh();
      }
      if (evaluateResult !== undefined) {
        evaluateResult.setSize("100%", $('#evaluate-result-container').height());
        evaluateResult.refresh();
      }
    }
  }
});