function makeBreakpointMarker() {
  var marker = document.createElement("div");
  marker.style.color = "#822";
  marker.innerHTML = "●";
  return marker;
}

function makeArrowMarker() {
  var marker = document.createElement("div");
  marker.style.color = "#0000FF";
  marker.innerHTML = "⇒";
  return marker;
}

function clearAllGutters(gutterID) {
  for (let k in filesInfo) {
    var editor = filesInfo[k].editor;
    editor.clearGutter(gutterID);
  }
}

function getPath(id) {
  for (let k in filesInfo) {
    if (id == filesInfo[k].id) {
      return filesInfo[k].path;
    }
  }
}

function enableBreakpoint(e, enable) {
  var path = s.strLeft(e.data, '#');
  var line = parseInt(s.strRight(e.data, '#'));
  var fileInfo = filesInfo[path];
  if (fileInfo !== undefined) {
    var info = fileInfo.editor.lineInfo(line);
    if (info != null) {
      fileInfo.editor.setGutterMarker(line, "breakpoints", (enable) ? makeBreakpointMarker() : null);
    }
  }
}

function focusEditor(editor, line, char) {
  if (line > -1) {
    editor.scrollIntoView({line:line, char:char}, 50);
    editor.focus();
    editor.setCursor({line:line, ch:char});
  }
}

function openOrSelectFile(path, line, char) {
  var tabs = w2ui.layout_main_tabs;
  var editor;
  var tab;
  var activeTab = w2ui.layout_main_tabs.active;
  var info = filesInfo[path];
  if (info === undefined) {
    var id = "id-" + tabId++;
    tab = tabs.add({ id: id, text: s.strRightBack(path, '/'), closable: true, path:path });
    var containerId = id + '-container';
    $("#editors").append('<div id="' + containerId + '" style="height:100%;width=100%"><textarea id="' + id + '"></textarea></div>');;
    
    var mode;
    if (path.match(/\.(xml|xsd|xslt?|stx)$/g)) {
      mode = "xml";
    } else if (path.match(/\.(xquery|xqy|xq)$/g)) {
      mode = "xquery";
    } else if (path.match(/\.css$/g)) {
      mode = "css";
    } else if (path.match(/\.js$/g)) {
      mode = "javascript";
    } else {
      mode = "text";
    }
   
    editor = CodeMirror.fromTextArea(document.getElementById(id), {
      mode: mode,
      lineNumbers: true,
      gutters: ["CodeMirror-linenumbers", "breakarrow", "breakpoints"],
      styleActiveLine: true,
      readOnly: true,
      path: path,
      extraKeys: {"Alt-F": "findPersistent"}
    });
    
    editor.on("gutterClick", function(instance, n) {
      var info = instance.lineInfo(n);
      if (info.gutterMarkers) {
        removeBreakpoint(instance.options.path, n);
      } else {
        setBreakpoint(instance.options.path, n);
      }
    });
    
    loadEditor(editor, path, line, char);
    
    /* 
    $.get("dbg-servlet/get-file-contents?path=" + encodeURIComponent(path), function(data) {
      editor.setValue(data);
      
      $.get("dbg-servlet/get-breakpoints?path=" + encodeURIComponent(path), function(data) {
        data.data.forEach(function(line, index) {
          editor.setGutterMarker(line, "breakpoints", makeBreakpointMarker());
        });
      });
      
      focusEditor(editor, line, char);
      
    });
    */
    
    filesInfo[path] = { id:id, path:path, tab:tab, editor:editor};
    if (activeTab != null) {
      $("#editors").append($('#' + activeTab + '-container'));  
    }
    
    w2ui.layout.html('main', $('#' + containerId)[0]);
  
    tabs.select(id);
    
  } else {
    tabs.click(info.id);
    editor = info.editor;
    focusEditor(editor, line, char);
  }
  return editor;
}

function showMessage(title, message) {
  w2popup.open({
    title     : title,
    body      : '<div class="w2ui-centered">' + message + '</div>',
    buttons   : '<button class="w2ui-btn" onclick="w2popup.close();">Ok</button>',
    width     : 500,
    height    : 300,
    overflow  : 'hidden',
    color     : '#333',
    speed     : '0.3',
    opacity   : '0.8',
    modal     : true,
    showClose : true,
    showMax   : false
  });
}

function startReconnectSSE() {
  if (reconnectSSEIntervalId != null) {
    // Already reconnecting...
    return;
  }
  setConnectionButtonState(2);
  console.log("Start reconnecting SSE connection ...");
  try {
    reconnectSSEIntervalId = 1;
    sse.stop();
  } catch(err) {
    console.log(err.message);
  } finally {
    reconnectSSEIntervalId = null;
  }
  reconnectSSEIntervalId = setInterval(function() { 
    console.log("Trying to reconnect to SSE debug servlet ...");
    console.log("Starting SSE ...");
    sse.start();
  }, 2000);
}

function stopReconnectSSE() {
  if (reconnectSSEIntervalId == null) {
    // Already stopped
    return;
  }
  console.log("Stop reconnecting SSE connection ...");
  clearInterval(reconnectSSEIntervalId);
  reconnectSSEIntervalId = null;
  console.log("Stopped reconnecting SSE connection");
}

function setConnectionButtonState(state) {
  var text;
  var style;
  var icon;
  switch(state) {
  case 1:
    text = "Not connected";
    style = "color: #DC143C";
    icon = "far fa-thumbs-down";
    break;
  case 2:
    text = "Connecting ...";
    style = "color: #DC143C";
    icon = "far fa-thumbs-down";
    break;
  case 3:
    text = "Connected";
    style = "color: #228B22";
    icon = "far fa-thumbs-up";
    break;
  }
  var toolbar = w2ui['layout'].get('top').toolbar;
  var button = toolbar.get('connection-status');
  button.text = text;
  button.icon = icon;
  button.style = style;
  toolbar.refresh();
}

function clearTabContents() {
  w2ui['variablesGrid'].clear();
  w2ui['contextGrid'].clear();;  
  w2ui['stackGrid'].clear();
  w2ui['expressionGrid'].clear();
  variablesResult.setValue('');
  contextResult.setValue('');
  stackResult.setValue('');
  evaluateResult.setValue('');
  clearAllGutters("breakarrow");
}