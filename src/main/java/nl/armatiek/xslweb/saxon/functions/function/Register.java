/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.armatiek.xslweb.saxon.functions.function;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExplicitLocation;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.CodedName;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.Fingerprints;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;
import nl.armatiek.xslweb.saxon.utils.NodeInfoUtils;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class Register extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_FUNCTION, "register");

  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 1;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 1;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { SequenceType.makeSequenceType(BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ONE_OR_MORE) };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.OPTIONAL_NODE;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new RegisterFunctionCall();
  }
  
  private static class RegisterFunctionCall extends ExtensionFunctionCall {
    
    private ArrayList<String> codeUnits;
    private ArrayList<String> qualifiedClassNames;
    
    private Iterable<? extends JavaFileObject> getCompilationUnits() {
      List<JavaFileObject> objectList = new ArrayList<JavaFileObject>();
      for (int i=0; i<codeUnits.size(); i++) {
        objectList.add(new JavaStringObject(qualifiedClassNames.get(i), codeUnits.get(i)));
      }
      return objectList;
    }
    
    private static class ClassJavaFileObject extends SimpleJavaFileObject {
      
      private final ByteArrayOutputStream outputStream;
      private final String className;

      protected ClassJavaFileObject(String className, Kind kind) {
        super(URI.create("mem:///" + className.replace('.', '/') + kind.extension), kind);
        this.className = className;
        outputStream = new ByteArrayOutputStream();
      }

      @Override
      public OutputStream openOutputStream() throws IOException {
        return outputStream;
      }

      public byte[] getBytes() {
        return outputStream.toByteArray();
      }

      public String getClassName() {
        return className;
      }
    }
    
    private static class SimpleJavaFileManager extends ForwardingJavaFileManager {

      private final List<ClassJavaFileObject> outputFiles;

      @SuppressWarnings("unchecked")
      protected SimpleJavaFileManager(JavaFileManager fileManager) {
        super(fileManager);
        outputFiles = new ArrayList<ClassJavaFileObject>();
      }

      @Override
      public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        ClassJavaFileObject file = new ClassJavaFileObject(className, kind);
        outputFiles.add(file);
        return file;
      }

      public List<ClassJavaFileObject> getGeneratedOutputFiles() {
        return outputFiles;
      
      }
    }
    
    private static class CompiledClassLoader extends ClassLoader {

      private final List<ClassJavaFileObject> files;
      private final ClassLoader baseLoader;

      private CompiledClassLoader(ClassLoader baseLoader, List<ClassJavaFileObject> files) {
        this.baseLoader = baseLoader;
        this.files = files;
      }

      @Override
      protected Class<?> findClass(String name) throws ClassNotFoundException {
        // Then try to find the class in out ClassJavaFileObjects: 
        Iterator<ClassJavaFileObject> itr = files.iterator();
        while (itr.hasNext()) {
          ClassJavaFileObject file = itr.next();
          if (file.getClassName().equals(name)) {
            itr.remove();
            byte[] bytes = file.getBytes();
            return super.defineClass(name, bytes, 0, bytes.length);
          }
        }
        // Then try to find the class in the base class loader:
        try {
          Class<?> clazz = baseLoader.loadClass(name);
          return clazz;
        } catch (ClassNotFoundException e) {
          // NOP
        }
        // Else in the current class loader:
        return super.findClass(name);
      }
    }
    
    @Override
    public ZeroOrOne<NodeInfo> call(XPathContext context, Sequence[] arguments) throws XPathException {            
      try {      
        // className = "Call" + UUID.randomUUID().toString().replace("-", "");
        qualifiedClassNames = new ArrayList<String>();
        codeUnits = new ArrayList<String>();
        SequenceIterator iter = arguments[0].iterate();
        StringValue item;
        while ((item = (StringValue) iter.next()) != null) {
          codeUnits.add(item.getStringValue().trim());
        }
        
        // Generate a unique package name:
        String packageName = "com.armatiek.xslweb.functions.dynfunc.p" + UUID.randomUUID().toString().replace("-", "");
        
        for (int i=0; i<codeUnits.size(); i++) {
          String codeUnit = codeUnits.get(i);
          
          // Get the class name;
          Pattern p = Pattern.compile("(class\\s+)(\\S+)(\\s*\\{)", Pattern.MULTILINE);
          Matcher m = p.matcher(codeUnit);
          m.find();
          String className = m.group(2);    
          
          if (className == null) {
            throw new XPathException(String.format("Could not determine classname from code unit %d", i), "DF003");
          }
          
          // Delete any existing package name, and add the new one
          codeUnit = codeUnit.replaceFirst("package\\s+[^;]+;", "").trim();
          codeUnit = "package " + packageName + ";\n\n" + codeUnit;          
          
          qualifiedClassNames.add(packageName + "." + className);
          
          codeUnits.set(i, codeUnit);
        }
       
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
          throw new XPathException("No Java compiler could be found. Are you using a JDK version of the Java platform?", "DF002");
        }
        
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        
        List<String> optionList = new ArrayList<String>();
        optionList.addAll(Arrays.asList("-classpath", Context.getInstance().getClassPath()));
        
        SimpleJavaFileManager fileManager = new SimpleJavaFileManager(compiler.getStandardFileManager(null, null, null));
        
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, optionList, null, getCompilationUnits());
        
        NodeInfo diagnosticsNode = null;
        
        if (!task.call()) {
          WebApp webApp = getWebApp(context);
          Fingerprints fingerprints = webApp.getFingerprints();
          NamePool namePool = context.getConfiguration().getNamePool();
          
          PipelineConfiguration config = context.getConfiguration().makePipelineConfiguration();
          TinyBuilder builder = (TinyBuilder) TreeModel.TINY_TREE.makeBuilder(config);
          builder.setLineNumbering(false);
          builder.open();
          builder.startDocument(0);
          builder.startElement(new CodedName(fingerprints.FUNCTION_DIAGNOSTICS, "function", namePool), Untyped.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0);
          List<Diagnostic<? extends JavaFileObject>> diags = diagnostics.getDiagnostics();
          for (Diagnostic<? extends JavaFileObject> diag : diags) {
            builder.startElement(new CodedName(fingerprints.FUNCTION_DIAGNOSTIC, "function", namePool), Untyped.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0);
            builder.attribute(new CodedName(fingerprints.CODE, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, StringUtils.defaultString(diag.getCode()), null, 0); 
            builder.attribute(new CodedName(fingerprints.LINE, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, Long.toString(diag.getLineNumber()), null, 0); 
            builder.attribute(new CodedName(fingerprints.COLUMN, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, Long.toString(diag.getColumnNumber()), null, 0);
            builder.attribute(new CodedName(fingerprints.START, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, Long.toString(diag.getStartPosition()), null, 0); 
            builder.attribute(new CodedName(fingerprints.END, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, Long.toString(diag.getEndPosition()), null, 0); 
            builder.attribute(new CodedName(fingerprints.KIND, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, diag.getKind().name(), null, 0);
            builder.attribute(new CodedName(fingerprints.MESSAGE, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, StringUtils.defaultString(diag.getMessage(null)), null, 0);
            builder.attribute(new CodedName(fingerprints.POSITION, "", namePool), BuiltInAtomicType.UNTYPED_ATOMIC, Long.toString(diag.getPosition()), null, 0); 
            builder.endElement();
          }
          builder.endElement();
          builder.endDocument();
          builder.close();
          diagnosticsNode = NodeInfoUtils.getFirstChildElement(builder.getCurrentRoot());
        } else {
          CompiledClassLoader classLoader = new CompiledClassLoader(getClass().getClassLoader(), fileManager.getGeneratedOutputFiles());  
          for (String className : qualifiedClassNames) {
            Class<?> callClass = ((Class<?>) classLoader.loadClass(className));
            Method[] methods = MethodUtils.getMethodsWithAnnotation(callClass, ExtensionFunction.class);          
            for (Method method: methods) {
              ExtensionFunction extFunc = method.getAnnotation(ExtensionFunction.class);
              StructuredQName functName = new StructuredQName("", extFunc.uri(), extFunc.name()); 
              Class<?> returnType = method.getReturnType();
              Class<?>[] parameterTypes = method.getParameterTypes();
              ExtensionFunctionDefinition funcDef = new DynamicExtensionFunctionDefinition(context.getConfiguration(), 
                  functName, returnType, parameterTypes, extFunc.hasSideEffects(), method);
              getWebApp(context).registerExtensionFunctionDefinition(functName.getClarkName(), funcDef);            
            }
          }
        }
        return new ZeroOrOne<NodeInfo>(diagnosticsNode);
      } catch (Exception e) {
        XPathException xe = XPathException.makeXPathException(e);
        xe.setXPathContext(context);        
        if (xe.getErrorCodeQName() == null) {
          xe.setErrorCode("DF001");
        }
        throw xe;
      }
    }
  }
}