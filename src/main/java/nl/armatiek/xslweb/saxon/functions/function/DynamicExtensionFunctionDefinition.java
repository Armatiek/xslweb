package nl.armatiek.xslweb.saxon.functions.function;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.PJConverter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionDefinition;

public class DynamicExtensionFunctionDefinition extends ExtensionFunctionDefinition {

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
      types[i] = PJConverter.getEquivalentSequenceType(parameterTypes[i]);
    }
    return types;
  }
  
  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return PJConverter.getEquivalentSequenceType(returnType);
  }
  
  @Override
  public boolean hasSideEffects() {
    return hasSideEffects;
  }
  
  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new ExtensionFunctionCall() {
      @SuppressWarnings("unchecked")
      @Override
      public Sequence<?> call(XPathContext context, Sequence[] arguments) throws XPathException {
        try { 
          Method method = callClass.getMethod("call", parameterTypes);
          ArrayList<Object> parameters = new ArrayList<Object>();
          for (int i=0; i<arguments.length; i++) {
            Sequence<Item<?>> argument = arguments[i];
            Class paramType = parameterTypes[i];
            boolean isArray = paramType.isArray();
            Object param;
            if (isArray) {
              ArrayList<Object> list = new ArrayList<Object>();
              SequenceIterator<Item<?>> si = argument.iterate();
              Item<?> item;
              while ((item = si.next()) != null) {
                list.add(SequenceTool.convertToJava(item));
              }
              Object[] objArr = list.toArray(new Object[list.size()]);
              param = Arrays.copyOf(objArr, objArr.length, paramType); 
            } else {
              param = SequenceTool.convertToJava(argument.head());
            }
            parameters.add(param);
          }
          Object callObj = callClass.newInstance();
          Object resultObj = method.invoke(callObj, parameters.toArray());
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
  
}