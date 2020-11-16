package nl.armatiek.xslweb.web.filter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.owasp.encoder.Encode;

import com.github.rwitzel.streamflyer.core.Modifier;
import com.github.rwitzel.streamflyer.core.ModifyingWriter;
import com.github.rwitzel.streamflyer.regex.AbstractMatchProcessor;
import com.github.rwitzel.streamflyer.regex.MatchProcessor;
import com.github.rwitzel.streamflyer.regex.MatchProcessorResult;
import com.github.rwitzel.streamflyer.regex.RegexModifier;

import nl.armatiek.xslweb.configuration.Attribute;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.pipeline.PipelineHandler;
import nl.armatiek.xslweb.web.servlet.DelegatingServletOutputStream;

public class XSSEncodingFilter implements Filter {
  
  public static final int XSSFILTER_HTML             = 0x01;
  public static final int XSSFILTER_CSS_STRING       = 0x02;
  public static final int XSSFILTER_CSS_URL          = 0x04;
  public static final int XSSFILTER_JAVASCRIPT       = 0x08;
  public static final int XSSFILTER_URI              = 0x10;
  public static final int XSSFILTER_URI_COMPONENT    = 0x20;
  public static final int XSSFILTER_XML              = 0x40;
  public static final int XSSFILTER_CDATA            = 0x80;
  
  public class XSSEncodingResponseWrapper extends HttpServletResponseWrapper {
    
    private ServletOutputStream os;
    private Modifier xssEncodingModifier;
    
    public XSSEncodingResponseWrapper(HttpServletResponse response, Modifier xssEncodingModifier) {
      super(response);
      this.xssEncodingModifier = xssEncodingModifier;
    }
    
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
      this.os = 
          new DelegatingServletOutputStream(
              new WriterOutputStream(
                  new ModifyingWriter(
                      new OutputStreamWriter(
                          getResponse().getOutputStream(), 
                          StandardCharsets.UTF_8), 
                      xssEncodingModifier), 
                  StandardCharsets.UTF_8));
      
      return os;
    }
    
    public void close() throws IOException {
      if (os != null) {
        os.close();
      }
    }
    
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException { }

  @Override
  public void destroy() { }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {  
    PipelineHandler pipelineHandler = (PipelineHandler) request.getAttribute(Definitions.ATTRNAME_PIPELINEHANDLER);
    int xssFilterFlags;
    if (pipelineHandler == null || (xssFilterFlags = pipelineHandler.getXSSFilterFlags()) == 0) {
      chain.doFilter(request, response);
      return;
    }
    
    boolean xssFiltering = getXssFiltering(request, xssFilterFlags);
    ArrayList<Attribute> seq = new ArrayList<Attribute>();
    seq.add(new Attribute(new Boolean(xssFiltering), "xs:boolean"));  
    request.setAttribute(Definitions.ATTRNAME_XSSFILTERING, seq);
    ServletResponse newResponse;
    if (xssFiltering) {
      Modifier xssEncodingModifier = new RegexModifier("\\[\\[%([a-z]{2})(.*?)%\\]\\]", Pattern.DOTALL, new EncodingProcessor(), 1, 2048);
      newResponse = new XSSEncodingResponseWrapper((HttpServletResponse) response, xssEncodingModifier);  
    } else {
      newResponse = response;
    }
    chain.doFilter(request, newResponse);
    if (newResponse instanceof XSSEncodingResponseWrapper) {
      ((XSSEncodingResponseWrapper) newResponse).close();
    }
  }
  
  private boolean getXssFiltering(ServletRequest request, int xssFilterFlags) {
    Enumeration<String> parameterNames = request.getParameterNames();
    while (parameterNames.hasMoreElements()) {
      String paramName = parameterNames.nextElement();
      String[] paramValues = request.getParameterValues(paramName);
      for (int i=0; i<paramValues.length; i++) {
        String paramValue = paramValues[i];
        if (((XSSFILTER_HTML & xssFilterFlags) != 0) && !StringUtils.equals(paramValue, Encode.forHtml(paramValue))) {
          return true;
        }  
        if (((XSSFILTER_CSS_STRING & xssFilterFlags) != 0) && !StringUtils.equals(paramValue, Encode.forCssString(paramValue))) {
          return true;
        }
        if (((XSSFILTER_CSS_URL & xssFilterFlags) != 0) && !StringUtils.equals(paramValue, Encode.forCssUrl(paramValue))) {
          return true;
        }
        if (((XSSFILTER_JAVASCRIPT & xssFilterFlags) != 0) && !StringUtils.equals(paramValue, Encode.forJavaScript(paramValue))) {
          return true;
        }
        if (((XSSFILTER_URI_COMPONENT & xssFilterFlags) != 0) && !StringUtils.equals(paramValue, Encode.forUriComponent(paramValue))) {
          return true;
        }
        if (((XSSFILTER_URI & xssFilterFlags) != 0) && !StringUtils.equals(paramValue, Encode.forUriComponent(paramValue))) {
          return true;
        }
        if (((XSSFILTER_XML & xssFilterFlags) != 0) && !StringUtils.equals(paramValue, Encode.forXml(paramValue))) {
          return true;
        }
        if (((XSSFILTER_CDATA & xssFilterFlags) != 0) && !StringUtils.equals(paramValue, Encode.forCDATA(paramValue))) {
          return true;
        }
      }
    }
    return false;
  }
  
  private static class EncodingProcessor extends AbstractMatchProcessor implements MatchProcessor {

    @Override
    public MatchProcessorResult process(StringBuilder characterBuffer, 
        int firstModifiableCharacterInBuffer, MatchResult matchResult) {
      int start = matchResult.start();
      int end = matchResult.end();
      String method = matchResult.group(1);
      String textToEncode = matchResult.group(2);
      String encodedText;
      switch (method) {
      case "ht" :
        textToEncode = StringEscapeUtils.unescapeXml(textToEncode);
        encodedText = Encode.forHtml(textToEncode);
        break;
      case "cs" :
        encodedText = Encode.forCssString(textToEncode);
        break;
      case "cu" :
        encodedText = Encode.forCssUrl(textToEncode);
        break;
      case "js" :
        encodedText = Encode.forJavaScript(textToEncode);
        break;
      case "ur" :
        textToEncode = StringEscapeUtils.unescapeXml(textToEncode);
        String queryString;
        try {
          queryString = new URI(textToEncode).getQuery();
        } catch (URISyntaxException e) {
          queryString = null;
        }
        ArrayList<String> components = new ArrayList<String>();
        if (queryString != null) {
          for (String param : queryString.split("&")) {
            int idx = param.indexOf('=');
            String component;
            if (idx > -1) {
              component = param.substring(0, idx) + "=" + Encode.forUriComponent(param.substring(idx+1));
            } else { 
              component = param; 
            } 
            components.add(component);
          }
          String newQueryString = StringUtils.join(components, "&"); 
          encodedText = textToEncode.replaceFirst("\\?(.*?)(#.*)?$", newQueryString);
        } else {
          encodedText = textToEncode;
        }  
        break;
      case "uc" :
        encodedText = Encode.forUriComponent(textToEncode);
      case "xm" :
        encodedText = Encode.forXml(textToEncode);
        break;
      case "cd" :
        encodedText = Encode.forCDATA(textToEncode);
        break;
      default:
        encodedText = textToEncode;
      }
      characterBuffer.delete(start, end);
      characterBuffer.insert(start, encodedText);
      return createResult(matchResult, start + encodedText.length(), true);
    }
  }
  
}