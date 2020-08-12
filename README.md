# XSLWeb
### Web application framework for XSLT and XQuery developers
XSLWeb is an open source and free to use web development framework for XSLT and XQuery developers. It is based on concepts similar to frameworks like [Cocoon](http://cocoon.apache.org/) and [Servlex](http://servlex.net/), but aims to be more easily accessible and pragmatic. 

Using XSLWeb, XSLT/XQuery developers can develop both web applications (dynamic websites) and web services. In essence, an XSLWeb web application is one or more XSLT stylesheets or XQueries that transform an XML representation of the HTTP request (the *Request XML*) to an XML representation of the HTTP response (the *Response XML*). Which specific (pipeline of) XSLT stylesheet or XQueries must be executed for a particular HTTP request is governed by another XSLT stylesheet, the *Request Dispatcher stylesheet*.

After every XSLT transformation step, an optional validation pipeline step (XML Schema or Schematron) can be added to validate the result of the previous transformation step.

During transformations, data sources can be accessed using a built-in library of extension functions that provide HTTP communication (for example to consume REST or SOAP based web services), file and directory access, relational database access and so on.

The result of a pipeline can be serialized to XML, (X)HTML or plain text and using specific serializer pipeline steps to JSON, ZIP files, PDF, Postscript or RTF (using XSL:FO and Apache FOP).

See [Developer Manual](https://armatiek.github.io/xslweb/XSLWeb%20Developer%20Manual.html).

![XSLWeb HTTP request to response flow](https://raw.githubusercontent.com/Armatiek/xslweb/master/docs/xslweb_flow.png)