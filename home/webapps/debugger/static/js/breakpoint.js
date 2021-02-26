function Breakpoint(path, line, column, condition, active) {
  this.path = path;
  this.line = line;
  this.column = column;
  this.condition = condition;
  this.active = active;
}

function Breakpoints(path, line, column, condition, active) {
  
  this.breakpoints = {};
  
  this.set = function(path, line, column, condition, active) {
    this.breakpoints[path + '#' + line] = new Breakpoint(path, line, column, condition, active);
    this.store();
  }
  
  this.remove = function(path, line) {
    delete this.breakpoints[path + '#' + line];
    this.store();
  }
  
  this.removeAll = function() {
    this.breakpoints = {};
    this.store();
  }
 
  this.setActive = function(path, line, active) {
    var key = path + "#" + line;
    if (!(key in breakpoints)) {
      return;
    }
    var breakpoint = this.breakpoints[key];
    breakpoint.active = active;
    this.store();
  } 
  
  this.setCondition = function(path, line, condition) {
    var key = path + "#" + line;
    if (!(key in breakpoints)) {
      return;
    }
    var breakpoint = this.breakpoints[key];
    breakpoint.condition = condition;
    this.store();
  }
  
  this.store = function() {
    var json = JSON.stringify(this.breakpoints);
    localStorage.setItem('breakpoints', json);
    this.updateCookie(json);
  }
  
  this.init = function() {
    var json = localStorage.getItem('breakpoints');
    if (json != null) {
      breakpoints = JSON.parse(json);
      this.updateCookie(json);
    }
  }
 
  this.updateCookie = function(json) {
    document.cookie = 'breakpoints=' + encodeURIComponent(json);
  } 
  
  this.init();
  
}