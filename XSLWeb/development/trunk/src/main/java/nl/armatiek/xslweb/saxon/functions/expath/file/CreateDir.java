package nl.armatiek.xslweb.saxon.functions.expath.file;

import java.io.File;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0002Exception;

public class CreateDir extends ExtensionFunctionDefinition {

  private static final long serialVersionUID = 1L;
  
  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_EXPATH_FILE, "create-dir");

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
    return new SequenceType[] { SequenceType.SINGLE_STRING };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {    
    return SequenceType.EMPTY_SEQUENCE;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new CreateDirCall();
  }
  
  private static class CreateDirCall extends FileExtensionFunctionCall {
        
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    public SequenceIterator<Item> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {      
      try {                
        File dir = getFile(((StringValue) arguments[0].next()).getStringValue());
        if (dir.isFile()) {
          throw new FILE0002Exception(dir);
        }        
        dir.mkdirs();                                              
        return EmptyIterator.emptyIterator();        
      } catch (Exception e) {
        throw new XPathException(e);
      }
    } 
  }
}