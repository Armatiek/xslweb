var breakpointsGrid = {     
  name: 'breakpointsGrid', 
  columns: [                
      { field: 'active', text: 'Enabled', size: '15%' },
      { field: 'path', text: 'Path', size: '50%' },
      { field: 'line', text: 'Line', size: '15%' },
      { field: 'condition', text: 'Condition', size: '30%' }
  ],
  onDblClick: function(event) {
    var selectedRecord = this.get(event.recid);
    openOrSelectFile(selectedRecord.path, selectedRecord.line - 1, -1);
  }  
};
  
var breakpointsLayout = $().w2layout({
  name: 'breakpointsLayout',
  panels: [
    { type: 'main', size: '100%', resizable: true, style: pstyle, content: $().w2grid(breakpointsGrid),
      toolbar: {
        items:[
          { type: 'button', id: 'toggle-breakpoint-btn', text: 'Enable/disable', icon: 'fas fa-toggle-on' },
          { type: 'button', id: 'remove-breakpoint-btn', text: 'Remove', icon: 'fas fa-trash' },
          { type: 'button', id: 'remove-all-breakpoints-btn', text: 'Remove all', icon: 'fas fa-trash-alt' },
          { type: 'button', id: 'goto-breakpoint-btn', text: 'Go to file', icon: 'fas fa-file-code' },
          { type: 'button', id: 'edit-condition-btn', text: 'Edit condition', icon: 'fas fa-edit' }
        ],
        onClick: function (event) {
          var grid = w2ui['breakpointsGrid'];
          var selectedRecId = grid.getSelection();
          var selectedRecord = null;
          if (selectedRecId.length > 0) {
            selectedRecord = grid.get(selectedRecId)[0];
          }
          switch (event.target) {
            case "toggle-breakpoint-btn":
              if (selectedRecord != null) {
                toggleBreakpoint(selectedRecord.path, selectedRecord.line - 1);
              }
            break;
            case "remove-breakpoint-btn":
              if (selectedRecord != null) {
                removeBreakpoint(selectedRecord.path, selectedRecord.line - 1);
              }
            break;
            case "remove-all-breakpoints-btn":
              removeAllBreakpoints();
            break;
            case "goto-breakpoint-btn":    
              if (selectedRecord != null) {
                openOrSelectFile(selectedRecord.path, selectedRecord.line - 1, -1);
              }
            break;
            case "edit-condition-btn":    
              if (selectedRecord != null) {
                w2prompt({
                  label       : 'Edit condition (XPath)',
                  value       : selectedRecord.condition,
                  attrs       : 'style="width: 300px"',
                  title       : w2utils.lang('Edit breakpoint condition'),
                  ok_text     : w2utils.lang('Ok'),
                  cancel_text : w2utils.lang('Cancel'),
                  width       : 500,
                  height      : 200
                })
                .change(function (event) {
                    console.log('change', event);
                })
                .ok(function (event) {
                  setBreakpointCondition(selectedRecord.path, selectedRecord.line - 1, event);
                });
              }
            break;
          }
        }
      }
    }]
});