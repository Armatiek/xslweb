package nl.armatiek.xslweb.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.value.ObjectValue;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.Parameter;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.error.XSLWebException;

import org.apache.commons.io.filefilter.DirectoryFileFilter;

public class XSLWebUtils {
  
  private static Pattern variablesPattern = Pattern.compile("\\$\\{(.+?)\\}");
  
  public static String resolveProperties(String sourceString, Properties props) {
    if (sourceString == null) {
      return null;
    }
    Matcher m = variablesPattern.matcher(sourceString);
    StringBuffer result = new StringBuffer();
    while (m.find()) {
      String variable = m.group(1);
      String value = props.getProperty(variable);
      if (value == null) {
        throw new XSLWebException(String.format("No value specified for variable \"%s\"", variable));
      }
      String resolved = resolveProperties(value.toString(), props);
      resolved = resolved.replaceAll("([\\\\\\$])", "\\\\$1");
      m.appendReplacement(result, resolved);
    }
    m.appendTail(result);
    return result.toString();
  }
  
  public static Properties readProperties(File propsFile) throws IOException {    
    if (!propsFile.isFile()) {
      throw new FileNotFoundException(String.format("Properties file \"%s\" not found", propsFile.getAbsolutePath()));
    }
    Properties props = new Properties();
    InputStream is = new BufferedInputStream(new FileInputStream(propsFile));
    try {
      props.load(is);
    } finally {
      is.close();
    } 
    return props;
  }
  
  public static boolean hasSubDirectories(File file) {
    return file.listFiles((FileFilter) DirectoryFileFilter.INSTANCE).length > 0;
  }
  
  /**
   * Converts a standard POSIX Shell globbing pattern into a regular expression
   * pattern. The result can be used with the standard {@link java.util.regex} API to
   * recognize strings which match the glob pattern.
   * <p/>
   * See also, the POSIX Shell language:
   * http://pubs.opengroup.org/onlinepubs/009695399/utilities/xcu_chap02.html#tag_02_13_01
   * 
   * @param pattern A glob pattern.
   * @return A regex pattern to recognize the given glob pattern.
   */
  public static final String convertGlobToRegex(String pattern) {
    StringBuilder sb = new StringBuilder(pattern.length());
    int inGroup = 0;
    int inClass = 0;
    int firstIndexInClass = -1;
    char[] arr = pattern.toCharArray();
    for (int i = 0; i < arr.length; i++) {
      char ch = arr[i];
      switch (ch) {
      case '\\':
        if (++i >= arr.length) {
          sb.append('\\');
        } else {
          char next = arr[i];
          switch (next) {
          case ',':
            // escape not needed
            break;
          case 'Q':
          case 'E':
            // extra escape needed
            sb.append('\\');
          default:
            sb.append('\\');
          }
          sb.append(next);
        }
        break;
      case '*':
        if (inClass == 0)
          sb.append(".*");
        else
          sb.append('*');
        break;
      case '?':
        if (inClass == 0)
          sb.append('.');
        else
          sb.append('?');
        break;
      case '[':
        inClass++;
        firstIndexInClass = i + 1;
        sb.append('[');
        break;
      case ']':
        inClass--;
        sb.append(']');
        break;
      case '.':
      case '(':
      case ')':
      case '+':
      case '|':
      case '^':
      case '$':
      case '@':
      case '%':
        if (inClass == 0 || (firstIndexInClass == i && ch == '^'))
          sb.append('\\');
        sb.append(ch);
        break;
      case '!':
        if (firstIndexInClass == i)
          sb.append('^');
        else
          sb.append('!');
        break;
      case '{':
        inGroup++;
        sb.append('(');
        break;
      case '}':
        inGroup--;
        sb.append(')');
        break;
      case ',':
        if (inGroup > 0)
          sb.append('|');
        else
          sb.append(',');
        break;
      default:
        sb.append(ch);
      }
    }
    return sb.toString();
  }
  
  public static void setPropertyParameters(XsltTransformer transformer, WebApp webApp, File homeDir) throws IOException {
    Properties props = Context.getInstance().getProperties();
    for (String key : props.stringPropertyNames()) {
      String value = props.getProperty(key);      
      transformer.setParameter(new QName(Definitions.NAMESPACEURI_XSLWEB_CONFIGURATION, key), new XdmAtomicValue(value));
    }    
    transformer.setParameter(new QName(Definitions.NAMESPACEURI_XSLWEB_CONFIGURATION, "home-dir"), new XdmAtomicValue(homeDir.getAbsolutePath()));
    transformer.setParameter(new QName(Definitions.NAMESPACEURI_XSLWEB_CONFIGURATION, "webapp-dir"), new XdmAtomicValue(webApp.getHomeDir().getAbsolutePath()));
    transformer.setParameter(new QName(Definitions.NAMESPACEURI_XSLWEB_CONFIGURATION, "development-mode"), new XdmAtomicValue(webApp.getDevelopmentMode()));
  }
  
  public static void setParameters(XsltTransformer transformer, List<Parameter> parameters) throws IOException {
    if (parameters == null) {
      return;
    }
    for (Parameter param : parameters) {
      QName qname = (param.getURI() != null) ? new QName(param.getURI(), param.getName()) : new QName(param.getName());                 
      transformer.setParameter(qname, new XdmValue(param.getValue()));                  
    }        
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static void setObjectParameters(XsltTransformer transformer, WebApp webApp, HttpServletRequest req, HttpServletResponse resp) throws IOException {            
    transformer.setParameter(new QName(Definitions.NAMESPACEURI_XSLWEB_REQUEST, "request"),  XdmValue.wrap(new ObjectValue(req)));
    transformer.setParameter(new QName(Definitions.NAMESPACEURI_XSLWEB_RESPONSE, "response"),  XdmValue.wrap(new ObjectValue(resp)));
    transformer.setParameter(new QName(Definitions.NAMESPACEURI_XSLWEB_WEBAPP, "webapp"),  XdmValue.wrap(new ObjectValue(webApp)));               
  }
}