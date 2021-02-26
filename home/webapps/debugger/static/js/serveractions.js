function refreshSession() {
  $.get("dbg-servlet/refresh-session", function(data) {
    processResult(data, true);
  })
  .fail(function() {
    startReconnectSSE();
  });
}

function activateDebugSession() {
  $.get("dbg-servlet/activate-debug-session", function(data) {
    processResult(data, true);    
  })
  .fail(function() {
    startReconnectSSE();
  });
}

function deactivateDebugSession() {
  $.get("dbg-servlet/deactivate-debug-session", function(data) {
    processResult(data, true);    
  })
  .fail(function() {
    startReconnectSSE();
  });
}

function reloadSidebar() {
  var sidebar = w2ui['sidebar'];
  sidebar.lock("Loading...", true);
  sidebar.nodes.forEach(function (item, index) {
    sidebar.remove(item.id);
  });
  $.get("dbg-servlet/get-directory-listing", function(data) {
    sidebar.add(data);  
    sidebar.unlock();
  })
  .fail(function() {
    startReconnectSSE();
  });
}

function reloadActiveEditor() {
  var activeTabId = w2ui['layout'].get('main').tabs.active;
  if (activeTabId == '')
    return;
  var path = getPath(activeTabId);
  var info = filesInfo[path];
  if (info === undefined) {
    return;
  }
  var editor = info.editor;
  loadEditor(editor, path, -1, -1);
}

function loadEditor(editor, path, line, char) {
  $.get("dbg-servlet/get-file-contents?path=" + encodeURIComponent(path), function(data) {
    editor.setValue(data);
    /* Show any previously set breakpoints: */
    $.get("dbg-servlet/get-breakpoints?path=" + encodeURIComponent(path), function(data) {
      data.data.forEach(function(line, index) {
        editor.setGutterMarker(line, "breakpoints", makeBreakpointMarker());
      })
    })
    .fail(function() {
      startReconnectSSE();
    });
    if (line != -1) {
      focusEditor(editor, line, char);
    }
  })
  .fail(function() {
    startReconnectSSE();
  });
}

function debugRun() {
  $.get("dbg-servlet/run", function(data) {
    processResult(data, true);   
  })
  .fail(function() {
    startReconnectSSE();
  });
}

function debugStep() {
  $.get("dbg-servlet/step", function(data) {
    processResult(data, true);    
  })
  .fail(function() {
    startReconnectSSE();
  });
}

function refreshBreakpointsGrid() {
  $.get("dbg-servlet/get-all-breakpoints", function(data) {
    w2ui['breakpointsGrid'].records = data;
    w2ui['breakpointsGrid'].refresh();
  })
  .fail(function() {
    startReconnectSSE();
  });
}

function setBreakpoint(path, line) {
  breakpoints.set(path, line, -1, null, true);
  $.get("dbg-servlet/set-breakpoint?path=" + encodeURIComponent(path) + "&line=" + line, function(data) {
    if (processResult(data, true)) {
      refreshBreakpointsGrid();
    }
  })
  .fail(function() {
    startReconnectSSE();
  });
}

function removeBreakpoint(path, line) {
  breakpoints.remove(path, line);
  $.get("dbg-servlet/remove-breakpoint?path=" + encodeURIComponent(path) + '&line=' + line, function(data) {
    if (processResult(data, true)) {
      refreshBreakpointsGrid();
    }
  })
  .fail(function() {
    startReconnectSSE();
  });
}

function toggleBreakpoint(path, line) {
  $.get("dbg-servlet/toggle-breakpoint?path=" + encodeURIComponent(path) + '&line=' + line, function(data) {
    processResult(data, true);
  })
  .fail(function() {
    startReconnectSSE();
  });
}

function removeAllBreakpoints() {
  breakpoints.removeAll();
  $.get("dbg-servlet/remove-all-breakpoints", function(data) {
    if (processResult(data, true)) {
      refreshBreakpointsGrid();
    }
  })
  .fail(function() {
    startReconnectSSE();
  });
}

function setBreakpointCondition(path, line, condition) {
  breakpoints.setCondition(path, line, condition);
  $.get("dbg-servlet/set-breakpoint-condition?path=" + encodeURIComponent(path) + '&line=' + line + '&condition=' + condition, function(data) {
    if (processResult(data, true)) {
      refreshBreakpointsGrid();
    }
  })
  .fail(function() {
    startReconnectSSE();
  });
}

function showSerializedSequence(id, editor) {
  $.get("dbg-servlet/get-serialized-sequence?id=" + encodeURIComponent(id), function(data) {
    if (processResult(data, false)) {
      editor.setValue(data.message);
    }
  })
  .fail(function() {
    startReconnectSSE();
  });
}

function processResult(data, log) {
  if (log) {
    console.log(data.message);
  }
  if (data.code != 200) {
    showMessage("ERROR", data.message);
    startReconnectSSE();
  }
  return (data.code == 200);
}