package nl.armatiek.xslweb.saxon.functions.expath.file;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0001Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE9999Exception;

public class LastModified extends ExtensionFunctionDefinition {

  private static final long serialVersionUID = 1L;
  
  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_EXPATH_FILE, "last-modified");

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
    return SequenceType.makeSequenceType(BuiltInAtomicType.DATE_TIME, StaticProperty.EXACTLY_ONE);
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new LastModifiedCall();
  }
  
  private static class LastModifiedCall extends FileExtensionFunctionCall {
        
    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("rawtypes")
    public SequenceIterator<DateTimeValue> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {      
      try {                        
        File file = getFile(((StringValue) arguments[0].next()).getStringValue());
        if (!file.exists()) {
          throw new FILE0001Exception(file);
        }        
        Calendar cal = Calendar.getInstance();        
        cal.setTime(new Date(file.lastModified()));                 
        return SingletonIterator.makeIterator(new DateTimeValue(cal, false));        
      } catch (Exception e) {
        throw new FILE9999Exception(e);
      }
    } 
  }
}