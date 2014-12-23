package nl.armatiek.xslweb.saxon.functions.expath.file;

import java.io.File;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.ExpectedFileException;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0003Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0004Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE0005Exception;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FILE9999Exception;

import org.apache.commons.io.FileUtils;

public class WriteTextLinesCall extends FileExtensionFunctionCall {
  
  private boolean append;
  
  public WriteTextLinesCall(boolean append) {
    this.append = append;
  }
  
  @Override
  public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {      
    try {                      
      File file = getFile(((StringValue) arguments[0].head()).getStringValue());
      File parentFile = file.getParentFile();
      if (!parentFile.exists()) {
        throw new FILE0003Exception(parentFile);
      }     
      if (file.isDirectory()) {
        throw new FILE0004Exception(file);
      }                
      String encoding = "UTF-8";
      if (arguments.length > 2) {
        encoding = ((StringValue) arguments[2].head()).getStringValue();                   
      }        
      try {
        ArrayList<String> lines = new ArrayList<String>();
        Item item = null;
        Sequence seq = arguments[1];
        SequenceIterator iter = seq.iterate();
        while ((item = iter.next()) != null) {
          lines.add(item.getStringValue());            
        }                              
        FileUtils.writeLines(file, encoding, lines, System.getProperty("line.separator"), append);                    
      } catch (UnsupportedCharsetException uce) {
        throw new FILE0005Exception(encoding);
      }
      return BooleanValue.TRUE;
    } catch (ExpectedFileException e) {
      throw e;
    } catch (Exception e) {
      throw new FILE9999Exception(e);
    }
  } 
}
