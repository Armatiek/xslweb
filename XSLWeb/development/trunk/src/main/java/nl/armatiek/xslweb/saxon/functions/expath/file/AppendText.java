package nl.armatiek.xslweb.saxon.functions.expath.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Definitions;

import org.apache.commons.io.FileUtils;

public class AppendText extends ExtensionFunctionDefinition {

  private static final long serialVersionUID = 1L;
  
  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_EXPATH_FILE, "append-text");

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
    return 3;
  }

  @Override
  public SequenceType[] getArgumentTypes() {    
    return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {    
    return SequenceType.EMPTY_SEQUENCE;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new AppendTextCall();
  }
  
  private static class AppendTextCall extends ExtensionFunctionCall {
        
    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("rawtypes")
    public SequenceIterator call(SequenceIterator[] arguments, XPathContext context) throws XPathException {      
      try {         
        String path = ((IntegerValue) arguments[0].next()).getStringValue();        
        File file = (path.startsWith("file:")) ? new File(new URI(path)) : new File(path);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
          throw new XPathException("The specified path does not point to a directory (" + parentFile.getAbsolutePath() + ")");
        }     
        if (file.isDirectory()) {
          throw new XPathException("The specified path points to a directory (" + file.getAbsolutePath() + ")");
        }
        String value = ((IntegerValue) arguments[1].next()).getStringValue();
        String encoding = "UTF-8";
        if (arguments.length > 2) {
          encoding = ((IntegerValue) arguments[2].next()).getStringValue();
        }        
        FileUtils.write(file, value, encoding, true);                
        return EmptyIterator.getInstance();
      } catch (XPathException xe) {
        throw xe;
      } catch (Exception e) {
        throw new XPathException(e);
      }
    } 
  }
}