$(function () {

  breakpoints = new Breakpoints();

  $.get("dbg-servlet/refresh-session", function(data) {
  
    console.log("Starting SSE ...");
    sse = $.SSE("dbg-sse-servlet", {
      onMessage: function(e){ 
        console.log("SSE message without event");
      },
      onOpen: function(e){ 
        console.log("SSE connection opened");
        setConnectionButtonState(3);
        stopReconnectSSE();
        refreshBreakpointsGrid();
      },
      onEnd: function(e){ 
        console.log("SSE connection closed");
        // setConnectionButtonState(1);
        startReconnectSSE();  
      },
      onError: function(e) { 
        console.log("Could not connect to SSE debug servlet");
        // showMessage("Error", "Could not connect to SSE debug servlet");
        // setConnectionButtonState(1);
        startReconnectSSE();
      },
      events: {
        break: function(e) {
          breakpointInfo = JSON.parse(e.data);
          var editor = openOrSelectFile( 
            breakpointInfo.path, 
            (breakpointInfo.line > -1) ? breakpointInfo.line : 0, 
            (breakpointInfo.column > -1) ? breakpointInfo.column : 0);
          
          clearAllGutters("breakarrow");
          editor.setGutterMarker(breakpointInfo.line, "breakarrow", makeArrowMarker());
       
          w2ui['variablesGrid'].records = breakpointInfo.variablesRecords;
          w2ui['contextGrid'].records = breakpointInfo.contextRecords;
          w2ui['stackGrid'].records = breakpointInfo.stackRecords;
          w2ui['expressionGrid'].records = breakpointInfo.expressionRecords;
          var allGrids = [w2ui['variablesGrid'], w2ui['contextGrid'],  w2ui['stackGrid'], w2ui['expressionGrid']];
          allGrids.forEach(function(grid, index) {
            grid.refresh();
            var selection = grid.getSelection();
            if (selection.length > 0) {
              grid.unselect(selection[0]);
            }
          });
          variablesResult.setValue('');
          contextResult.setValue('');
          stackResult.setValue('');
          evaluateResult.setValue('');
        },
        stateChanged: function(e) {
          console.log("State changed: " + e.data);
          clearTabContents();
        },
        setBreakpoint: function(e) {
          enableBreakpoint(e, true);
        },
        removeBreakpoint: function(e) {
          enableBreakpoint(e, false);
        },
        toggleBreakpoint: function(e) {
          refreshBreakpointsGrid();
        },
        removeAllBreakpoints: function(e) {
          clearAllGutters("breakpoints");
        },
        /*
        openPipeline: function(e) {
          
        },
        closePipeline: function(e) {
          clearTabContents();
        },
        */
        setBreakpointCondition: function(e) {
          // breakpointInfo = JSON.parse(e.data);
          refreshBreakpointsGrid();
        },
        conditionalBreakpointError: function(e) {
          showMessage('Error on conditional breakpoint', e.data);
        },
        error: function(e) {
          console.log("Error communicating with SSE debug servlet");
          startReconnectSSE();
        },
        message: function(e) {
          showMessage("Message", e.data);
        }
      }
    });
    sse.start();
    
    $('#layout').w2layout({
      name: 'layout',
      padding: 4,
      panels:[ {
        type: 'top', size: 50, resizable: false, style: pstyle,
        toolbar: {
          items:[ {
            type: 'button', id: 'connection-status', text: 'Not connected', icon: 'far fa-thumbs-down', style: 'color: #DC143C'
          }, {
            type: 'break', id: 'break0'
          }, {
            type: 'check', id: 'toggle-debugger-btn', text: 'Debugger on/off', icon: 'fas fa-bug', checked: false
          }, {
            type: 'break', id: 'break1'
          }, {
            type: 'button', id: 'reload-sidebar-btn', text: 'Reload tree', icon: 'fas fa-sync'
          }, {
            type: 'button', id: 'reload-editor-btn', text: 'Reload editor', icon: 'fas fa-sync'
          }, {
            type: 'break', id: 'break2'
          }, {
            type: 'button', id: 'toggle-breakpoint-btn', text: 'Toggle breakpoint', icon: 'fas fa-toggle-on'
          }, {
            type: 'button', id: 'remove-all-breakpoints-btn', text: 'Remove all breakpoints', icon: 'fas fa-trash-alt'
          }, {
            type: 'break', id: 'break3'
          }, {
            type: 'button', id: 'run-btn', text: 'Run', icon: 'fas fa-play'
          }, {
            type: 'button', id: 'step-btn', text: 'Step', icon: 'fas fa-step-forward'
          }],
          onClick: function (event) {
            switch (event.target) {
              case "toggle-debugger-btn":
                if (event.object.checked) {
                  deactivateDebugSession();
                } else {
                  activateDebugSession();
                };
              break;
              case "reload-sidebar-btn":
                reloadSidebar();
              break;
              case "reload-editor-btn":
                reloadActiveEditor();
              break;
              case "toggle-breakpoint-btn":
                var activeTabId = w2ui['layout'].get('main').tabs.active;
                if (activeTabId == '')
                  break;
                var editor = filesInfo[getPath(activeTabId)].editor; 
                var line = editor.getCursor().line;
                var info = editor.lineInfo(line);
                if (info.gutterMarkers) {
                  removeBreakpoint(editor.options.path, line); 
                } else {
                  setBreakpoint(editor.options.path, line);
                }
              break;
              case "remove-all-breakpoints-btn":
                removeAllBreakpoints();
              break;
              case "run-btn":
                debugRun();
              break;
              case "step-btn":
                debugStep();
              break;
            } 
          }
        }
      }, {
        type: 'left', size: 200, resizable: true, style: pstyle
      }, {
        type: 'main', style: pstyle, overflow: 'hidden',
        tabs: {
          onClick: function (event) {
            $("#editors").append($('#' + this.active + '-container'));
            w2ui.layout.html('main', $('#' + event.target + '-container')[0]);
          },
          onClose: function (event) {
            var currentId = event.target;
            var activateId = null;
            this.tabs.forEach(function (item, index) {
              if (item.id != currentId) {
                activateId = item.id;
              }
            });
            $('#' + currentId + '-container').remove();
            delete filesInfo[getPath(currentId)];
            if (activateId != null) {
              event.onComplete = function () {
                this.click(activateId);
              }
            }
          }
        }
      }, {
        type: 'preview', size: '50%', resizable: true, style: pstyle, content: variablesLayout,
        tabs: {
          name: 'tabs2',
          active: 'variablesTab',
          tabs: [
              { id: 'variablesTab', text: 'Variables' },
              { id: 'expressionTab', text: 'Expression' },
              { id: 'contextTab', text: 'Context' },
              { id: 'stackTab', text: 'Call stack' },
              { id: 'evaluateTab', text: 'Evaluate' },
              { id: 'breakpointsTab', text: 'Breakpoints' }
          ],
          onClick: function (event) {
            switch (event.target) {
              case 'variablesTab':
                w2ui['layout'].html('preview', variablesLayout);
              break;
              case 'expressionTab': 
                w2ui['layout'].html('preview', expressionLayout);
              break;
              case 'contextTab': 
                w2ui['layout'].html('preview', contextLayout);
              break;
              case 'stackTab': 
                w2ui['layout'].html('preview', stackLayout);
              break;
              case 'evaluateTab': 
                w2ui['layout'].html('preview', evaluateLayout);
              break;
              case 'breakpointsTab': 
                w2ui['layout'].html('preview', breakpointsLayout);
              break;
            }
          }
        }
      }],
      onResize: function(event) {
        event.onComplete = function () {
          for (let k in filesInfo) {
            var editor = filesInfo[k].editor;
            var containerId = filesInfo[k].id + "-container";
            editor.setSize("100%", $('#' + containerId).height());
            editor.refresh();  
          }
        }
      }
    });
      
    w2ui['layout'].html('left', $().w2sidebar({
      name: 'sidebar',
      topHTML: '<div style="background-color: #eee; padding: 5px 3px; border-bottom: 1px solid silver">Webapps</div>',
      img: null,
      menu : [
          { id: 1, text: 'Open', icon: 'far fa-folder-open' }
      ],
      onClick: function (event) {
        if (event.node.type != 'file') {
          return;
        }
        var path = event.node.path;
        openOrSelectFile(path, -1, -1);
      },
      onMenuClick: function (event) {
        //
      },
      onContextMenu: function(event) {
        event.onComplete = function () {
        }
      }   
    }));
    
    reloadSidebar();
    
    w2ui['layout'].sizeTo('top', $('div[name=layout_top_toolbar]').height() + 8, true);
    
    setInterval(refreshSession, 60000);
    
    refreshBreakpointsGrid();
    
  });
  
});