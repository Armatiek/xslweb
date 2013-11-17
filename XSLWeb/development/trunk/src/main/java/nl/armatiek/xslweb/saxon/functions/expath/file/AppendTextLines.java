package nl.armatiek.xslweb.saxon.functions.expath.file;

import java.io.File;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0003Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0004Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0005Exception;

import org.apache.commons.io.FileUtils;

public class AppendTextLines extends ExtensionFunctionDefinition {

  private static final long serialVersionUID = 1L;
  
  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_EXPATH_FILE, "append-text-lines");

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
    return new SequenceType[] { 
        SequenceType.SINGLE_STRING, 
        SequenceType.makeSequenceType(BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_MORE), 
        SequenceType.SINGLE_STRING };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {    
    return SequenceType.EMPTY_SEQUENCE;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new AppendTextLinesCall();
  }
  
  private static class AppendTextLinesCall extends FileExtensionFunctionCall {
        
    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("rawtypes")
    public SequenceIterator<Item> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {      
      try {         
        String path = ((StringValue) arguments[0].next()).getStringValue();        
        File file = getFile(path);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
          throw new FILE0003Exception(parentFile);
        }     
        if (file.isDirectory()) {
          throw new FILE0004Exception(file);
        }                
        String encoding = "UTF-8";
        if (arguments.length > 2) {
          encoding = ((StringValue) arguments[2].next()).getStringValue();                   
        }        
        try {
          ArrayList<String> lines = new ArrayList<String>();
          Item item = null;
          while ((item = arguments[1].next()) != null) {
            lines.add(item.getStringValue());            
          }                              
          FileUtils.writeLines(file, encoding, lines, System.getProperty("line.separator"), true);                    
        } catch (UnsupportedCharsetException uce) {
          throw new FILE0005Exception(encoding);
        }
        return EmptyIterator.emptyIterator();
      } catch (Exception e) {
        throw new XPathException(e);
      }
    } 
  }
}