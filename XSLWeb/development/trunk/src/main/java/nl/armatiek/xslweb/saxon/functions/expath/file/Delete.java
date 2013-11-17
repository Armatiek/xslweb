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
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0001Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0004Exception;

import org.apache.commons.io.FileUtils;

public class Delete extends ExtensionFunctionDefinition {

  private static final long serialVersionUID = 1L;
  
  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_EXPATH_FILE, "delete");

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
    return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.SINGLE_BOOLEAN };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {    
    return SequenceType.EMPTY_SEQUENCE;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new DeleteCall();
  }
  
  private static class DeleteCall extends FileExtensionFunctionCall {
        
    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("rawtypes")
    public SequenceIterator<Item> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {      
      try {         
        File file = getFile(((StringValue) arguments[0].next()).getStringValue());
        boolean recursive = false;
        if (arguments.length > 1) {
          recursive = ((BooleanValue) arguments[1].next()).getBooleanValue();
        }                
        if (!file.exists()) {
          throw new FILE0001Exception(file);          
        }
        if (file.isDirectory() && !recursive && file.list().length > 0) {
          throw new FILE0004Exception(file); // TODO error message
        }        
        if (file.isDirectory()) {
          FileUtils.deleteDirectory(file);                  
        } else {
          file.delete();
        }
        return EmptyIterator.emptyIterator();       
      } catch (Exception e) {
        throw new XPathException(e);
      }
    } 
  }
}