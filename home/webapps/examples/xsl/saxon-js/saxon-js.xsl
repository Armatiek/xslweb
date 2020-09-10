<xsl:stylesheet   
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"    
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  exclude-result-prefixes="#all"
  version="3.0">
 
  <xsl:output method="html" html-version="5.0" indent="yes"/>
  
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
        <title>Book Catalogue</title>
        <link href="styles/saxon-js/books.css" rel="stylesheet" type="text/css"/>
        <script src="scripts/saxon-js/SaxonJS2.rt.js" type="text/javascript"></script>
        <script>
          window.onload = function() {
            SaxonJS.transform({
              stylesheetLocation: "books.sef.json",
              sourceLocation: "books.xml",
              logLevel: 2
            },
            "async");
          }     
        </script>
      </head>
      <body>
        <h1 id="title">Book Catalogue</h1>
        <p>Click the selection button next to a genre to view books for that genre.</p>
        <p>Click on any column heading to sort the table.</p>
        <h2>Genres</h2>
        <div id="genres"/>
        <h2>Books</h2>
        <div id="books"/>
        <div id="sortToolTip" class="tooltip" style="position:fixed; visibility:hidden; left:0px; top:0px">Click to sort on this column</div>
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>