package nl.armatiek.xslweb.saxon.uriresolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.lib.StandardURIResolver;
import net.sf.saxon.trans.XPathException;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.web.servlet.InternalRequest;

public class XSLWebURIResolver extends StandardURIResolver {

  @Override
  public Source resolve(String href, String base) throws XPathException {
    try {
      URI uri = new URI(href);
      if (uri.isAbsolute() && uri.getScheme().equals(Definitions.SCHEME_XSLWEB)) {      
        InternalRequest request = new InternalRequest();
        ByteArrayOutputStream boas = new ByteArrayOutputStream();        
        String path = uri.getPath();
        String query = uri.getQuery();
        if (query != null) {
          path = path + "?" + query;
        }        
        request.execute(path, boas);
        return new StreamSource(new ByteArrayInputStream(boas.toByteArray()), href);                
      } 
      return super.resolve(href, base);                  
    } catch (Exception e) {
      throw new XPathException(e);
    }
  }

}