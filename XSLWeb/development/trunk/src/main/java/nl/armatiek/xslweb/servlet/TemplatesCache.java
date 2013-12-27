package nl.armatiek.xslweb.servlet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.Configuration;
import nl.armatiek.xslweb.configuration.Context;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class TemplatesCache {
  
  private static final Logger logger = LoggerFactory.getLogger(TemplatesCache.class);
  
  private static Map<String, Templates> templatesCache = 
      Collections.synchronizedMap(new HashMap<String, Templates>());
  
  public static Templates getTemplates(String transformationPath,  
      ErrorListener errorListener, Configuration configuration) throws Exception {
    String key = FilenameUtils.normalize(transformationPath);
    Templates templates = (Templates) templatesCache.get(key);
    if (templates == null) {
      logger.info("Compiling and caching stylesheet \"" + transformationPath + "\" ...");
      TransformerFactory factory = new net.sf.saxon.TransformerFactoryImpl(configuration);      
      if (errorListener != null) {
        factory.setErrorListener(errorListener);
      }
      try {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setXIncludeAware(true);
        spf.setValidating(false);
        SAXParser parser = spf.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        Source source = new SAXSource(reader, new InputSource(transformationPath));
        templates = factory.newTemplates(source);
      } catch (Exception e) {
        logger.error("Could not compile stylesheet \"" + transformationPath + "\"", e);
        throw e;
      }      
      if (!Context.getInstance().isDevelopmentMode()) {
        templatesCache.put(key, templates);
      }      
    }
    return templates;
  }
  
  public static void clearCache() {
    templatesCache.clear();
  }
  
}