package nl.armatiek.xslweb.saxon.functions.function;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.PJConverter;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.wrapper.VirtualNode;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DateValue;
import net.sf.saxon.value.DayTimeDurationValue;
import net.sf.saxon.value.DecimalValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.DurationValue;
import net.sf.saxon.value.ExternalObject;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.GDayValue;
import net.sf.saxon.value.GMonthDayValue;
import net.sf.saxon.value.GMonthValue;
import net.sf.saxon.value.GYearMonthValue;
import net.sf.saxon.value.GYearValue;
import net.sf.saxon.value.HexBinaryValue;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.TimeValue;
import net.sf.saxon.value.YearMonthDurationValue;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionDefinition;

public class DynamicExtensionFunctionDefinition extends ExtensionFunctionDefinition {
  
  private static HashMap<Class, SequenceType> jpmap = new HashMap<>();

  static {
    jpmap.put(Date.class, SequenceType.OPTIONAL_DATE_TIME);
    jpmap.put(Duration.class, SequenceType.OPTIONAL_DURATION);
    jpmap.put(QName.class, SequenceType.OPTIONAL_QNAME);
  }

  private StructuredQName funcName;
  private int minArguments;
  private int maxArguments;
  private Class<?> returnType;
  private Class<?>[] parameterTypes;
  private boolean hasSideEffects;
  private Class<?> callClass;
  
  public DynamicExtensionFunctionDefinition(Configuration configuration, StructuredQName funcName, 
      Class<?> returnType, Class<?>[] parameterTypes, boolean hasSideEffects, Class<?> callClass) {
    super(configuration);
    this.minArguments = parameterTypes.length;
    this.maxArguments = parameterTypes.length;
    this.parameterTypes = parameterTypes;
    this.returnType = returnType;
    this.hasSideEffects = hasSideEffects;
    this.callClass = callClass;
  }
  
  @Override
  public StructuredQName getFunctionQName() {
    return funcName;
  }
  
  @Override
  public int getMinimumNumberOfArguments() {
    return minArguments;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return maxArguments;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    SequenceType[] types = new SequenceType[parameterTypes.length];
    for (int i=0; i<parameterTypes.length; i++) {
      types[i] = getSequenceType(parameterTypes[i]);
    }
    return types;
  }
  
  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return getSequenceType(returnType);
  }
  
  @Override
  public boolean hasSideEffects() {
    return hasSideEffects;
  }
  
  private Object convertToJava(Item item, Class javaClass) throws XPathException {
    if (item instanceof NodeInfo) {
      Object node = item;
      while (node instanceof VirtualNode) {
        // strip off any layers of wrapping
        node = ((VirtualNode) node).getRealNode();
      }
      return node;
    } else if (item instanceof Function) {
      return item;
    } else if (item instanceof ExternalObject) {
      return ((ExternalObject) item).getObject();
    } else {
      AtomicValue value = (AtomicValue) item;
      switch (value.getItemType().getPrimitiveType()) {
      case StandardNames.XS_STRING:
      case StandardNames.XS_UNTYPED_ATOMIC:
        if (javaClass.equals(String.class)) {
          return value.getStringValue();
        } else if (javaClass.equals(CharSequence.class)) {
          return (CharSequence) value.getStringValue();
        } else if (javaClass.equals(StringValue.class)) {
          return new StringValue(value.getStringValue());
        }
      case StandardNames.XS_ANY_URI:
        AnyURIValue av = (AnyURIValue) value;
        try {
          if (javaClass.equals(URI.class)) {
            return new URI(av.getStringValue());
          } else if (javaClass.equals(URL.class)) {
            return new URL(av.getStringValue());
          } else if (javaClass.equals(AnyURIValue.class)) {
            return av;
          }
        } catch (URISyntaxException e) {
          throw new XPathException("Syntax error in URI \"" + av.getStringValue() + "\"", e);
        } catch (MalformedURLException e) {
          throw new XPathException("Malformed URL \"" + av.getStringValue() + "\"", e);
        }  
      case StandardNames.XS_DURATION:
        if (javaClass.equals(DurationValue.class)) {
          return value;
        }
      case StandardNames.XS_DAY_TIME_DURATION:
        if (javaClass.equals(DayTimeDurationValue.class)) {
          return value;
        }
      case StandardNames.XS_YEAR_MONTH_DURATION:
        if (javaClass.equals(YearMonthDurationValue.class)) {
          return value;
        }
      case StandardNames.XS_G_YEAR:
        if (javaClass.equals(GYearValue.class)) {
          return value;
        }
      case StandardNames.XS_G_YEAR_MONTH:
        if (javaClass.equals(GYearMonthValue.class)) {
          return value;
        }
      case StandardNames.XS_G_MONTH_DAY:
        if (javaClass.equals(GMonthDayValue.class)) {
          return value;
        }
      case StandardNames.XS_G_MONTH:
        if (javaClass.equals(GMonthValue.class)) {
          return value;
        }
      case StandardNames.XS_G_DAY:
        if (javaClass.equals(GDayValue.class)) {
          return value;
        }  
      case StandardNames.XS_BOOLEAN:
        if (javaClass.equals(Boolean.class) || javaClass.equals(boolean.class)) {
          return ((BooleanValue) value).getBooleanValue() ? Boolean.TRUE : Boolean.FALSE;
        } else if (javaClass.equals(BooleanValue.class)) {
          return item;
        }
      case StandardNames.XS_DECIMAL:
        BigDecimal bd = ((BigDecimalValue) value).getDecimalValue();
        if (javaClass.equals(Integer.class) || javaClass.equals(int.class)) {
          return bd.intValue();
        } else if (javaClass.equals(Long.class) || javaClass.equals(long.class)) {
          return bd.longValue();
        } else if (javaClass.equals(Short.class) || javaClass.equals(short.class)) {
          return bd.shortValue();
        } else if (javaClass.equals(Double.class) || javaClass.equals(double.class)) {
          return bd.doubleValue();
        } else if (javaClass.equals(Float.class) || javaClass.equals(float.class)) {
          return bd.floatValue();
        } else if (javaClass.equals(BigDecimal.class)) {
          return bd;
        } else if (javaClass.equals(DecimalValue.class)) {
          return (DecimalValue) value;
        }
      case StandardNames.XS_INTEGER:
        NumericValue nv = ((NumericValue) value);
        if (javaClass.equals(Integer.class) || javaClass.equals(int.class)) {
          return (int) nv.longValue();
        } else if (javaClass.equals(Long.class) || javaClass.equals(long.class)) {
          return (long) nv.longValue();
        } else if (javaClass.equals(Short.class) || javaClass.equals(short.class)) {
          return (short) nv.longValue();
        } else if (javaClass.equals(Byte.class) || javaClass.equals(byte.class)) {
          return (byte) nv.longValue();
        } else if (javaClass.equals(BigInteger.class)) {
          return BigInteger.valueOf(nv.longValue());
        } else if (javaClass.equals(IntegerValue.class)) {
          return (IntegerValue) value;
        }
      case StandardNames.XS_DOUBLE:
        if (javaClass.equals(Double.class) || javaClass.equals(double.class)) {
          return ((DoubleValue) value).getDoubleValue();
        } else if (javaClass.equals(DoubleValue.class)) {
          return (DoubleValue) value;
        }
      case StandardNames.XS_FLOAT:
        if (javaClass.equals(Float.class) || javaClass.equals(float.class)) {
          return ((FloatValue) value).getFloatValue();
        } else if (javaClass.equals(FloatValue.class)) {
          return (FloatValue) value;
        }
      case StandardNames.XS_DATE_TIME:
        if (javaClass.equals(Date.class)) {
          return ((DateTimeValue) value).getCalendar().getTime();
        } else if (javaClass.equals(DateTimeValue.class)) {
          return (DateTimeValue) value;
        }
      case StandardNames.XS_DATE:
        if (javaClass.equals(Date.class)) {
          return ((DateValue) value).getCalendar().getTime();
        } else if (javaClass.equals(DateValue.class)) {
          return (DateValue) value;
        }
      case StandardNames.XS_TIME:
        if (javaClass.equals(TimeValue.class)) {
          return (TimeValue) value;
        }
        return value.getStringValue();
      case StandardNames.XS_BASE64_BINARY:
        if (javaClass.equals(byte[].class)) {
          return ((Base64BinaryValue) value).getBinaryValue();
        } else if (javaClass.equals(Base64BinaryValue.class)) {
          return value;
        }
      case StandardNames.XS_HEX_BINARY:
        if (javaClass.equals(byte[].class)) {
          return ((HexBinaryValue) value).getBinaryValue();
        } else if (javaClass.equals(HexBinaryValue.class)) {
          return value;
        }
      case StandardNames.XS_QNAME:
        if (javaClass.equals(QName.class)) {
          return new QName(((QNameValue) value).getStructuredQName());
        } else if (javaClass.equals(javax.xml.namespace.QName.class)) {
          return ((QNameValue) value).toJaxpQName();
        } else if (javaClass.equals(QNameValue.class)) {
          return value;
        }
      default:
        throw new XPathException("Conversion from \"" + value.getItemType().getDisplayName() + "\" to \"" + javaClass.toString() + "\" is not supported");
      }
    }
  }
  
  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new ExtensionFunctionCall() {
      @SuppressWarnings("unchecked")
      @Override
      public Sequence<?> call(XPathContext context, Sequence[] arguments) throws XPathException {
        try { 
          Method method = callClass.getMethod("call", parameterTypes);
          
          ArrayList<Object> implicitObjects = new ArrayList<Object>();
          for (Class paramType: parameterTypes) {
            if (paramType.equals(XPathContext.class)) {
              implicitObjects.add(context);
              continue;
            } else if (paramType.equals(Context.class)) {
              implicitObjects.add(Context.getInstance());
              continue;
            } else if (paramType.equals(WebApp.class)) {
              implicitObjects.add(getWebApp(context));
              continue;
            } else if (paramType.equals(HttpSession.class)) {
              implicitObjects.add(getSession(context));
              continue;
            } else if (paramType.equals(HttpServletRequest.class)) {
              implicitObjects.add(getRequest(context));
              continue;
            } else if (paramType.equals(HttpServletResponse.class)) {
              implicitObjects.add(getResponse(context));
              continue;
            } 
            break;
          }
          
          Object[] parameters = new Object[implicitObjects.size() + arguments.length];
          
          if (parameters.length != parameterTypes.length) {
            throw new XPathException(
                String.format(
                  "The number of supplied arguments in the call to the XPath extension function \"%s\" (%d) "
                  + "does not match the number of declared arguments in the Java \"call\" method (%d)%s", 
                  funcName.getClarkName(), 
                  arguments.length,
                  parameterTypes.length,
                  implicitObjects.isEmpty() ? "" : ", considering \"" + implicitObjects.size() + " implicit objects"
                ), 
                "TODO");
          }
          
          if (!implicitObjects.isEmpty()) {
            System.arraycopy(implicitObjects.toArray(new Object[implicitObjects.size()]), 0, parameters, 0, implicitObjects.size());
          }
          
          for (int i=0; i<arguments.length; i++) {
            Sequence<Item<?>> argument = arguments[i];
            Class paramType = parameterTypes[i + implicitObjects.size()];
            boolean isArray = paramType.isArray();
            Object param;
            if (isArray) {
              Class componentType = paramType.getComponentType();
              ArrayList<Object> list = new ArrayList<Object>();
              SequenceIterator<Item<?>> si = argument.iterate();
              Item<?> item;
              while ((item = si.next()) != null) {
                list.add(convertToJava(item, componentType));
              }
              if (componentType.isPrimitive()) {
                if (componentType.equals(byte.class)) {
                  byte[] byteArray = new byte[list.size()];
                  for (int j=0; j<list.size(); j++) {
                    byteArray[j] = (byte) list.get(j);
                  }
                  param = byteArray;
                } else if (componentType.equals(short.class)) {
                  short[] shortArray = new short[list.size()];
                  for (int j=0; j<list.size(); j++) {
                    shortArray[j] = (short) list.get(j);
                  }
                  param = shortArray;
                } else if (componentType.equals(int.class)) {
                  int[] intArray = new int[list.size()];
                  for (int j=0; j<list.size(); j++) {
                    intArray[j] = (int) list.get(j);
                  }
                  param = intArray;
                } else if (componentType.equals(long.class)) {
                  long[] longArray = new long[list.size()];
                  for (int j=0; j<list.size(); j++) {
                    longArray[j] = (long) list.get(j);
                  }
                  param = longArray;
                } else if (componentType.equals(float.class)) {
                  float[] floatArray = new float[list.size()];
                  for (int j=0; j<list.size(); j++) {
                    floatArray[j] = (float) list.get(j);
                  }
                  param = floatArray;
                } else if (componentType.equals(double.class)) {
                  double[] doubleArray = new double[list.size()];
                  for (int j=0; j<list.size(); j++) {
                    doubleArray[j] = (double) list.get(j);
                  }
                  param = doubleArray;
                } else if (componentType.equals(boolean.class)) {
                  boolean[] booleanArray = new boolean[list.size()];
                  for (int j=0; j<list.size(); j++) {
                    booleanArray[j] = (boolean) list.get(j);
                  }
                  param = booleanArray;
                } else if (componentType.equals(char.class)) {
                  char[] charArray = new char[list.size()];
                  for (int j=0; j<list.size(); j++) {
                    charArray[j] = (char) list.get(j);
                  }
                  param = charArray;
                } else {
                  throw new UnsupportedOperationException("Unsupported primitive type");
                }
              } else {
                Object[] objArr = list.toArray(new Object[list.size()]);
                param = Arrays.copyOf(objArr, objArr.length, paramType);
              }
            } else {
              /*
              Object obj = convertToJava(argument.head(), paramType);
              if (paramType.isPrimitive()) {
                if (paramType.equals(byte.class)) {
                  param = ((Byte) obj).byteValue();
                } else if (paramType.equals(short.class)) {
                  param = ((Short) obj).shortValue();
                } else if (paramType.equals(int.class)) {
                  if (obj.getClass().equals(Long.class)) {
                    param = ((Long) obj).intValue();
                  } else {
                    param = ((Integer) obj).intValue();  
                  }
                } else if (paramType.equals(long.class)) {
                  param = ((Long) obj).longValue();
                } else if (paramType.equals(float.class)) {
                  param = ((Float) obj).floatValue();
                } else if (paramType.equals(double.class)) {
                  param = ((Double) obj).doubleValue();
                } else if (paramType.equals(boolean.class)) {
                  param = ((Boolean) obj).booleanValue();
                } else if (paramType.equals(char.class)) {
                  param = ((Character) obj).charValue();
                } else {
                  throw new UnsupportedOperationException("Unsupported primitive type");
                }
              } else {
                param = obj;
              }
              */
              param = convertToJava(argument.head(), paramType);
            }
            parameters[i + implicitObjects.size()] = param;
          }
          Object callObj = callClass.newInstance();
          Object resultObj = method.invoke(callObj, parameters);
          JPConverter conv = JPConverter.allocate(returnType, null, context.getConfiguration());
          return conv.convert(resultObj, context);
        } catch (NoSuchMethodException e) {
          throw new XPathException("No method \"call\" found with specified parameters");
        } catch (InvocationTargetException e) {
          throw new XPathException(e);
        } catch (IllegalAccessException e) {
          throw new XPathException(e);
        } catch (IllegalArgumentException e) {
          throw new XPathException("Illegal arguments were passed to the custom extension function", e);
        } catch (InstantiationException e) {
          throw new XPathException("Error instantiating custom extension function", e);
        }
      }
    };
    
  }
  
  private SequenceType getOverrideSequenceType(Class javaClass) {
    if (javaClass.isArray()) {
      Class memberClass = javaClass.getComponentType();
      SequenceType memberType = getOverrideSequenceType(memberClass);
      if (memberType != null) {
        return SequenceType.makeSequenceType(memberType.getPrimaryType(), StaticProperty.ALLOWS_ZERO_OR_MORE);
      }
    }
    return jpmap.get(javaClass);
  }
  
  private SequenceType getSequenceType(Class javaClass) {
    SequenceType type = getOverrideSequenceType(javaClass);
    if (type == null) {
      type = PJConverter.getEquivalentSequenceType(javaClass);
    }
    return type;
  }
  
}