Dropzone.autoDiscover = false;

var myLayout;

var fileName = $("#fileName");
var folderName = $("#folderName");

function closeTab(tabObj) {
  var panelId = tabObj.closest("li").remove().attr("aria-controls");
  $("#" + panelId).remove();
  tabs.tabs("refresh");
}

var tabs = $("#tabs").tabs();
tabs.on("click", "span.ui-icon-close", function() {
  var tab = $(this).closest("li").find("a");
  if (tab.data("modified")) {
	saveConfirmDlg.data("path", tab.data("path"));
	saveConfirmDlg.data("editor", tab.data("editor"));
	saveConfirmDlg.data("tabObj", $(this));
	saveConfirmDlg.dialog("open");
  } else
    closeTab($(this));
});

tabs.on( "keyup", function( event ) {
  if ( event.altKey && event.keyCode === $.ui.keyCode.BACKSPACE ) {
    var panelId = tabs.find( ".ui-tabs-active" ).remove().attr( "aria-controls" );
    $( "#" + panelId ).remove();
    tabs.tabs("refresh");
  }
});

var toolbar = $("#toolbar").controlgroup();
$("#index").selectmenu();
$("#saveBtn").button({
  "icon": "ui-icon-arrowstop-1-s",
  "showLabel": true
});
$("#runBtn").button({
  "icon": "ui-icon-play",
  "showLabel": true
});
$("#uploadBtn").button({
  "icon": " ui-icon-arrowthick-1-n",
  "showLabel": true
});
$("#searchBtn").button({
  "icon": "ui-icon-search",
  "showLabel": true
});
$("#replaceBtn").button({
  "icon": "ui-icon-circle-zoomin",
  "showLabel": true
});


var newFileDlg = $("#newFileDlg").dialog({
  autoOpen: false,
  resizable: false,
  modal: true,
  buttons: {
    Create: function() {
      var path = newFileDlg.data("path");
      var filePath = path + '/' + fileName.val();
      $.post("xmlindex-ide/createfile", { path: filePath });
      $("#filesystem").jstree(true).refresh_node(path);
      var codeMirror = createTextEditor(filePath, "");
      $(this).dialog("close");
    },
    Cancel: function() {
      $(this).dialog("close");
    }
  },
  open: function() {
    $(this).parents('.ui-dialog-buttonpane button:eq(0)').focus(); 
  },
  close: function() {
    document.forms[0].reset();
  }
});

var newFolderDlg = $("#newFolderDlg").dialog({
  autoOpen: false,
  resizable: false,
  modal: true,
  buttons: {
    Create: function() {
      $.post("xmlindex-ide/createfolder", { path: newFolderDlg.data("path") + '/' + folderName.val() });
      $("#filesystem").jstree(true).refresh_node(newFolderDlg.data("path"));	
      $(this).dialog("close");
    },
    Cancel: function() {
      $(this).dialog("close");
    }
  },
  open: function() {
    $(this).parents('.ui-dialog-buttonpane button:eq(0)').focus(); 
  },
  close: function() {
    document.forms[0].reset();
  }
});

var saveConfirmDlg = $("#saveConfirmDlg").dialog({
  autoOpen: false,
  resizable: false,
  modal: true,
  buttons: {
    "Save": function() {
      $.post("xmlindex-ide/save", { path: saveConfirmDlg.data("path"), code: saveConfirmDlg.data("editor").getValue() });
      closeTab(saveConfirmDlg.data("tabObj"));
      $(this).dialog("close");
    },
    "Don't Save": function() {
      closeTab(saveConfirmDlg.data("tabObj"));
      $(this).dialog("close");
    },
    Cancel: function() {
      $(this).dialog("close");
    }
  },
  open: function() {
    $(this).parents('.ui-dialog-buttonpane button:eq(0)').focus(); 
  },
});

var uploadDlg = $("#uploadDlg").dialog({
  autoOpen: false,
  resizeable: true,
  height: $(window).height() * 0.6,
  width: $(window).width() * 0.5,
  modal: true,
  buttons: {
    Close: function() {
      $(this).dialog("close");
    }
  },
  open: function() {
    Dropzone.forElement("#dropzone").removeAllFiles(true);
  }
});


$("#dropzone").dropzone({
  url: "xmlindex-ide/uploaddocument",
  paramName: "file",
  maxFilesize: 5,
  parallelUploads: 1,
  autoProcessQueue: true,
  dictDefaultMessage: 'Drop XML files here or click to upload',
  init: function() {
    this.hiddenFileInput.click();
  },
  accept: function(file, done) {
    done();
  },
  success: function (file, responseText) {
  },
  sending: function(file, xhr, formData) {
    formData.append("index", $("#index").val());
  }
});

var tabTemplate = "<li><a href='#{href}'><span>#{label}</span></a><span class='ui-icon ui-icon-close' role='presentation'>Remove Tab</span></li>";
var tabCounter = 0;

function createTextEditor(path, code) {
  var fileName = path.substring(path.lastIndexOf('/')  + 1);
  var ext = fileName.substring(fileName.lastIndexOf('.')  + 1);
  var mode = CodeMirror.findModeByExtension(ext);
  
  mode = (mode == undefined) ? CodeMirror.findModeByExtension('xml') : mode;
  
  var theme;
  if (mode.mode == 'xquery')
    theme = 'xq-light';
  else
	theme = 'default';
  
  var id = "tab_" + tabCounter;
  li = $( tabTemplate.replace( /#\{href\}/g, "#" + id ).replace( /#\{label\}/g, fileName ) );
  tabs.find(".ui-tabs-nav").append(li);	
  tabs.find(".ui-layout-content").append("<div id='" + id + "'/></div>");
  tabs.tabs("refresh");
  tabCounter++;
  
  var codeMirror = CodeMirror(function(elt) {
    var div = document.getElementById(id);
	div.appendChild(elt);
  }, {	
    theme: theme,
    mode: mode.mode,
    styleActiveLine: true,
    lineNumbers: true,
    lineWrapping: true,
    matchBrackets: true,
    extraKeys: {
      "F11": function(cm) {
         cm.setOption("fullScreen", !cm.getOption("fullScreen"));
	   },
      "Esc": function(cm) {
        if (cm.getOption("fullScreen")) cm.setOption("fullScreen", false);
      },
	  "Alt-F": "findPersistent"
	}
  });
  
  codeMirror.setValue(code);
  codeMirror.clearHistory();
  
  codeMirror.on("change", function(cm, change) {
	var tab = $("#tabs .ui-tabs-active a");
	if (!tab.data("modified")) {
	  tab.data("modified", true);
	  tab.text(tab.text() + " *");
	}
  });
  
  $("a[href='#" + id + "']").click();
  
  codeMirror.setSize("100%", $('.ui-layout-center').height() - $('#toolbar').height() - $('.ui-tabs-nav').height() - 10);
  codeMirror.display.wrapper.style.fontSize = '13px';
  codeMirror.refresh();
  
  $('#tabs a:last').data("path", path);
  $('#tabs a:last').data("modified", false);
  $('#tabs a:last').data("editor", codeMirror);
  
  return codeMirror;
};

function setStatusMessage(messageHtml) {
  $("#statusPanel").html(messageHtml);
}

$(function() {
	
  $(window).on("beforeunload", function() {
	  var modifications = false;
    $("#tabs a").each(function() { 
      if ($(this).data("modified"))
        modifications = true;
	  });
    if (modifications)
      return "There are text editors with unsaved changes. Are you sure you want to close this browser window?"; 
  });
	
  $(".header-footer").hover(
    function(){ $(this).addClass('ui-state-hover'); },
    function(){ $(this).removeClass('ui-state-hover'); }
  );

  myLayout = $('#optional-container').layout({
	  onresize: function () {
	    $("#tabs a").each(function() {
	      $(this).data('editor').setSize("100%", $('.ui-layout-center').height() - $('#toolbar').height() - $('.ui-tabs-nav').height() - 10);
	    });
	    resultsCodeMirror.setSize("100%", $('.ui-layout-south').height() - $('#statusPanel').height() - 12);
    },
    west__size  : "15%",
    south__size : "40%"
  });
  
  var resultsCodeMirror = CodeMirror.fromTextArea(document.getElementById("resultsTextArea"), {
    theme: 'default',
    mode: 'xml',
    lineNumbers: true,
    lineWrapping: true,
    readOnly: true,
    extraKeys: {
      "F11": function(cm) {
        cm.setOption("fullScreen", !cm.getOption("fullScreen"));
      },
      "Esc": function(cm) {
        if (cm.getOption("fullScreen")) cm.setOption("fullScreen", false);
      },
      "Alt-F": "findPersistent"
    }
  });
  
  resultsCodeMirror.setSize("100%", $('.ui-layout-south').height() - $('#statusPanel').height() - 12);
  resultsCodeMirror.refresh();
  
  $('#saveBtn').on('click', function () {
	  var tab = $("#tabs .ui-tabs-active a");
    $.post("xmlindex-ide/save", { path: tab.data("path"), code: tab.data("editor").getValue() });
    tab.data("modified", false);
    tab.text(tab.text().substring(0, tab.text().length-2));
  });
  
  $('#runBtn').on('click', function () {
	  setStatusMessage('Status : running ...');
	  var tab = $("#tabs .ui-tabs-active a");
	  var editor = tab.data("editor");
    $.post("xmlindex-ide/run", { index: $("#index").val(), code: editor.getValue(), path: tab.data("path") }).done(function(data) {
	    var json = JSON.parse(data);
	    setStatusMessage('Status : Finished in ' + Math.round(json.time / 1000) + ' ms');
	    resultsCodeMirror.setValue(json.result);
	    if (json.errLine && json.errLine > -1) {
	      var col = (json.errColumn && json.errColumn > -1) ? json.errColumn : 0;
		    editor.setCursor(json.errLine - 1, col - 1);
		    editor.focus();
	    }
    }, "json");
  });
  
  $('#uploadBtn').on('click', function () {
    uploadDlg.dialog("open");
  });
  
  $('#searchBtn').on('click', function () {
    $("#tabs .ui-tabs-active a").data("editor").execCommand("find");
  });
  
  $('#replaceBtn').on('click', function () {
    $("#tabs .ui-tabs-active a").data("editor").execCommand("replace");
  });
  
  function customMenu(node) {
    var items = {
      openItem: {
        label: "Open",
        icon: "glyphicon glyphicon-open-file",
        action: function () {
          var alreadyOpen = false;
          $("#tabs a").each(function() {
            if ($(this).data("path") == node.id) {
        	  alreadyOpen = true;
        	  $(this).click();
        	}
          });	
          if (!alreadyOpen) {
            $.get("xmlindex-ide/open", { path: node.id }).done(function(data) {
        	  var codeMirror = createTextEditor(node.id, data);  
            });
          }
        }
      },
      createFileItem: {
        label: "New File",
        icon: "glyphicon glyphicon-plus-sign",
        action: function () {
          newFileDlg.data("path", node.id);
          newFileDlg.dialog("open");
        }
      },
      createFolderItem: {
        label: "New Folder",
        icon: "glyphicon glyphicon-plus-sign",
        action: function () {
          newFolderDlg.data("path", node.id);
          newFolderDlg.dialog("open");
        }
      },
      deleteItem: {
        label: "Delete",
        separator_before: true,
        icon: "glyphicon glyphicon-remove-sign",
        action: function () {
          $.post("xmlindex-ide/deletefile", { path: node.id });
          $("#filesystem").jstree(true).refresh_node($("#filesystem").jstree(true).get_parent(node.id));
        }
      },
    };
  
    if ($(node)[0].type == 'folder') {
      delete items.openItem;
      delete items.deleteItem;
    } else {
      delete items.createFolderItem;
      delete items.createFileItem;
    }
  
    return items;
  }
  
  $('#filesystem').jstree({
    plugins: ["contextmenu", "themes", "types"],
    contextmenu: {items: customMenu},
    types : {
	  'file' : { 'icon' : 'jstree-icon jstree-file' },
      'folder' : { 'icon' : 'jstree-icon jstree-folder' }
    },
    'core' : {
      'data' : {
        "url" : "xmlindex-ide/filesystem",
        "data" : function (node) {
          return { "id" : node.id };
        },
        "dataType" : "json"
      }
    }
  });
  
});