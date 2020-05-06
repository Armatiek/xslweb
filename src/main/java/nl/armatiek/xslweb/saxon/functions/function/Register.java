package nl.armatiek.xslweb.saxon.functions.function;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExplicitLocation;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.FingerprintedQName;
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
    return 2;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 2;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.makeSequenceType(BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ONE_OR_MORE) };
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
    
    private static final FingerprintedQName nameDiagnostics = new FingerprintedQName("script", Definitions.NAMESPACEURI_XSLWEB_FX_FUNCTION, "diagnostics");
    private static final FingerprintedQName nameDiagnostic = new FingerprintedQName("script", Definitions.NAMESPACEURI_XSLWEB_FX_FUNCTION, "diagnostic");
    private static final FingerprintedQName nameCode = new FingerprintedQName("", "", "code");
    private static final FingerprintedQName nameLine = new FingerprintedQName("", "", "line");
    private static final FingerprintedQName nameColumn = new FingerprintedQName("", "", "column");
    private static final FingerprintedQName nameStart = new FingerprintedQName("", "", "start");
    private static final FingerprintedQName nameEnd = new FingerprintedQName("", "", "end");
    private static final FingerprintedQName nameKind = new FingerprintedQName("", "", "kind");
    private static final FingerprintedQName nameMessage = new FingerprintedQName("", "", "message");
    private static final FingerprintedQName namePosition = new FingerprintedQName("", "", "position");
    
    private String functionName;
    private Sequence codeUnits;
    private String className;
    
    private Iterable<? extends JavaFileObject> getCompilationUnits() throws XPathException {
      List<JavaFileObject> objectList = new ArrayList<JavaFileObject>();
      SequenceIterator iter = codeUnits.iterate();
      StringValue item;
      while ((item = (StringValue) iter.next()) != null) {
        objectList.add(new JavaStringObject(className, StringUtils.replace(item.getStringValue(), "%CLASSNAME%", className)));
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
        functionName = ((StringValue) arguments[0].head()).getStringValue();        
        codeUnits = arguments[1];
        // className = "p" + UUID.randomUUID().toString().replace('-', '_');
        className = "Test";
        
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        // JavaCompiler compiler = (JavaCompiler) Class.forName("com.sun.tools.javac.api.JavacTool", true, getClass().getClassLoader()).newInstance();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        
        List<String> optionList = new ArrayList<String>();
        optionList.addAll(Arrays.asList("-classpath", Context.getInstance().getClassPath()));
        
        SimpleJavaFileManager fileManager = new SimpleJavaFileManager(compiler.getStandardFileManager(null, null, null));
        
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, optionList, null, getCompilationUnits());
        
        NodeInfo diagnosticsNode = null;
        
        if (!task.call()) {
          PipelineConfiguration config = context.getConfiguration().makePipelineConfiguration();
          TinyBuilder builder = (TinyBuilder) TreeModel.TINY_TREE.makeBuilder(config);
          builder.setLineNumbering(false);
          builder.open();
          builder.startDocument(0);
          builder.startElement(nameDiagnostics, Untyped.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0);
          List<Diagnostic<? extends JavaFileObject>> diags = diagnostics.getDiagnostics();
          for (Diagnostic<? extends JavaFileObject> diag : diags) {
            builder.startElement(nameDiagnostic, Untyped.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0);
            builder.attribute(nameCode, BuiltInAtomicType.UNTYPED_ATOMIC, StringUtils.defaultString(diag.getCode()), null, 0); 
            builder.attribute(nameLine, BuiltInAtomicType.UNTYPED_ATOMIC, Long.toString(diag.getLineNumber()), null, 0); 
            builder.attribute(nameColumn, BuiltInAtomicType.UNTYPED_ATOMIC, Long.toString(diag.getColumnNumber()), null, 0);
            builder.attribute(nameStart, BuiltInAtomicType.UNTYPED_ATOMIC, Long.toString(diag.getStartPosition()), null, 0); 
            builder.attribute(nameEnd, BuiltInAtomicType.UNTYPED_ATOMIC, Long.toString(diag.getEndPosition()), null, 0); 
            builder.attribute(nameKind, BuiltInAtomicType.UNTYPED_ATOMIC, diag.getKind().name(), null, 0);
            builder.attribute(nameMessage, BuiltInAtomicType.UNTYPED_ATOMIC, StringUtils.defaultString(diag.getMessage(null)), null, 0);
            builder.attribute(namePosition, BuiltInAtomicType.UNTYPED_ATOMIC, Long.toString(diag.getPosition()), null, 0); 
            builder.endElement();
          }
          builder.endElement();
          builder.endDocument();
          builder.close();
          diagnosticsNode = NodeInfoUtils.getFirstChildElement(builder.getCurrentRoot());
        } else {
          CompiledClassLoader classLoader = new CompiledClassLoader(getClass().getClassLoader(), fileManager.getGeneratedOutputFiles());
          ExtensionFunctionDefinition funcDef = ((Class<ExtensionFunctionDefinition>) classLoader.loadClass(className)).newInstance();
          getWebApp(context).registerExtensionFunctionDefinition(funcDef.getFunctionQName().getClarkName(), funcDef);
        }
        return new ZeroOrOne<NodeInfo>(diagnosticsNode);
      } catch (Exception e) {
        throw new XPathException("Error compiling and registering script", e);
      }
    }
  }
}