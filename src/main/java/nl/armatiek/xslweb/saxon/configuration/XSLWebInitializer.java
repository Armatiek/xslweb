package nl.armatiek.xslweb.saxon.configuration;

import javax.xml.transform.TransformerException;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Initializer;
import nl.armatiek.xslweb.saxon.functions.expath.file.Append;
import nl.armatiek.xslweb.saxon.functions.expath.file.AppendBinary;
import nl.armatiek.xslweb.saxon.functions.expath.file.AppendText;
import nl.armatiek.xslweb.saxon.functions.expath.file.AppendTextLines;
import nl.armatiek.xslweb.saxon.functions.expath.file.BaseName;
import nl.armatiek.xslweb.saxon.functions.expath.file.Copy;
import nl.armatiek.xslweb.saxon.functions.expath.file.CreateDir;
import nl.armatiek.xslweb.saxon.functions.expath.file.Delete;
import nl.armatiek.xslweb.saxon.functions.expath.file.DirName;
import nl.armatiek.xslweb.saxon.functions.expath.file.DirSeparator;
import nl.armatiek.xslweb.saxon.functions.expath.file.Exists;
import nl.armatiek.xslweb.saxon.functions.expath.file.IsDir;
import nl.armatiek.xslweb.saxon.functions.expath.file.IsFile;
import nl.armatiek.xslweb.saxon.functions.expath.file.LastModified;
import nl.armatiek.xslweb.saxon.functions.expath.file.LineSeparator;
import nl.armatiek.xslweb.saxon.functions.expath.file.Move;
import nl.armatiek.xslweb.saxon.functions.expath.file.PathSeparator;
import nl.armatiek.xslweb.saxon.functions.expath.file.PathToNative;
import nl.armatiek.xslweb.saxon.functions.expath.file.PathToURI;
import nl.armatiek.xslweb.saxon.functions.expath.file.ReadBinary;
import nl.armatiek.xslweb.saxon.functions.expath.file.ReadText;
import nl.armatiek.xslweb.saxon.functions.expath.file.ReadTextLines;
import nl.armatiek.xslweb.saxon.functions.expath.file.ResolvePath;
import nl.armatiek.xslweb.saxon.functions.expath.file.Size;
import nl.armatiek.xslweb.saxon.functions.expath.file.Write;
import nl.armatiek.xslweb.saxon.functions.expath.file.WriteBinary;
import nl.armatiek.xslweb.saxon.functions.expath.file.WriteText;
import nl.armatiek.xslweb.saxon.functions.expath.file.WriteTextLines;
import nl.armatiek.xslweb.saxon.functions.log.Log;
import nl.armatiek.xslweb.saxon.functions.response.Cookies;
import nl.armatiek.xslweb.saxon.functions.response.Headers;
import nl.armatiek.xslweb.saxon.functions.response.Session;
import nl.armatiek.xslweb.saxon.functions.response.SetStatus;

import org.expath.httpclient.saxon.SendRequestFunction;
import org.expath.pkg.saxon.EXPathFunctionDefinition;
import org.expath.zip.saxon.BinaryEntryFunction;
import org.expath.zip.saxon.EntriesFunction;
import org.expath.zip.saxon.HtmlEntryFunction;
import org.expath.zip.saxon.TextEntryFunction;
import org.expath.zip.saxon.UpdateEntriesFunction;
import org.expath.zip.saxon.XmlEntryFunction;
import org.expath.zip.saxon.ZipFileFunction;

public class XSLWebInitializer implements Initializer {

  @Override
  public void initialize(Configuration configuration) throws TransformerException {    
    configuration.setXIncludeAware(true);
    
    /* Log */
    configuration.registerExtensionFunction(new Log());
    
    /* Response */
    configuration.registerExtensionFunction(new SetStatus());
    configuration.registerExtensionFunction(new Headers());
    configuration.registerExtensionFunction(new Session());
    configuration.registerExtensionFunction(new Cookies());
          
    /* EXPath File: */
    configuration.registerExtensionFunction(new Append());
    configuration.registerExtensionFunction(new AppendBinary());
    configuration.registerExtensionFunction(new AppendText());
    configuration.registerExtensionFunction(new AppendTextLines());
    configuration.registerExtensionFunction(new BaseName());
    configuration.registerExtensionFunction(new Copy());
    configuration.registerExtensionFunction(new CreateDir());
    configuration.registerExtensionFunction(new Delete());
    configuration.registerExtensionFunction(new DirName());
    configuration.registerExtensionFunction(new DirSeparator());
    configuration.registerExtensionFunction(new Exists());
    configuration.registerExtensionFunction(new IsDir());
    configuration.registerExtensionFunction(new IsFile());
    configuration.registerExtensionFunction(new LastModified());
    configuration.registerExtensionFunction(new LineSeparator());
    configuration.registerExtensionFunction(new nl.armatiek.xslweb.saxon.functions.expath.file.List());
    configuration.registerExtensionFunction(new Move());
    configuration.registerExtensionFunction(new PathSeparator());
    configuration.registerExtensionFunction(new PathToNative());
    configuration.registerExtensionFunction(new PathToURI());
    configuration.registerExtensionFunction(new ReadBinary());
    configuration.registerExtensionFunction(new ReadText());
    configuration.registerExtensionFunction(new ReadTextLines());
    configuration.registerExtensionFunction(new ResolvePath());
    configuration.registerExtensionFunction(new Size());
    configuration.registerExtensionFunction(new Write());
    configuration.registerExtensionFunction(new WriteBinary());
    configuration.registerExtensionFunction(new WriteText());
    configuration.registerExtensionFunction(new WriteTextLines());

    /* EXPath Zip: */
    registerEXPathFunction(new EntriesFunction(), configuration);
    registerEXPathFunction(new UpdateEntriesFunction(), configuration);
    registerEXPathFunction(new ZipFileFunction(), configuration);
    configuration.registerExtensionFunction(new BinaryEntryFunction());
    configuration.registerExtensionFunction(new HtmlEntryFunction());
    configuration.registerExtensionFunction(new TextEntryFunction());
    configuration.registerExtensionFunction(new XmlEntryFunction());
    
    /* EXPath HttpClient: */           
    registerEXPathFunction(new SendRequestFunction(), configuration);    
  }
  
  private void registerEXPathFunction(EXPathFunctionDefinition function, Configuration configuration) {
    function.setConfiguration(configuration);      
    configuration.registerExtensionFunction(function);
  }
  
}