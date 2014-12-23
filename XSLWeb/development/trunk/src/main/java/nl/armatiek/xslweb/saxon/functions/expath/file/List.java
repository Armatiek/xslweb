package nl.armatiek.xslweb.saxon.functions.expath.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrMore;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.ExpectedFileException;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0003Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE9999Exception;
import nl.armatiek.xslweb.utils.XSLWebUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;

public class List extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_EXPATH_FILE, "list");

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
    return 3;
  }

  @Override
  public SequenceType[] getArgumentTypes() {    
    return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.SINGLE_BOOLEAN, SequenceType.SINGLE_STRING };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {    
    return SequenceType.makeSequenceType(BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_MORE);
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new ListCall();
  }
  
  private static class ListCall extends FileExtensionFunctionCall {
        
    @Override
    public ZeroOrMore<StringValue> call(XPathContext context, Sequence[] arguments) throws XPathException {      
      try {         
        File dir = getFile(((StringValue) arguments[0].head()).getStringValue());                       
        if (!dir.isDirectory()) {
          throw new FILE0003Exception(dir);          
        }
        boolean recursive = false;
        if (arguments.length > 1) {
          recursive = ((BooleanValue) arguments[1].head()).getBooleanValue();
        }        
        Pattern regex = null;
        if (arguments.length > 2) {
          String pattern = ((StringValue) arguments[2].head()).getStringValue();
          regex = Pattern.compile(XSLWebUtils.convertGlobToRegex(pattern));
        }
        
        String dirPath = FilenameUtils.normalizeNoEndSeparator(dir.getAbsolutePath(), true) + "/";            
        ArrayList<StringValue> fileList = new ArrayList<StringValue>();
        IOFileFilter fileFilter = (regex != null) ? new RegexFileFilter(regex) : TrueFileFilter.INSTANCE;
        IOFileFilter dirFilter = (recursive) ? TrueFileFilter.INSTANCE : null;                       
        Iterator<File> files = FileUtils.listFilesAndDirs(dir, fileFilter, dirFilter).iterator();
        while (files.hasNext()) {
          File file = files.next();          
          if (file.isDirectory() && regex != null && !regex.matcher(file.getName()).matches()) {
            continue;
          }          
          String filePath = FilenameUtils.normalizeNoEndSeparator(file.getAbsolutePath(), true);
          String relPath = StringUtils.substringAfter(filePath, dirPath);          
          fileList.add(new StringValue(relPath));                    
        }                        
        return new ZeroOrMore<StringValue>(fileList.toArray(new StringValue[fileList.size()]));
      } catch (ExpectedFileException e) {
        throw e;
      } catch (Exception e) {
        throw new FILE9999Exception(e);
      }
    } 
  }
}