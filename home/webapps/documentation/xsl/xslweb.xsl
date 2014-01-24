<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
  <!ENTITY nbsp "&#160;">
]>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xs="http://www.w3.org/2001/XMLSchema" 
  xmlns:xhtml="http://www.w3.org/1999/xhtml" 
  xmlns:config="http://www.armatiek.com/xslweb/configuration" 
  xmlns:req="http://www.armatiek.com/xslweb/request" 
  xmlns:resp="http://www.armatiek.com/xslweb/response"
  xmlns:ser="http://www.armatiek.com/xslweb/functions/serialize"  
  exclude-result-prefixes="#all" 
  version="2.0">

  <xsl:output method="xhtml" indent="yes" omit-xml-declaration="yes" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" />
  
  <xsl:variable name="context-path" select="concat(/*/req:context-path, /*/req:webapp-path)" as="xs:string"/>

  <xsl:template match="/">
    <resp:response status="200">
      <resp:body>
        <xsl:call-template name="body"/>
      </resp:body>
    </resp:response>
  </xsl:template>

  <xsl:template name="body">
    <html lang="en">
      <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta charset="utf-8"/>
        <title>XSLWeb Documentation</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        <meta name="description" content="Description"/>
        <meta name="author" content="Autor"/>
        <link href="{$context-path}/styles/bootstrap.css" rel="stylesheet"/>
        <link href="{$context-path}/styles/bootstrap-responsive.css" rel="stylesheet"/>
        <link href="{$context-path}/styles/prettify.css" rel="stylesheet"/>
        
        <style type="text/css">
          body{
            padding-bottom:40px;
          }
          
          section{
            padding:60px 0 0 0;
          }</style>

        <!--[if lt IE 9]>
          <script src="//html5shim.googlecode.com/svn/trunk/html5.js"></script>
        <![endif]-->
      </head>

      <body data-spy="scroll" data-target=".navbar" id="top">

        <!-- ========================= NAVIGATION BAR >> START ========================= -->
        <div class="navbar navbar-fixed-top navbar-inverse">
          <div class="navbar-inner">
            <div class="container">
              <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"/>
                <span class="icon-bar"/>
                <span class="icon-bar"/>
              </a>
              <a class="brand" href="http://demo.infuse.at/projects/documentation-template/#overview">File Name</a>
              <!-- TIP: Change this for the name of your file -->
              <div class="nav-collapse">
                <ul class="nav">
                  <li class="active">
                    <a href="http://demo.infuse.at/projects/documentation-template/#overview">Overview</a>
                  </li>
                  <li class="">
                    <a href="http://demo.infuse.at/projects/documentation-template/#markup">Markup</a>
                  </li>
                  <li class="">
                    <a href="./Bootstraped Documentation_files/Bootstraped Documentation.htm">Styles</a>
                  </li>
                  <li>
                    <a href="http://demo.infuse.at/projects/documentation-template/#javascript">JavaScript</a>
                  </li>
                  <li>
                    <a href="http://demo.infuse.at/projects/documentation-template/#psd">Photoshop</a>
                  </li>
                  <li class="">
                    <a href="http://demo.infuse.at/projects/documentation-template/#installation">Installation</a>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        <!-- ========================= NAVIGATION BAR >> END ========================= -->



        <!-- ========================= MAIN CONTAINER >> START ========================= -->
        <div class="container">

          <!-- ========================= SECTION: OVERVIEW >> START ========================= -->
          <section id="overview">

            <div class="hero-unit">
              <h1>XSLWeb Documentation</h1>
              <p>Yet Another Documentation Template Crafted with ♥ by <a href="http://themeforest.net/user/Ivor">Ivor</a></p>
              <p>Simple, flexible and responsive documentation template for your premium files made with Twitter Bootstrap. Alerts, Highlighted Code, Tables and more.</p>
              <p class="well well-small">Thank you for purchasing my theme. If you have any questions that are beyond the scope of this help file, please feel free to email via my user page contact form <a href="http://demo.infuse.at/projects/documentation-template/#">here</a>. Thanks so much!</p>
              <div class="row-fluid">
                <div class="span3">
                  <a href="http://demo.infuse.at/projects/documentation-template/#nowhere" class="btn btn-primary btn-large btn-block">Item Support</a>
                </div>
                <div class="span3">
                  <a href="http://demo.infuse.at/projects/documentation-template/#nowhere" class="btn btn-inverse btn-large btn-block">Support Forum</a>
                </div>
                <div class="span3">
                  <a href="http://demo.infuse.at/projects/documentation-template/#changelog" role="button" class="btn btn-success btn-large btn-block" data-toggle="modal">Changelog</a>
                </div>
                <div class="span3">
                  <a href="http://demo.infuse.at/projects/documentation-template/#credits" role="button" class="btn btn-info btn-large btn-block" data-toggle="modal">Credits</a>
                </div>
              </div>

            </div>

            <hr/>

            <div class="row-fluid">
              <div class="span3">
                <table width="100%">
                  <tbody>
                    <tr>
                      <td>Author:</td>
                      <td>Maarten Kroon</td>
                    </tr>
                    <tr>
                      <td>Contact:</td>
                      <td>
                        <a href="http://demo.infuse.at/projects/documentation-template/#nowhere">author@authormail.com</a>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div class="span3">
                <table width="100%">
                  <tbody>
                    <tr>
                      <td>Author URL:</td>
                      <td>
                        <a href="http://demo.infuse.at/projects/documentation-template/#nowhere">http://authorurl.com</a>
                      </td>
                    </tr>
                    <tr>
                      <td>Item URL:</td>
                      <td>
                        <a href="http://demo.infuse.at/projects/documentation-template/#nowhere">http://itemurl.com</a>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div class="span3">
                <table width="100%">
                  <tbody>
                    <tr>
                      <td>Current Version:</td>
                      <td>1.5.0</td>
                    </tr>
                    <tr>
                      <td>Documentation Version</td>
                      <td>1.1.0</td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div class="span3">
                <table width="100%">
                  <tbody>
                    <tr>
                      <td>Created:</td>
                      <td>2012-09-29</td>
                    </tr>
                    <tr>
                      <td>Modified:</td>
                      <td>2012-09-30</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <hr/>

          </section>
          <!-- ========================= SECTION: OVERVIEW >> END ========================= -->



          <!-- ========================= SECTION: MARKUP >> START ========================= -->
          <section id="markup">
            <div class="page-header">
              <h1>Markup <small>Markup customization and highlights</small></h1>
            </div>

            <div class="alert">
              <a class="close" data-dismiss="alert">×</a>
              <strong>Warning!</strong> Cras mattis consectetur purus sit amet fermentum. Aenean lacinia bibendum nulla sed consectetur.. </div>
            <p>This theme is a fixed layout with two columns. All of the information within the main content area is nested within a div with an id of "primaryContent". The sidebar's (column #2) content is within a div with an id of "secondaryContent".</p>

            <hr/>

            <div class="row">
              <div class="span3">
                <h3>General Markup</h3>
                <p> The general template structure is the same throughout the template. Here is the general structure.</p>
              </div>

              <div class="span9">

                <!-- Code Block >> Start -->
                <pre class="prettyprint linenums">
                  <xsl:variable name="params">
                    <output:serialization-parameters xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization">
                      <output:omit-xml-declaration value="yes"/>
                    </output:serialization-parameters>
                  </xsl:variable>
                  
                  <xsl:value-of select="ser:serialize(document('request.xml'), $params)"/>
                </pre>
                <!-- Code Block >> End -->

              </div>
            </div>

            <hr/>

            <div class="row">
              <div class="span3">
                <h3>How To Edit Color</h3>
                <p>If you would like to edit the color, font, or style of any elements in one of these columns, you would do the following:</p>
              </div>

              <div class="span9">

                <!-- Code Block >> Start -->
                <pre class="prettyprint linenums"><ol class="linenums"><li class="L0"><span class="com">#primaryContent a {</span></li><li class="L1"><span class="pln">  color</span><span class="pun">:</span><span class="pln"> </span><span class="com">#someColor;</span></li><li class="L2"><span class="pun">}</span></li></ol></pre>
                <!-- Code Block >> End -->

              </div>
            </div>

            <hr/>

            <div class="row">
              <div class="span3">
                <h3>Change Structure</h3>
                <p>If you find that your new style is not overriding, it is most likely because of a specificity problem. Scroll down in your CSS file and make sure that there isn't a similar style that has more weight.</p>
              </div>

              <div class="span9">

                <!-- Code Block >> Start -->
                <pre class="prettyprint linenums"><ol class="linenums"><li class="L0"><span class="com">#wrap #primaryContent a {</span></li><li class="L1"><span class="pln">  color</span><span class="pun">:</span><span class="pln"> </span><span class="com">#someColor;</span></li><li class="L2"><span class="pun">}</span></li></ol></pre>
                <!-- Code Block >> End -->

              </div>
            </div>
          </section>
          <!-- ========================= SECTION: MARKUP >> END ========================= -->



          <!-- ========================= SECTION: STYLES >> START ========================= -->
          <section id="styles">
            <div class="page-header">
              <h1>Styles <small>Styles customization and highlights</small></h1>
            </div>

            <div class="alert alert-info">
              <b>Heads up!</b> I'm using two CSS files in this theme. The first one is a generic reset file. Many browser interpret the default behavior of html elements differently. By using a general reset CSS file, we can work round this. This file also contains some general styling, such as anchor tag colors, font-sizes, etc. Keep in mind, that these values might be overridden somewhere else in the file. </div>

            <div class="row">
              <div class="span3">
                <h3>Stylesheets</h3>
                <p>Here's a list of the stylesheet files I'm using with this template, you can find more information opening each file:</p>
              </div>
              <div class="span9">
                <table class="table table-bordered table-striped">
                  <thead>
                    <tr>
                      <th>File Name</th>
                      <th>Description</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td>
                        <code>styles.css</code>
                      </td>
                      <td>Lorem ipsum dolor sit amet, consectetur adipiscing elit.</td>
                    </tr>
                    <tr>
                      <td>
                        <code>reset.css</code>
                      </td>
                      <td>Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus. Cras justo odio.</td>
                    </tr>
                    <tr>
                      <td>
                        <code>typography.css</code>
                      </td>
                      <td>Sed posuere consectetur est at lobortis.</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <hr/>

            <div class="row">
              <div class="span3">
                <h3>CSS Structure</h3>
                <p>The second file contains all of the specific stylings for the page. The file is separated into sections using:</p>
              </div>

              <div class="span9">

                <!-- Code Block >> Start -->
                <pre class="pre-scrollable prettyprint linenums"><ol class="linenums"><li class="L0"><span class="com">/* === Header Section === */</span></li><li class="L1"><span class="pln">&nbsp;</span></li><li class="L2"><span class="pln">some code</span></li><li class="L3"><span class="pln">&nbsp;</span></li><li class="L4"><span class="com">/* === Main Section === */</span></li><li class="L5"><span class="pln">&nbsp;</span></li><li class="L6"><span class="pln">some code</span></li><li class="L7"><span class="pln">&nbsp;</span></li><li class="L8"><span class="com">/* === Sidebar Section === */</span></li><li class="L9"><span class="pln">&nbsp;</span></li><li class="L0"><span class="pln">some code</span></li><li class="L1"><span class="pln">&nbsp;</span></li><li class="L2"><span class="com">/* === Footer === */</span></li><li class="L3"><span class="pln">&nbsp;</span></li><li class="L4"><span class="pln">some code</span></li><li class="L5"><span class="pln">&nbsp;</span></li><li class="L6"><span class="pln">etc</span><span class="pun">,</span><span class="pln"> etc</span><span class="pun">.</span></li></ol></pre>
                <!-- Code Block >> End -->

              </div>
            </div>

          </section>
          <!-- ========================= SECTION: STYLES >> END ========================= -->



          <!-- ========================= SECTION: JAVASCRIPT >> START ========================= -->
          <section id="javascript">

            <div class="page-header">
              <h1>JavaScript <small>JavaScript customization and highlights</small></h1>
            </div>

            <div class="row">
              <div class="span3">
                <h3>JavaScript Files</h3>
                <p>Vestibulum id ligula porta felis euismod semper. Curabitur blandit tempus porttitor. Vivamus sagittis lacus vel augue laoreet rutrum faucibus dolor auctor.</p>
              </div>
              <div class="span9">
                <table class="table table-bordered table-striped">
                  <thead>
                    <tr>
                      <th>Tag</th>
                      <th>Description</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td>
                        <code>jquery.js</code>
                      </td>
                      <td>jQuery is a Javascript library that greatly reduces the amount of code that you must write.</td>
                    </tr>
                    <tr>
                      <td>
                        <code>script.js</code>
                      </td>
                      <td>Most of the animation in this site is carried out from the customs scripts. There are a few functions worth looking over.</td>
                    </tr>
                    <tr>
                      <td>
                        <code>myPlugin.js</code>
                      </td>
                      <td>In addition to the custom scripts, I implement a few "tried and true" plugins to create the effects. This plugin is packed, so you won't need to manually edit anything in the file.</td>
                    </tr>
                  </tbody>
                </table>
                
                <table class="table table-bordered table-striped">
                  <thead>
                    <tr>
                      <th>Function</th>
                      <th>Description</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td>
                        <code>mail:send-mail($email as xs:element()) as xs:boolean</code>
                      </td>
                      <td>jQuery is a Javascript library that greatly reduces the amount of code that you must write.</td>
                    </tr>
                    <tr>
                      <td>
                        <code>script.js</code>
                      </td>
                      <td>Most of the animation in this site is carried out from the customs scripts. There are a few functions worth looking over.</td>
                    </tr>
                    <tr>
                      <td>
                        <code>myPlugin.js</code>
                      </td>
                      <td>In addition to the custom scripts, I implement a few "tried and true" plugins to create the effects. This plugin is packed, so you won't need to manually edit anything in the file.</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <hr/>

            <div class="row">
              <div class="span3">
                <h3>Customize Slider</h3>
                <p>If you want to change the slider's speed transition, open up the file <code> scripts.js </code> and change the following code: </p>
              </div>
              <div class="span9">

                <!-- Code Block >> Start -->
                <pre class="prettyprint js linenums:103"><ol class="linenums"><li value="103" class="L2"><span class="com">/* ---------- @ SlidesJS -----------*/</span></li><li class="L3"><span class="pln">$</span><span class="pun">(</span><span class="str">'#banner'</span><span class="pun">).</span><span class="pln">slides</span><span class="pun">({</span></li><li class="L4"><span class="pln">  preload</span><span class="pun">:</span><span class="pln"> </span><span class="kwd">true</span><span class="pun">,</span></li><li class="L5"><span class="pln">  generateNextPrev</span><span class="pun">:</span><span class="pln"> </span><span class="kwd">true</span><span class="pun">,</span></li><li class="L6"><span class="pln">  autoHeight</span><span class="pun">:</span><span class="pln"> </span><span class="kwd">true</span><span class="pun">,</span></li><li class="L7"><span class="pln">  effect</span><span class="pun">:</span><span class="pln"> </span><span class="str">"slide"</span><span class="pun">,</span></li><li class="L8"><span class="pln">  play</span><span class="pun">:</span><span class="pln"> </span><span class="lit">5000</span></li><li class="L9"><span class="pun">});</span></li></ol></pre>
                <!-- Code Block >> End -->

              </div>
            </div>

          </section>
          <!-- ========================= SECTION: JAVASCRIPT >> END ========================= -->



          <!-- ========================= SECTION: PSD >> START ========================= -->
          <section id="psd">
            <div class="page-header">
              <h1>Photoshop Files <small>Photoshop customization and highlights</small></h1>
            </div>
            <p>I've included three psds with this theme:</p>
            <ul class="thumbnails">
              <li class="span3">
                <div class="thumbnail">
                  <img src="./Bootstraped Documentation_files/260x180" alt=""/>
                  <div class="thumb-info">
                    <h5>Thumbnail label</h5>
                    <p>Thumbnail caption right here...</p>
                  </div>
                </div>
              </li>
              <li class="span3">
                <div class="thumbnail">
                  <img src="./Bootstraped Documentation_files/260x180" alt=""/>
                  <div class="thumb-info">
                    <h5>Thumbnail label</h5>
                    <p>Thumbnail caption right here...</p>
                  </div>
                </div>
              </li>
              <li class="span3">
                <div class="thumbnail">
                  <img src="./Bootstraped Documentation_files/260x180" alt=""/>
                  <div class="thumb-info">
                    <h5>Thumbnail label</h5>
                    <p>Thumbnail caption right here...</p>
                  </div>
                </div>
              </li>
              <li class="span3">
                <div class="thumbnail">
                  <img src="./Bootstraped Documentation_files/260x180" alt=""/>
                  <div class="thumb-info">
                    <h5>Thumbnail label</h5>
                    <p>Thumbnail caption right here...</p>
                  </div>
                </div>
              </li>
            </ul>
            <p>If you'd like to change the main image in the header, open "header.psd", make the necessary adjustments, and then save the file as "headerBG.png". Do the same for the buttons.</p>

          </section>
          <!-- ========================= SECTION: PSD >> START ========================= -->



          <!-- ========================= SECTION: INSTALLATION >> START ========================= -->
          <section id="installation">
            <div class="page-header">
              <h1>Installation <small>How to Install the Template</small></h1>
            </div>

            <div class="row">
              <div class="span3">
                <h3>1. Install Theme</h3>
                <p>Here's a brief information about how to install your WordPress theme from scratch</p>
              </div>

              <div class="span9">
                <div class="accordion" id="accordion2">
                  <div class="accordion-group">
                    <div class="accordion-heading">
                      <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="http://demo.infuse.at/projects/documentation-template/#collapseOne"> 1.1) Organize Your Files </a>
                    </div>
                    <div id="collapseOne" class="accordion-body in collapse" style="height: auto;">
                      <div class="accordion-inner">
                        <p>nim pariatur cliche reprehenderit, enim eiusmod high life accusamus terry richardson ad squid. 3 wolf moon officia aute, non cupidatat skateboard dolor brunch. Food truck quinoa nesciunt laborum eiusmod. Brunch 3 wolf moon tempor, sunt aliqua put a bird on it squid single-origin coffee nulla assumenda shoreditch et. Nihil anim keffiyeh helvetica, craft beer labore wes anderson cred nesciunt sapiente ea proident. Ad vegan excepteur butcher vice lomo. Leggings occaecat craft beer farm-to-table, raw denim aesthetic synth nesciunt you probably haven't heard of them accusamus labore sustainable VHS.</p>
                      </div>
                    </div>
                  </div>
                  <div class="accordion-group">
                    <div class="accordion-heading">
                      <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="http://demo.infuse.at/projects/documentation-template/#collapseTwo"> 1.2) Drag Your Files </a>
                    </div>
                    <div id="collapseTwo" class="accordion-body collapse" style="height: 0px;">
                      <div class="accordion-inner">
                        <p>Anim pariatur cliche reprehenderit, enim eiusmod high life accusamus terry richardson ad squid. 3 wolf moon officia aute, non cupidatat skateboard dolor brunch. Food truck quinoa nesciunt laborum eiusmod. Brunch 3 wolf moon tempor, sunt aliqua put a bird on it squid single-origin coffee nulla assumenda shoreditch et. Nihil anim keffiyeh helvetica, craft beer labore wes anderson cred nesciunt sapiente ea proident. Ad vegan excepteur butcher vice lomo. Leggings occaecat craft beer farm-to-table, raw denim aesthetic synth nesciunt you probably haven't heard of them accusamus labore sustainable VHS.</p>
                      </div>
                    </div>
                  </div>
                  <div class="accordion-group">
                    <div class="accordion-heading">
                      <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="http://demo.infuse.at/projects/documentation-template/#collapseThree"> 1.3) Click Activate </a>
                    </div>
                    <div id="collapseThree" class="accordion-body collapse" style="height: 0px;">
                      <div class="accordion-inner">
                        <p>Anim pariatur cliche reprehenderit, enim eiusmod high life accusamus terry richardson ad squid. 3 wolf moon officia aute, non cupidatat skateboard dolor brunch. Food truck quinoa nesciunt laborum eiusmod. Brunch 3 wolf moon tempor, sunt aliqua put a bird on it squid single-origin coffee nulla assumenda shoreditch et. Nihil anim keffiyeh helvetica, craft beer labore wes anderson cred nesciunt sapiente ea proident. Ad vegan excepteur butcher vice lomo. Leggings occaecat craft beer farm-to-table, raw denim aesthetic synth nesciunt you probably haven't heard of them accusamus labore sustainable VHS.</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

            </div>

          </section>
          <!-- ========================= SECTION: INSTALLATION >> END ========================= -->



          <!-- ========================= SECTION: ALERTS >> START ========================= -->
          <section id="alerts">
            <div class="page-header">
              <h1>Alerts <small>Twitter Bootstrap Alerts</small></h1>
            </div>
            <div class="alert">
              <a class="close" data-dismiss="alert">×</a>
              <strong>Info!</strong> Cras mattis consectetur purus sit amet fermentum. Aenean lacinia bibendum nulla sed consectetur.. </div>
            <div class="alert alert-error">
              <a class="close" data-dismiss="alert">×</a>
              <strong>Oh snap!</strong> Cras mattis consectetur purus sit amet fermentum. Aenean lacinia bibendum nulla sed consectetur.. </div>
            <div class="alert alert-success">
              <a class="close" data-dismiss="alert">×</a>
              <strong>Well Done!</strong> Cras mattis consectetur purus sit amet fermentum. Aenean lacinia bibendum nulla sed consectetur.. </div>
            <div class="alert alert-info">
              <a class="close" data-dismiss="alert">×</a>
              <strong>Heads Up!</strong> Cras mattis consectetur purus sit amet fermentum. Aenean lacinia bibendum nulla sed consectetur.. </div>
            <div class="alert alert-block">
              <a class="close" data-dismiss="alert">×</a>
              <h4 class="alert-heading">Warning!</h4>
              <p>Cras mattis consectetur purus sit amet fermentum. Aenean lacinia bibendum nulla sed consectetur..</p>
            </div>

          </section>
          <!-- ========================= SECTION: ALERTS >> END ========================= -->

          <hr/>

          <div class="goodbye">
            <p>Once again, thank you so much for purchasing this theme. As I said at the beginning, I'd be glad to help you if you have any questions relating to this theme. No guarantees, but I'll do my best to assist. If you have a more general question relating to the themes on ThemeForest, you might consider visiting the forums and asking your question in the "Item Discussion" section.</p>
          </div>

          <hr/>

          <footer>
            <p>© Company 2012</p>
          </footer>

        </div>
        <!-- ========================= MAIN CONTAINER >> END ========================= -->


        <!-- CHANGELOG >> START -->
        <div class="modal hide fade" id="changelog" tabindex="-1" role="dialog" aria-labelledby="changelog-label" aria-hidden="true">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3 id="changelog-label">Changelog</h3>
          </div>
          <div class="modal-body">

            <!-- Code Block >> Start -->
            <pre>Legend:
+   Addition
^   Change
-   Removed
*   Security Fix
#   Bug Fix
!   Note</pre>
            <!-- Code Block >> End -->

            <!-- Code Block >> Start -->
            <pre>&lt;p&gt;Sample text here...&lt;/p&gt;</pre>
            <!-- Code Block >> End -->

          </div>
          <div class="modal-footer">
            <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
          </div>
        </div>
        <!-- CHANGELOG >> END -->



        <!-- CREDITS >> START -->
        <div class="modal hide fade" id="credits" tabindex="-1" role="dialog" aria-labelledby="credits-label" aria-hidden="true">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3 id="credits-label">Credits</h3>
          </div>
          <div class="modal-body">
            <p>I've used the following images, icons or other files as listed.</p>
            <h3>JavaScript</h3>
            <ol>
              <li>
                <a href="http://demo.infuse.at/projects/documentation-template/#">jQuery</a>
                <small>— Nullam quis risus eget urna mollis ornare vel eu leo. </small>
              </li>
              <li>
                <a href="http://demo.infuse.at/projects/documentation-template/#">jCarousel</a>
                <small>— Nullam quis risus eget urna mollis ornare vel eu leo. </small>
              </li>
              <li>
                <a href="http://demo.infuse.at/projects/documentation-template/#">CodeCanyon</a>
                <small>— Nullam quis risus eget urna mollis ornare vel eu leo. </small>
              </li>
            </ol>
            <h3>CSS</h3>
            <ol>
              <li>
                <a href="http://demo.infuse.at/projects/documentation-template/#">960.gs</a>
                <small>— Nullam quis risus eget urna mollis ornare vel eu leo.</small>
              </li>
            </ol>
            <h3>Images</h3>
            <ol>
              <li>
                <a href="http://demo.infuse.at/projects/documentation-template/#">PhotoDune</a>
                <small>— Nullam quis risus eget urna mollis ornare vel eu leo. </small>
              </li>
            </ol>
          </div>
          <div class="modal-footer">
            <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
          </div>
        </div>
        <!-- CREDITS >> END -->

        <!-- loading javascripts -->
        <script src="{$context-path}/scripts/jquery.js"/>
        <script src="{$context-path}/scripts/bootstrap.min.js"/>
        <script src="{$context-path}/scripts/prettify.js"/>

        <!-- init javascripts -->
        <script src="{$context-path}/scripts/init.js"/>
      </body>
    </html>

  </xsl:template>

</xsl:stylesheet>
