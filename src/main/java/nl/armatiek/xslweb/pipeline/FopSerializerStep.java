package nl.armatiek.xslweb.pipeline;

import java.io.File;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.xml.sax.Attributes;

import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.SAXDestination;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;

public class FopSerializerStep extends SerializerStep {
  
  private static Map<String, FopFactory> fopFactoryMap = Collections.synchronizedMap(new HashMap<String, FopFactory>());
  
  private String configName; 
  private String pdfAMode;
  
  public FopSerializerStep(Attributes atts) {
    super(atts);        
    pdfAMode = getAttribute(atts, "pdf-a-mode", null);
    configName = getAttribute(atts, "config-name", "fop.xconf");
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Destination getDestination(WebApp webApp, HttpServletResponse resp, 
      OutputStream os, Properties outputProperties) throws Exception {    
    resp.setContentType(Definitions.MIMETYPE_PDF);
    FopFactory fopFactory = fopFactoryMap.get(configName);
    if (fopFactory == null) {
      fopFactory = FopFactory.newInstance(new File(Context.getInstance().getHomeDir(), "config/fop/" + configName));      
      fopFactoryMap.put(configName, fopFactory);
    }    
    FOUserAgent userAgent = fopFactory.newFOUserAgent();    
    if (pdfAMode != null) {
      userAgent.getRendererOptions().put("pdf-a-mode", pdfAMode);
    }
    Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, userAgent, os);
    return new SAXDestination(fop.getDefaultHandler());            
  }
  
}