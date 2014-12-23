package nl.armatiek.xslweb.saxon.functions.expath.file;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class BaseName extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_EXPATH_FILE, "base-name");

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
    return 2;
  }

  @Override
  public SequenceType[] getArgumentTypes() {    
    return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {    
    return SequenceType.SINGLE_STRING;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new BaseNameCall();
  }
  
  private static class BaseNameCall extends FileExtensionFunctionCall {
        
    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
      String path = ((StringValue) arguments[0].head()).getStringValue();
      String suffix = null;
      if (arguments.length > 1) {
        suffix = ((StringValue) arguments[1].head()).getStringValue();
      }      
      String baseName;      
      if (StringUtils.containsOnly("/\\")) {
        baseName = "";
      } else if (StringUtils.isBlank(path)) {
        baseName = ".";
      } else {
        baseName = FilenameUtils.getName(path);
      }       
      if (suffix != null && baseName.endsWith(suffix)) {
        baseName = StringUtils.substringBeforeLast(baseName, suffix);
      }            
      return new StringValue(baseName);             
    } 
  }
}