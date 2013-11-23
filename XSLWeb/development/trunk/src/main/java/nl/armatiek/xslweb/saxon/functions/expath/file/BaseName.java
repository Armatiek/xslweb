package nl.armatiek.xslweb.saxon.functions.expath.file;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class BaseName extends ExtensionFunctionDefinition {

  private static final long serialVersionUID = 1L;
  
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
        
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    public SequenceIterator<StringValue> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {
      String path = ((StringValue) arguments[0].next()).getStringValue();
      String suffix = null;
      if (arguments.length > 1) {
        suffix = ((StringValue) arguments[1].next()).getStringValue();
      }      
      String baseName;      
      if (StringUtils.containsOnly("/\\")) {
        baseName = "";
      } else if (StringUtils.isBlank(path)) {
        baseName = ".";
      } else {
        baseName = FilenameUtils.getName(path);
      }       
      if (baseName.endsWith(suffix)) {
        baseName = StringUtils.substringBeforeLast(baseName, suffix);
      }            
      return SingletonIterator.makeIterator(new StringValue(baseName));             
    } 
  }
}