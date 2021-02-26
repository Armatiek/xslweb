<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"  
  xmlns:req="http://www.armatiek.com/xslweb/request"
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:session="http://www.armatiek.com/xslweb/session"
  xmlns:config="http://www.armatiek.com/xslweb/configuration"
  expand-text="yes"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:output method="html" version="5.0" indent="yes"/>
  
  <xsl:variable name="context-path" select="/*/req:context-path || /*/req:webapp-path" as="xs:string"/>
  
  <xsl:template match="/">
    <resp:response status="200">
      <resp:body>
        <xsl:call-template name="body"/>
      </resp:body>
    </resp:response>          
  </xsl:template>
  
  <xsl:template name="body">
    <html>
      <head>
        <title>XSLWeb debugger</title>
        <link rel="stylesheet" type="text/css" href="{$context-path}/css/debugger.css"/>
        <link rel="stylesheet" type="text/css" href="{$context-path}/w2ui/1.5.master/w2ui.min.css"/>
        <link rel="stylesheet" type="text/css" href="{$context-path}/font-awesome/5.15.1/css/all.min.css"/>
        <link rel="stylesheet" type="text/css" href="{$context-path}/codemirror/5.58.3/lib/codemirror.css"/>
        <link rel="stylesheet" type="text/css" href="{$context-path}/codemirror/5.58.3/addon/fold/foldgutter.css"/>
        <link rel="stylesheet" type="text/css" href="{$context-path}/codemirror/5.58.3/addon/search/matchesonscrollbar.css"/>
        <link rel="stylesheet" type="text/css" href="{$context-path}/codemirror/5.58.3/addon/dialog/dialog.css"/>        
        <script type="text/javascript" src="{$context-path}/jquery/3.5.1/jquery-3.5.1.min.js"/>
        <script type="text/javascript" src="{$context-path}/w2ui/1.5.master/w2ui.min.js"/>
        <script type="text/javascript" src="{$context-path}/codemirror/5.58.3/lib/codemirror.js"/>
        <script type="text/javascript" src="{$context-path}/codemirror/5.58.3/mode/xml/xml.js"/>
        <script type="text/javascript" src="{$context-path}/codemirror/5.58.3/mode/xquery/xquery.js"/>
        <script type="text/javascript" src="{$context-path}/codemirror/5.58.3/mode/css/css.js"/>
        <script type="text/javascript" src="{$context-path}/codemirror/5.58.3/mode/javascript/javascript.js"/>
        <script type="text/javascript" src="{$context-path}/codemirror/5.58.3/addon/fold/foldcode.js"/>
        <script type="text/javascript" src="{$context-path}/codemirror/5.58.3/addon/fold/foldgutter.js"/>
        <script type="text/javascript" src="{$context-path}/codemirror/5.58.3/addon/fold/brace-fold.js"/>
        <script type="text/javascript" src="{$context-path}/codemirror/5.58.3/addon/fold/xml-fold.js"/>
        <script type="text/javascript" src="{$context-path}/codemirror/5.58.3/addon/fold/indent-fold.js"/>
        <script type="text/javascript" src="{$context-path}/codemirror/5.58.3/addon/fold/comment-fold.js"/>
        <script type="text/javascript" src="{$context-path}/codemirror/5.58.3/addon/selection/active-line.js"/>
        <script type="text/javascript" src="{$context-path}/codemirror/5.58.3/addon/display/fullscreen.js"/>
        <script type="text/javascript" src="{$context-path}/codemirror/5.58.3/addon/dialog/dialog.js"/>
        <script type="text/javascript" src="{$context-path}/codemirror/5.58.3/addon/search/searchcursor.js"/>
        <script type="text/javascript" src="{$context-path}/codemirror/5.58.3/addon/search/search.js"/>
        <script type="text/javascript" src="{$context-path}/codemirror/5.58.3/addon/scroll/annotatescrollbar.js"/>
        <script type="text/javascript" src="{$context-path}/codemirror/5.58.3/addon/search/matchesonscrollbar.js"/>
        <script type="text/javascript" src="{$context-path}/codemirror/5.58.3/addon/search/jump-to-line.js"/>
        <script type="text/javascript" src="{$context-path}/codemirror/5.58.3/addon/mode/keyword.js"/>
        <script type="text/javascript" src="{$context-path}/jquery-sse/0.14/jquery.sse.min.js"/>
        <script type="text/javascript" src="{$context-path}/underscore.string/3.3.4/underscore.string.min.js"/>
      </head>
      <body style="margin:0px;"> 
        <xsl:sequence select="session:set-attribute('marker', 'marker')"/>
        <div id="layout" style="position:absolute; width:100%; height:100%;"></div>
        <div id="editors" hidden="hidden" style="display:hidden">
          <div id="preview-result-container" style="height:100%;width=100%"><textarea id="preview-result"></textarea></div>
          <div id="context-result-container" style="height:100%;width=100%"><textarea id="context-result"></textarea></div>
          <div id="stack-result-container" style="height:100%;width=100%"><textarea id="stack-result"></textarea></div>
          <div id="evaluate-editor-container" style="height:100%;width=100%"><textarea id="evaluate-editor">/root()</textarea></div>
          <div id="evaluate-result-container" style="height:100%;width=100%"><textarea id="evaluate-result"></textarea></div>
        </div>
        <script type="text/javascript" src="{$context-path}/js/globals.js"/>
        <script type="text/javascript" src="{$context-path}/js/breakpoint.js"/>
        <script type="text/javascript" src="{$context-path}/js/utils.js"/>
        <script type="text/javascript" src="{$context-path}/js/serveractions.js"/>
        <script type="text/javascript" src="{$context-path}/js/variablespanel.js"/>
        <script type="text/javascript" src="{$context-path}/js/expressionpanel.js"/>
        <script type="text/javascript" src="{$context-path}/js/contextpanel.js"/>
        <script type="text/javascript" src="{$context-path}/js/callstackpanel.js"/>
        <script type="text/javascript" src="{$context-path}/js/evaluatepanel.js"/>
        <script type="text/javascript" src="{$context-path}/js/breakpointspanel.js"/>
        <script type="text/javascript" src="{$context-path}/js/debugger.js"/>
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>