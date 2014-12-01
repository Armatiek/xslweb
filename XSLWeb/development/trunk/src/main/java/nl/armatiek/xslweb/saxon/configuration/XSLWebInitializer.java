package nl.armatiek.xslweb.saxon.configuration;

import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.TransformerException;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.lib.Initializer;
import nl.armatiek.xslweb.saxon.functions.base64.Base64Decode;
import nl.armatiek.xslweb.saxon.functions.base64.Base64Encode;
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
import nl.armatiek.xslweb.saxon.functions.mail.SendMail;
import nl.armatiek.xslweb.saxon.functions.response.Cookies;
import nl.armatiek.xslweb.saxon.functions.response.Headers;
import nl.armatiek.xslweb.saxon.functions.response.Session;
import nl.armatiek.xslweb.saxon.functions.response.SetStatus;
import nl.armatiek.xslweb.saxon.functions.serialize.Serialize;

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
  
  private Set<String> functionClassNames = new HashSet<String>(); 

  @Override
  public void initialize(Configuration configuration) throws TransformerException {    
    configuration.setXIncludeAware(true);
    
    configuration.setConfigurationProperty(FeatureKeys.RECOVERY_POLICY_NAME, "recoverWithWarnings");
    
    /* Log */
    registerEXPathFunction(new Log(), configuration);
    
    /* Response */
    registerEXPathFunction(new SetStatus(), configuration);
    registerEXPathFunction(new Headers(), configuration);
    registerEXPathFunction(new Session(), configuration);
    registerEXPathFunction(new Cookies(), configuration);
    
    /* Base64 */
    registerEXPathFunction(new Base64Encode(), configuration);
    registerEXPathFunction(new Base64Decode(), configuration);
    
    /* Context */
    registerEXPathFunction(new nl.armatiek.xslweb.saxon.functions.context.GetAttribute(), configuration);
    registerEXPathFunction(new nl.armatiek.xslweb.saxon.functions.context.SetAttribute(), configuration);
    
    /* Session */
    registerEXPathFunction(new nl.armatiek.xslweb.saxon.functions.session.GetAttribute(), configuration);
    registerEXPathFunction(new nl.armatiek.xslweb.saxon.functions.session.SetAttribute(), configuration);
    
    /* Webapp */
    registerEXPathFunction(new nl.armatiek.xslweb.saxon.functions.webapp.GetAttribute(), configuration);
    registerEXPathFunction(new nl.armatiek.xslweb.saxon.functions.webapp.SetAttribute(), configuration);
    registerEXPathFunction(new nl.armatiek.xslweb.saxon.functions.webapp.GetCacheValue(), configuration);
    registerEXPathFunction(new nl.armatiek.xslweb.saxon.functions.webapp.SetCacheValue(), configuration);
    
    /* Email */
    registerEXPathFunction(new SendMail(), configuration);   
    
    /* Serialize */
    registerEXPathFunction(new Serialize(), configuration);   
    
    /* EXPath File: */
    registerEXPathFunction(new Append(), configuration);
    registerEXPathFunction(new AppendBinary(), configuration);
    registerEXPathFunction(new AppendText(), configuration);
    registerEXPathFunction(new AppendTextLines(), configuration);
    registerEXPathFunction(new BaseName(), configuration);
    registerEXPathFunction(new Copy(), configuration);
    registerEXPathFunction(new CreateDir(), configuration);
    registerEXPathFunction(new Delete(), configuration);
    registerEXPathFunction(new DirName(), configuration);
    registerEXPathFunction(new DirSeparator(), configuration);
    registerEXPathFunction(new Exists(), configuration);
    registerEXPathFunction(new IsDir(), configuration);
    registerEXPathFunction(new IsFile(), configuration);
    registerEXPathFunction(new LastModified(), configuration);
    registerEXPathFunction(new LineSeparator(), configuration);
    registerEXPathFunction(new nl.armatiek.xslweb.saxon.functions.expath.file.List(), configuration);
    registerEXPathFunction(new Move(), configuration);
    registerEXPathFunction(new PathSeparator(), configuration);
    registerEXPathFunction(new PathToNative(), configuration);
    registerEXPathFunction(new PathToURI(), configuration);
    registerEXPathFunction(new ReadBinary(), configuration);
    registerEXPathFunction(new ReadText(), configuration);
    registerEXPathFunction(new ReadTextLines(), configuration);
    registerEXPathFunction(new ResolvePath(), configuration);
    registerEXPathFunction(new Size(), configuration);
    registerEXPathFunction(new Write(), configuration);
    registerEXPathFunction(new WriteBinary(), configuration);
    registerEXPathFunction(new WriteText(), configuration);
    registerEXPathFunction(new WriteTextLines(), configuration);

    /* EXPath Zip: */
    registerEXPathFunction(new EntriesFunction(), configuration);
    registerEXPathFunction(new UpdateEntriesFunction(), configuration);
    registerEXPathFunction(new ZipFileFunction(), configuration);
    registerEXPathFunction(new BinaryEntryFunction(), configuration);
    registerEXPathFunction(new HtmlEntryFunction(), configuration);
    registerEXPathFunction(new TextEntryFunction(), configuration);
    registerEXPathFunction(new XmlEntryFunction(), configuration);
    
    /* EXPath HttpClient: */           
    registerEXPathFunction(new SendRequestFunction(), configuration);    
  }
  
  private void registerEXPathFunction(ExtensionFunctionDefinition function, Configuration configuration) {
    if (function instanceof EXPathFunctionDefinition) {
      ((EXPathFunctionDefinition) function).setConfiguration(configuration);
    }              
    configuration.registerExtensionFunction(function);
    functionClassNames.add(function.getClass().getName());
  }
  
  public boolean isFunctionRegistered(String className) {
    return functionClassNames.contains(className);
  }
  
}