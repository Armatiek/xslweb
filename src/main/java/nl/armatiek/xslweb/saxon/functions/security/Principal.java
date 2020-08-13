package nl.armatiek.xslweb.saxon.functions.security;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class Principal extends ExtensionFunctionDefinition {
  
  private static final Logger logger = LoggerFactory.getLogger(Principal.class);

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_SECURITY, "principal");

  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 0;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 2;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { SequenceType.OPTIONAL_STRING, SequenceType.SINGLE_STRING };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.OPTIONAL_STRING;
  }
  
  @Override
  public boolean hasSideEffects() {
    return true;
  }
  
  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new PrincipalCall();
  }

  private static class PrincipalCall extends ExtensionFunctionCall {
    
    @SuppressWarnings({ "unchecked" })
    private Object getPrincipalFromClassName(Subject subject, String type) {
      Object principal = null;
      try {
        Class cls = Class.forName(type);
        principal = subject.getPrincipals().oneByType(cls);
      } catch (ClassNotFoundException e) {
        if (logger.isErrorEnabled()) {
          logger.error("Unable to find class for type [" + type + "]");
        }
      }
      return principal;
    }
    
    private String getPrincipalProperty(Object principal, String property) throws XPathException {
      String strValue = null;
      try {
        BeanInfo bi = Introspector.getBeanInfo(principal.getClass());

        // Loop through the properties to get the string value of the specified property
        boolean foundProperty = false;
        for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {
          if (pd.getName().equals(property)) {
            Object value = pd.getReadMethod().invoke(principal, (Object[]) null);
            strValue = String.valueOf(value);
            foundProperty = true;
            break;
          }
        }

        if (!foundProperty) {
          final String message = "Property [" + property + "] not found in principal of type [" + principal.getClass().getName() + "]";
          if (logger.isErrorEnabled()) {
            logger.error(message);
          }
          throw new XPathException(message);
        }

      } catch (Exception e) {
        final String message = "Error reading property [" + property + "] from principal of type [" + principal.getClass().getName() + "]";
        if (logger.isErrorEnabled()) {
          logger.error(message, e);
        }
        throw new XPathException(message, e);
      }

      return strValue;
    }
    
    @Override
    public ZeroOrOne<StringValue> call(XPathContext context, Sequence[] arguments) throws XPathException {                            
      Subject subject = SecurityUtils.getSubject();
      if (subject == null) {
        return ZeroOrOne.empty();
      }
      String strValue = null;
      Object principal;
      if (arguments.length == 0 || arguments[0].head() == null) {
        principal = subject.getPrincipal();  
      } else {
        String type = ((StringValue) arguments[0].head()).getStringValue();
        principal = getPrincipalFromClassName(subject, type);
      }
      if (principal != null) {
        if (arguments.length < 2) {
          strValue = principal.toString();
        } else {
          String property = ((StringValue) arguments[1].head()).getStringValue();
          strValue = getPrincipalProperty(principal, property);
        }
      }
      return new ZeroOrOne<StringValue>(strValue == null ? null : new StringValue(strValue));
    }
    
  }

}