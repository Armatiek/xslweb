package nl.armatiek.xslweb.saxon.trace;

import java.io.File;

import net.sf.saxon.PreparedStylesheet;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.trace.TimingTraceListener;
import net.sf.saxon.trans.XPathException;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.WebApp;

public class XSLWebTimingTraceListener extends TimingTraceListener {
  
  private WebApp webApp;
  private PreparedStylesheet stylesheet;
  
  public XSLWebTimingTraceListener(WebApp webApp) {
    this.webApp = webApp;
  }

  @Override
  public PreparedStylesheet getStyleSheet() throws XPathException {
    try {
      if (this.stylesheet == null) {
        XsltExecutable executable = webApp.tryXsltExecutableCache(
            new File(Context.getInstance().getHomeDir(), "common/xsl/system/trace/timing.xsl").getAbsolutePath(), null, false);
        this.stylesheet = executable.getUnderlyingCompiledStylesheet();
      }
      return this.stylesheet;
    } catch (Exception e) {
      throw new XPathException(e);
    }
  }
  
}