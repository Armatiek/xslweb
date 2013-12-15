package nl.armatiek.xslweb.utils;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.armatiek.xslweb.error.XSLWebException;

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

}