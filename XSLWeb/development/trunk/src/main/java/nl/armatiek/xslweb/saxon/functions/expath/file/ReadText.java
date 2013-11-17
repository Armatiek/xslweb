package nl.armatiek.xslweb.saxon.functions.expath.file;

import java.io.File;
import java.nio.charset.UnsupportedCharsetException;

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
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0001Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0004Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0005Exception;

import org.apache.commons.io.FileUtils;

public class ReadText extends ExtensionFunctionDefinition {

  private static final long serialVersionUID = 1L;
  
  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_EXPATH_FILE, "read-text");

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
    return new ReadTextCall();
  }
  
  private static class ReadTextCall extends FileExtensionFunctionCall {
        
    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("rawtypes")
    public SequenceIterator<StringValue> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {      
      try {                        
        File file = getFile(((StringValue) arguments[0].next()).getStringValue());
        if (!file.exists()) {
          throw new FILE0001Exception(file);
        }
        if (file.isDirectory()) {
          throw new FILE0004Exception(file);
        }        
        String encoding = "UTF-8";
        if (arguments.length > 1) {
          encoding = ((StringValue) arguments[1].next()).getStringValue();                   
        }        
        String value;
        try {
          value = FileUtils.readFileToString(file, encoding);
        } catch (UnsupportedCharsetException ece) {
          throw new FILE0005Exception(encoding);
        }                
        return SingletonIterator.makeIterator(new StringValue(value));               
      } catch (Exception e) {
         throw new XPathException(e);
      }
    } 
  }
}